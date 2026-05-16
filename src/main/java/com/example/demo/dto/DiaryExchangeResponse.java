package com.example.demo.dto;

import com.example.demo.entity.DiaryExchangeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DiaryExchangeResponse {
    private Long exchangeId;
    private LocalDate exchangeDate;
    private DiaryResponse myDiary;
    private DiaryResponse partnerDiary;
    private boolean active;

    public static DiaryExchangeResponse from(DiaryExchangeEntity exchange) {
        return DiaryExchangeResponse.builder()
                .exchangeId(exchange.getId())
                .exchangeDate(exchange.getExchangeDate())
                .myDiary(DiaryResponse.from(exchange.getMyDiary()))
                .partnerDiary(DiaryResponse.from(exchange.getPartnerDiary()))
                .active(exchange.isActive())
                .build();
    }
}
