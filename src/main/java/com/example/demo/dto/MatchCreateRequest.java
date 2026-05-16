package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long diaryId;
}
