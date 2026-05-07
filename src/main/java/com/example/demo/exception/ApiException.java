package com.example.demo.exception;

import org.springframework.http.HttpStatus;

// 도메인 로직에서 HTTP 상태 코드와 메시지를 함께 전달하기 위한 예외
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
