package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryUpdateRequest {
    // 일기 수정 요청 본문
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String mood;
}
