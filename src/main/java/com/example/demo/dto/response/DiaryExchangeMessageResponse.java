package com.example.demo.dto.response;

import com.example.demo.entity.DiaryExchangeMessageEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryExchangeMessageResponse(Long id, Long authorUserId, LocalDate diaryDate,
                                           String title, String content, String mood, LocalDateTime createdAt) {
    // 교환 중 작성한 답장 일기를 응답으로 변환
    public static DiaryExchangeMessageResponse from(DiaryExchangeMessageEntity message) {
        return new DiaryExchangeMessageResponse(message.getId(), message.getAuthorUser().getId(),
                message.getDiaryDate(), message.getTitle(), message.getContent(), message.getMood(),
                message.getCreatedAt());
    }
}
