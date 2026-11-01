package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryCreateResponse {
    private DiaryResponse diary;
    private DiaryExchangeResponse exchange;
    private String message;

    public static DiaryCreateResponse of(DiaryResponse diary, DiaryExchangeResponse exchange) {
        return DiaryCreateResponse.builder()
                .diary(diary)
                .exchange(exchange)
                .message(exchange == null ? "일기가 저장되었습니다. 현재 전달 가능한 상대가 없습니다." : "일기가 상대에게 전달되었습니다. 답장을 기다리는 중입니다.")
                .build();
    }
}
