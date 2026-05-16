package com.example.demo.dto;

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
                .message(exchange == null ? "일기가 저장되었습니다. 아직 교환 가능한 상대가 없습니다." : "일기가 저장되고 교환되었습니다.")
                .build();
    }
}
