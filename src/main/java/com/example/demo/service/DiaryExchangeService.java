package com.example.demo.service;

import com.example.demo.dto.request.DiaryCreateRequest;
import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.dto.response.BlockedUserResponse;
import com.example.demo.entity.*;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryExchangeService {
    private static final int MAX_PENDING_RECEIVED = 1;
    private final DiaryExchangeRequestRepository exchangeRepository;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;

    // 발신 일기를 랜덤한 상대에게 전달하고 24시간 답장 기한을 부여
    @Transactional
    public DiaryExchangeResponse createExchangeIfPossible(DiaryEntity diary) {
        // 같은 작성자의 동시 재매칭 요청이 여러 상대에게 전달되지 않도록 잠금
        userRepository.findForUpdate(diary.getUser().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (exchangeRepository.existsBySenderDiaryIdAndStatus(diary.getId(), ExchangeStatus.PENDING)
                || exchangeRepository.existsBySenderDiaryIdAndStatus(diary.getId(), ExchangeStatus.COMPLETED)
                || exchangeRepository.existsByReplyDiaryIdAndStatus(diary.getId(), ExchangeStatus.COMPLETED)) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 교환 중인 일기입니다.");
        }
        List<Long> previouslyReceived = exchangeRepository.findAllBySenderDiaryIdAndStatus(diary.getId(), ExchangeStatus.REJECTED)
                .stream().map(e -> e.getRecipientUser().getId()).toList();
        List<Long> expiredRecipients = exchangeRepository.findAllBySenderDiaryIdAndStatus(diary.getId(), ExchangeStatus.EXPIRED)
                .stream().map(e -> e.getRecipientUser().getId()).toList();
        List<UserEntity> candidates = userRepository.findAllByDelYn("N").stream()
                .filter(user -> !user.getId().equals(diary.getUser().getId()))
                .filter(user -> !previouslyReceived.contains(user.getId()) && !expiredRecipients.contains(user.getId()))
                .filter(user -> !blockedUserRepository.existsByOwnerUserIdAndBlockedUserId(diary.getUser().getId(), user.getId()))
                .filter(user -> !blockedUserRepository.existsByOwnerUserIdAndBlockedUserId(user.getId(), diary.getUser().getId()))
                .toList();
        candidates = new java.util.ArrayList<>(candidates);
        Collections.shuffle(candidates);
        for (UserEntity candidate : candidates) {
            UserEntity locked = userRepository.findForUpdate(candidate.getId()).orElseThrow();
            if ("Y".equals(locked.getDelYn())) continue;
            if (exchangeRepository.countByRecipientUserIdAndStatusAndExpiresAtAfter(
                    locked.getId(), ExchangeStatus.PENDING, LocalDateTime.now()) >= MAX_PENDING_RECEIVED) continue;
            DiaryExchangeRequestEntity request = exchangeRepository.save(DiaryExchangeRequestEntity.create(
                    diary.getUser(), locked, diary, LocalDateTime.now().plusHours(24)));
            return DiaryExchangeResponse.from(request, diary.getUser().getId());
        }
        return null;
    }

    // 교환할 상대가 없었거나 이전 요청이 끝난 일기를 다시 전달
    @Transactional
    public DiaryExchangeResponse matchDiary(Long userId, Long diaryId) {
        DiaryEntity diary = diaryRepository.findByIdAndUserIdAndDelYn(diaryId, userId, "N")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."));
        expireDueRequests();
        return createExchangeIfPossible(diary);
    }

    // 받은 요청은 발신자의 일기를 읽을 수 있고, 완료 후에는 양쪽 일기를 조회
    @Transactional
    public DiaryExchangeResponse findExchange(Long userId, Long diaryId) {
        return exchangeRepository.findAllBySenderUserIdOrRecipientUserIdOrderByIdDesc(userId, userId).stream()
                .filter(e -> e.getSenderDiary().getId().equals(diaryId)
                        || (e.getReplyDiary() != null && e.getReplyDiary().getId().equals(diaryId)))
                .findFirst().map(e -> {
                    expireIfDue(e);
                    return DiaryExchangeResponse.from(e, userId);
                }).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다."));
    }

    // 내가 보낸 요청과 받은 요청을 최신순으로 조회
    @Transactional
    public List<DiaryExchangeResponse> findActiveConnections(Long userId) {
        return exchangeRepository.findAllBySenderUserIdOrRecipientUserIdOrderByIdDesc(userId, userId).stream()
                .peek(this::expireIfDue).map(e -> DiaryExchangeResponse.from(e, userId)).toList();
    }

    // 수신자가 답장 일기를 작성하면 서로의 일기를 볼 수 있도록 완료 처리
    @Transactional
    public DiaryExchangeResponse reply(Long userId, Long exchangeId, DiaryCreateRequest body) {
        DiaryExchangeRequestEntity request = exchangeRepository.findForUpdate(exchangeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다."));
        requireRecipient(request, userId);
        expireIfDue(request);
        if (request.getStatus() != ExchangeStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "답장할 수 없는 교환 요청입니다.");
        }
        if ("Y".equals(request.getSenderDiary().getDelYn()) || "Y".equals(request.getSenderUser().getDelYn())) {
            request.cancel();
            throw new ApiException(HttpStatus.CONFLICT, "작성자의 일기를 확인할 수 없습니다.");
        }
        if (diaryRepository.existsByUserIdAndDiaryDateAndDelYn(userId, body.getDiaryDate(), "N")) {
            throw new ApiException(HttpStatus.CONFLICT, "해당 날짜에는 이미 일기를 작성했습니다.");
        }
        DiaryEntity reply = diaryRepository.save(DiaryEntity.create(request.getRecipientUser(), body.getDiaryDate(),
                body.getTitle(), body.getContent(), body.getMood()));
        request.complete(reply);
        return DiaryExchangeResponse.from(request, userId);
    }

    // 수신자가 원하지 않는 요청을 거절
    @Transactional
    public DiaryExchangeResponse reject(Long userId, Long exchangeId) {
        DiaryExchangeRequestEntity request = exchangeRepository.findForUpdate(exchangeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다."));
        requireRecipient(request, userId);
        expireIfDue(request);
        if (request.getStatus() != ExchangeStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "거절할 수 없는 교환 요청입니다.");
        }
        request.reject();
        return DiaryExchangeResponse.from(request, userId);
    }

    // 받은 요청 중 답장할 수 있는 요청을 조회
    @Transactional
    public List<DiaryExchangeResponse> findInbox(Long userId) {
        return exchangeRepository.findAllByRecipientUserIdAndStatusOrderByIdDesc(userId, ExchangeStatus.PENDING).stream()
                .peek(this::expireIfDue)
                .peek(e -> {
                    if (e.getStatus() == ExchangeStatus.PENDING &&
                            ("Y".equals(e.getSenderUser().getDelYn()) || "Y".equals(e.getSenderDiary().getDelYn()))) {
                        e.cancel();
                    }
                })
                .filter(e -> e.getStatus() == ExchangeStatus.PENDING)
                .map(e -> DiaryExchangeResponse.from(e, userId)).toList();
    }

    // 연결 종료 시 완료된 교환은 기록으로 두고 대기 중인 요청만 취소
    @Transactional
    public void disconnect(Long userId, Long exchangeId) {
        DiaryExchangeRequestEntity request = exchangeRepository.findForUpdate(exchangeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다."));
        if (!request.getSenderUser().getId().equals(userId) && !request.getRecipientUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다.");
        }
        expireIfDue(request);
        if (request.getStatus() != ExchangeStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "종료할 수 없는 교환 요청입니다.");
        }
        request.cancel();
    }

    // 일기가 삭제되면 해당 일기와 연결된 교환 요청도 취소
    @Transactional
    public void deactivateByDiaryId(Long diaryId) {
        exchangeRepository.findAllBySenderDiaryIdOrReplyDiaryId(diaryId, diaryId).stream()
                .forEach(DiaryExchangeRequestEntity::cancel);
    }

    // 답장 기한이 지난 요청을 주기적으로 만료 처리
    @Scheduled(fixedDelay = 60000) // 스케쥴링으로 1분 간격으로 기한이 지난 pending 요청을 확인
    @Transactional
    public void expireDueRequests() {
        exchangeRepository.findAllByStatusAndExpiresAtLessThanEqual(ExchangeStatus.PENDING, LocalDateTime.now())
                .forEach(DiaryExchangeRequestEntity::expire);
    }

    private void expireIfDue(DiaryExchangeRequestEntity request) {
        if (request.getStatus() == ExchangeStatus.PENDING && !LocalDateTime.now().isBefore(request.getExpiresAt())) {
            request.expire();
        }
    }

    private void requireRecipient(DiaryExchangeRequestEntity request, Long userId) {
        if (!request.getRecipientUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "교환 요청을 찾을 수 없습니다.");
        }
    }

    // 차단한 상대와의 대기 요청을 취소하고 다음 교환 후보에서 제외
    @Transactional
    public void blockUser(Long userId, Long blockedUserId) {
        if (userId.equals(blockedUserId)) throw new ApiException(HttpStatus.BAD_REQUEST, "본인은 차단할 수 없습니다.");
        UserEntity owner = userRepository.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        UserEntity blocked = userRepository.findById(blockedUserId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (!blockedUserRepository.existsByOwnerUserIdAndBlockedUserId(userId, blockedUserId)) {
            blockedUserRepository.save(BlockedUserEntity.create(owner, blocked));
        }
        exchangeRepository.findAllBySenderUserIdOrRecipientUserIdOrderByIdDesc(userId, userId).stream()
                .filter(e -> e.getStatus() == ExchangeStatus.PENDING)
                .filter(e -> e.getSenderUser().getId().equals(blockedUserId)
                        || e.getRecipientUser().getId().equals(blockedUserId))
                .forEach(DiaryExchangeRequestEntity::cancel);
    }

    @Transactional
    // 차단 해제
    public void unblockUser(Long userId, Long blockedUserId) {
        blockedUserRepository.findByOwnerUserIdAndBlockedUserId(userId, blockedUserId)
                .ifPresent(blockedUserRepository::delete);
    }

    // 내가 차단한 사용자를 최신순으로 조ㅎㅚ
    public List<BlockedUserResponse> findBlockedUsers(Long userId) {
        return blockedUserRepository.findAllByOwnerUserIdOrderByIdDesc(userId).stream()
                .map(BlockedUserResponse::from).toList();
    }
}
