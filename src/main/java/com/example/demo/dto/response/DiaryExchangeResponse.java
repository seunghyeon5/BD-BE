package com.example.demo.dto.response;

import com.example.demo.entity.DiaryExchangeRequestEntity;
import com.example.demo.entity.ExchangeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DiaryExchangeResponse {
    private Long exchangeId;
    private LocalDate exchangeDate;
    private Long senderUserId;
    private Long recipientUserId;
    private LocalDateTime expiresAt;
    private Long nextReplyUserId;
    private List<DiaryExchangeMessageResponse> messages;
    private DiaryResponse myDiary;
    private DiaryResponse partnerDiary;
    private boolean active;
    private ExchangeStatus status;

    // 요청을 보는 사용자 기준으로 내 일기와 상대 일기를 구분
    public static DiaryExchangeResponse from(DiaryExchangeRequestEntity exchange, Long viewerId) {
        boolean sender = exchange.getSenderUser().getId().equals(viewerId);
        boolean active = exchange.getStatus() == ExchangeStatus.PENDING || exchange.getStatus() == ExchangeStatus.COMPLETED;
        DiaryResponse sent = "Y".equals(exchange.getSenderDiary().getDelYn())
                ? null : DiaryResponse.from(exchange.getSenderDiary());
        DiaryResponse reply = exchange.getReplyDiary() == null || "Y".equals(exchange.getReplyDiary().getDelYn())
                ? null : DiaryResponse.from(exchange.getReplyDiary());
        return DiaryExchangeResponse.builder()
                .exchangeId(exchange.getId())
                .exchangeDate(exchange.getSenderDiary().getDiaryDate())
                .senderUserId(exchange.getSenderUser().getId())
                .recipientUserId(exchange.getRecipientUser().getId())
                .expiresAt(exchange.getExpiresAt())
                .nextReplyUserId(active ? exchange.nextReplyUserId() : null)
                .messages(active ? exchange.getMessages().stream().map(DiaryExchangeMessageResponse::from).toList() : List.of())
                .myDiary(sender ? sent : reply)
                .partnerDiary(active ? (sender ? reply : sent) : null)
                .active(active)
                .status(exchange.getStatus())
                .build();
    }
}
