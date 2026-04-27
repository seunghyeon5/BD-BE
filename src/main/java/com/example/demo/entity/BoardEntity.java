package com.example.demo.entity;

import com.example.demo.dto.BoardDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "board_table")
public class BoardEntity extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column //board 테이블 열 정의
    private String boardWriter;

    @Column
    private String boardPass;

    @Column
    private String boardTitle;

    @Column
    private String boardContents;

    @Column
    private int boardHits;

    public static BoardEntity toSaveEntity(BoardDto boardDto) {
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.boardWriter = boardDto.getBoardWriter();
        boardEntity.boardPass = boardDto.getBoardPass();
        boardEntity.boardTitle = boardDto.getBoardTitle();
        boardEntity.boardContents = boardDto.getBoardContents();
        boardEntity.boardHits = 0;
        return boardEntity;
    }

    public void update(BoardDto boardDto) {
        this.boardTitle = boardDto.getBoardTitle();
        this.boardContents = boardDto.getBoardContents();
    }
}
