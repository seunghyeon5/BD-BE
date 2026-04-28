package com.example.demo.exception;

public class InvalidBoardPasswordException extends RuntimeException {
    public InvalidBoardPasswordException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
