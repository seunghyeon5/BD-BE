package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiaryCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private LocalDate diaryDate;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String mood;
}
