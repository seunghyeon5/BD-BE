package com.example.demo.dto.response;

import com.example.demo.entity.DiaryEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DiaryResponse {
    private Long id;
    private Long userId;
    private LocalDate diaryDate;
    private String title;
    private String content;
    private String mood;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DiaryResponse from(DiaryEntity diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .userId(diary.getUser().getId())
                .diaryDate(diary.getDiaryDate())
                .title(diary.getTitle())
                .content(diary.getContent())
                .mood(diary.getMood())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }
}
