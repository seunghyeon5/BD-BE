package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_notifications")
public class MatchNotificationEntity extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private DiaryExchangeEntity exchange;

    @Column(nullable = false)
    private boolean readYn;

    // 새 교환이 생기면 사용자별 알림을 생성
    public static MatchNotificationEntity create(UserEntity user, DiaryExchangeEntity exchange) {
        MatchNotificationEntity notification = new MatchNotificationEntity();
        notification.user = user;
        notification.exchange = exchange;
        return notification;
    }

    // 확인한 교환 알림을 읽음 상태로 변경
    public void markRead() { this.readYn = true; }
}
