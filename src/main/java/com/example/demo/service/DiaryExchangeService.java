package com.example.demo.service;

import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.DiaryExchangeEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.DiaryExchangeRepository;
import com.example.demo.repository.DiaryRepository;
import com.example.demo.repository.BlockedUserRepository;
import com.example.demo.entity.BlockedUserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.entity.MatchNotificationEntity;
import com.example.demo.repository.MatchNotificationRepository;
import com.example.demo.dto.response.MatchNotificationResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryExchangeService {
    private final DiaryRepository diaryRepository;
    private final DiaryExchangeRepository diaryExchangeRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    private final MatchNotificationRepository matchNotificationRepository;

    // 읽지 않은 교환 알림 목록을 조회
    public List<MatchNotificationResponse> findNotifications(Long userId) {
        return matchNotificationRepository.findAllByUserIdAndReadYnFalseOrderByCreatedAtDesc(userId)
                .stream().map(MatchNotificationResponse::from).toList();
    }

    @Transactional
    // 본인의 교환 알림을 읽음 상태로 변경
    public void readNotification(Long userId, Long notificationId) {
        MatchNotificationEntity notification = matchNotificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));
        notification.markRead();
    }

    // 교환하고 싶지 않은 사용자를 차단하고 현재 연결도 종료
    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        if (userId.equals(blockedUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "본인은 차단할 수 없습니다.");
        }
        if (!blockedUserRepository.existsByOwnerUserIdAndBlockedUserId(userId, blockedUserId)) {
            var owner = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
            var blocked = userRepository.findById(blockedUserId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
            blockedUserRepository.save(BlockedUserEntity.create(owner, blocked));
        }
        diaryExchangeRepository.deactivateByUsers(userId, blockedUserId);
    }

    // 차단한 상대를 다시 교환 후보에 포함
    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        blockedUserRepository.findByOwnerUserIdAndBlockedUserId(userId, blockedUserId)
                .ifPresent(blockedUserRepository::delete);
    }

    // 내가 차단한 사용자 ID 목록 조회
    public List<Long> findBlockedUsers(Long userId) {
        return blockedUserRepository.findAllByOwnerUserId(userId).stream()
                .map(block -> block.getBlockedUser().getId()).toList();
    }

    // 사용자가 요청한 일기를 기준으로 매칭을 시도하고, 이미 매칭되어 있으면 기존 교환 정보를 반환
    @Transactional
    public DiaryExchangeResponse matchDiary(Long userId, Long diaryId) {
        DiaryEntity myDiary = diaryRepository.findByIdAndUserIdAndDelYn(diaryId, userId, "N")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."));

        return diaryExchangeRepository.findByOwnerUserIdAndMyDiaryIdAndActiveTrue(userId, diaryId)
                .map(DiaryExchangeResponse::from)
                .orElseGet(() -> createExchangeIfPossible(myDiary));
    }

    // 내 일기와 연결된 활성 교환 정보를 조회
    public DiaryExchangeResponse findExchange(Long userId, Long diaryId) {
        diaryRepository.findByIdAndUserIdAndDelYn(diaryId, userId, "N")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."));
        DiaryExchangeEntity exchange = diaryExchangeRepository.findByOwnerUserIdAndMyDiaryIdAndActiveTrue(userId, diaryId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "교환된 일기가 없습니다."));
        return DiaryExchangeResponse.from(exchange);
    }

    // 사용자가 현재 유지 중인 모든 일기 교환 연결을 조회
    public List<DiaryExchangeResponse> findActiveConnections(Long userId) {
        return diaryExchangeRepository.findAllByOwnerUserIdAndActiveTrueOrderByExchangeDateDescCreatedAtDesc(userId)
                .stream()
                .map(DiaryExchangeResponse::from)
                .toList();
    }

    // 교환 연결을 비활성화하여 더 이상 이어지지 않도록 처리
    @Transactional
    public void disconnect(Long userId, Long exchangeId) {
        DiaryExchangeEntity exchange = diaryExchangeRepository.findByIdAndOwnerUserId(exchangeId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "연결을 찾을 수 없습니다."));
        diaryExchangeRepository.deactivatePair(exchange.getMyDiary().getId(), exchange.getPartnerDiary().getId());
    }

    @Transactional
    // 일기가 삭제되면 양쪽 사용자에게 만들어진 교환 연결도 함께 비활성화
    public void deactivateByDiaryId(Long diaryId) {
        diaryExchangeRepository.deactivateByDiaryId(diaryId);
    }

    // 같은 날짜에 아직 교환되지 않은 다른 사용자의 일기를 찾아 양쪽 사용자에게 교환 정보를 생성
    @Transactional
    public DiaryExchangeResponse createExchangeIfPossible(DiaryEntity myDiary) {
        if (diaryExchangeRepository.existsByMyDiaryIdAndActiveTrue(myDiary.getId())) {
            return diaryExchangeRepository.findByOwnerUserIdAndMyDiaryIdAndActiveTrue(
                            myDiary.getUser().getId(),
                            myDiary.getId()
                    )
                    .map(DiaryExchangeResponse::from)
                    .orElse(null);
        }

        List<DiaryEntity> candidates = diaryRepository.findMatchCandidates(
                myDiary.getUser().getId(),
                myDiary.getDiaryDate()
        );

        if (candidates.isEmpty()) {
            return null;
        }

        DiaryEntity partnerDiary = candidates.get(0);
        DiaryExchangeEntity myExchange = DiaryExchangeEntity.create(
                myDiary.getUser(),
                partnerDiary.getUser(),
                myDiary,
                partnerDiary,
                myDiary.getDiaryDate()
        );
        DiaryExchangeEntity partnerExchange = DiaryExchangeEntity.create(
                partnerDiary.getUser(),
                myDiary.getUser(),
                partnerDiary,
                myDiary,
                myDiary.getDiaryDate()
        );

        diaryExchangeRepository.save(partnerExchange);
        diaryExchangeRepository.save(myExchange);
        matchNotificationRepository.save(MatchNotificationEntity.create(myDiary.getUser(), myExchange));
        matchNotificationRepository.save(MatchNotificationEntity.create(partnerDiary.getUser(), partnerExchange));
        return DiaryExchangeResponse.from(myExchange);
    }
}
