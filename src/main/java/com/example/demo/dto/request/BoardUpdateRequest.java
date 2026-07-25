package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardUpdateRequest {
    // 게시글 수정 요청 본문
    @NotBlank
    private String boardTitle;

    @NotBlank
    private String boardContents;

    @NotBlank
    private String boardPass;
}
