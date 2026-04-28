package com.example.demo.entity;

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

    // 게시글 최초 등록 시 사용하는 생성 메서드
    public static BoardEntity create(String boardWriter, String boardPass, String boardTitle, String boardContents) {
        BoardEntity boardEntity = new BoardEntity();
        boardEntity.boardWriter = boardWriter;
        boardEntity.boardPass = boardPass;
        boardEntity.boardTitle = boardTitle;
        boardEntity.boardContents = boardContents;
        boardEntity.boardHits = 0;
        return boardEntity;
    }

    // 수정 허용 항목만 변경
    public void update(String boardTitle, String boardContents) {
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
    }

    // 상세 조회 시 조회수 증가
    public void increaseHits() {
        this.boardHits += 1;
    }
}
