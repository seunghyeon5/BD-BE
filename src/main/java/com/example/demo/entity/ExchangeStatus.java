package com.example.demo.entity;

public enum ExchangeStatus {
    PENDING,    // 상대방 응답 대기
    COMPLETED,  // 첫 답장 이후 교환 진행 중
    REJECTED,   // 상대방 거절
    EXPIRED,    // 답장 기한 만료
    CANCELLED   // 일기 삭제 또는 연결 종료
}
