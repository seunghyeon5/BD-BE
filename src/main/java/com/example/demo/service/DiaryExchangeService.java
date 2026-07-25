package com.example.demo.service;

import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.DiaryExchangeEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.DiaryExchangeRepository;
import com.example.demo.repository.DiaryRepository;
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

    // 사용자가 요청한 일기를 기준으로 매칭을 시도하고, 이미 매칭되어 있으면 기존 교환 정보를 반환
    @Transactional
    public DiaryExchangeResponse matchDiary(Long userId, Long diaryId) {
        DiaryEntity myDiary = diaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."));

        return diaryExchangeRepository.findByOwnerUserIdAndMyDiaryIdAndActiveTrue(userId, diaryId)
                .map(DiaryExchangeResponse::from)
                .orElseGet(() -> createExchangeIfPossible(myDiary));
    }

    // 내 일기와 연결된 활성 교환 정보를 조회
    public DiaryExchangeResponse findExchange(Long userId, Long diaryId) {
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
        exchange.deactivate();
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
        return DiaryExchangeResponse.from(diaryExchangeRepository.save(myExchange));
    }
}
