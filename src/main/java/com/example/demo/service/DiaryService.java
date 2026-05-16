package com.example.demo.service;

import com.example.demo.dto.DiaryCreateRequest;
import com.example.demo.dto.DiaryCreateResponse;
import com.example.demo.dto.DiaryExchangeResponse;
import com.example.demo.dto.DiaryResponse;
import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final DiaryExchangeService diaryExchangeService;

    // 일기를 저장하고, 같은 날짜에 교환 가능한 상대 일기가 있으면 교환 정보를 생성
    @Transactional
    public DiaryCreateResponse create(DiaryCreateRequest request) {
        UserEntity user = userService.findUser(request.getUserId());

        if (diaryRepository.existsByUserIdAndDiaryDate(request.getUserId(), request.getDiaryDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "해당 날짜에는 이미 일기를 작성했습니다.");
        }

        DiaryEntity diary = DiaryEntity.create(
                user,
                request.getDiaryDate(),
                request.getTitle(),
                request.getContent(),
                request.getMood()
        );
        DiaryEntity savedDiary = diaryRepository.save(diary);

        return DiaryCreateResponse.of(
                DiaryResponse.from(savedDiary),
                diaryExchangeService.createExchangeIfPossible(savedDiary)
        );
    }

    // 내가 작성한 일기 기준으로 교환된 상대 일기를 조회
    public DiaryExchangeResponse findExchangedDiary(Long userId, Long diaryId) {
        return diaryExchangeService.findExchange(userId, diaryId);
    }

    // 특정 사용자가 작성한 모든 일기를 최신순으로 조회
    public List<DiaryResponse> findHistory(Long userId) {
        userService.findUser(userId);
        return diaryRepository.findAllByUserIdOrderByDiaryDateDescCreatedAtDesc(userId)
                .stream()
                .map(DiaryResponse::from)
                .toList();
    }
}
