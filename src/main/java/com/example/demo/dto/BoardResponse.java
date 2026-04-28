package com.example.demo.dto;

import com.example.demo.entity.BoardEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardResponse {
    // 클라이언트에 내려주는 게시글 응답 DTO
    private Long id;
    private String boardTitle;
    private String boardWriter;
    private String boardContents;
    private int boardHits;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse from(BoardEntity boardEntity) {
        return BoardResponse.builder()
                .id(boardEntity.getId())
                .boardTitle(boardEntity.getBoardTitle())
                .boardWriter(boardEntity.getBoardWriter())
                .boardContents(boardEntity.getBoardContents())
                .boardHits(boardEntity.getBoardHits())
                .createdAt(boardEntity.getCreatedAt())
                .updatedAt(boardEntity.getUpdatedAt())
                .build();
    }
}
