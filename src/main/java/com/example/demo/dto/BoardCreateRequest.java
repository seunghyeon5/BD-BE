package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCreateRequest {
    // 게시글 등록 요청 본문
    @NotBlank
    private String boardTitle;

    @NotBlank
    private String boardWriter;

    @NotBlank
    private String boardPass;

    @NotBlank
    private String boardContents;
}
