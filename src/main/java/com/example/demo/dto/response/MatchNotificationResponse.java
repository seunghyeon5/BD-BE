package com.example.demo.dto.response;

import com.example.demo.entity.MatchNotificationEntity;
import java.time.LocalDateTime;

public record MatchNotificationResponse(Long id, Long exchangeId, Long diaryId, LocalDateTime createdAt) {
    public static MatchNotificationResponse from(MatchNotificationEntity notification) {
        return new MatchNotificationResponse(notification.getId(), notification.getExchange().getId(),
                notification.getExchange().getMyDiary().getId(), notification.getCreatedAt());
    }
}
