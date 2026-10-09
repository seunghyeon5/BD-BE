package com.example.demo.service;

import com.example.demo.dto.request.DiaryCreateRequest;
import com.example.demo.dto.request.DiaryUpdateRequest;
import com.example.demo.dto.response.DiaryCreateResponse;
import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.dto.response.DiaryResponse;
import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserService userService;
    private final DiaryExchangeService diaryExchangeService;

    private static final String NOT_DELETED = "N";

    // 일기를 저장하고, 같은 날짜에 교환 가능한 상대 일기가 있으면 교환 정보를 생성
    @Transactional
    public DiaryCreateResponse create(Long userId, DiaryCreateRequest request) {
        UserEntity user = userService.findUser(userId);

        LocalDate diaryDate = request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now();
        if (diaryRepository.existsByUserIdAndDiaryDateAndDelYn(userId, diaryDate, NOT_DELETED)) {
            throw new ApiException(HttpStatus.CONFLICT, "해당 날짜에는 이미 일기를 작성했습니다.");
        }

        DiaryEntity diary = DiaryEntity.create(
                user,
                diaryDate,
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
        return diaryRepository.findAllByUserIdAndDelYnOrderByDiaryDateDescCreatedAtDesc(userId, NOT_DELETED)
                .stream()
                .map(DiaryResponse::from)
                .toList();
    }

    @Transactional
    // 저장된 일기의 작성자만 제목, 내용, 기분을 수정
    public DiaryResponse update(Long userId, Long diaryId, DiaryUpdateRequest request) {
        DiaryEntity diary = findActiveDiary(userId, diaryId);
        diary.update(request.getTitle(), request.getContent(), request.getMood());
        return DiaryResponse.from(diary);
    }

    @Transactional
    // 일기 삭제 시 실제 삭제 대신 삭제 상태로 변경하고 연결된 교환도 비활성화
    public void delete(Long userId, Long diaryId) {
        DiaryEntity diary = findActiveDiary(userId, diaryId);
        diary.delete();
        diaryExchangeService.deactivateByDiaryId(diaryId);
    }

    private DiaryEntity findActiveDiary(Long userId, Long diaryId) {
        return diaryRepository.findByIdAndUserIdAndDelYn(diaryId, userId, NOT_DELETED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다."));
    }
}
