package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary_exchange_requests")
public class DiaryExchangeRequestEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private UserEntity recipientUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_diary_id", nullable = false)
    private DiaryEntity senderDiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_diary_id")
    private DiaryEntity replyDiary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 일기를 받은 상대가 24시간 안에 답할 수 있도록 교환 요청 생성
    public static DiaryExchangeRequestEntity create(UserEntity sender, UserEntity recipient,
                                                     DiaryEntity senderDiary, LocalDateTime expiresAt) {
        DiaryExchangeRequestEntity exchange = new DiaryExchangeRequestEntity();
        exchange.senderUser = sender;
        exchange.recipientUser = recipient;
        exchange.senderDiary = senderDiary;
        exchange.expiresAt = expiresAt;
        exchange.status = ExchangeStatus.PENDING;
        return exchange;
    }

    // 수신자가 답장 일기를 쓰면 교환을 완료
    public void complete(DiaryEntity replyDiary) {
        this.replyDiary = replyDiary;
        this.status = ExchangeStatus.COMPLETED;
    }

    // 수신자가 교환을 원하지 않으면 요청을 거절
    public void reject() { this.status = ExchangeStatus.REJECTED; }

    // 답장 기한이 지나면 요청을 만료 처리
    public void expire() { this.status = ExchangeStatus.EXPIRED; }

    // 작성자가 일기를 삭제하거나 연결을 종료하면 요청을 취소
    public void cancel() { this.status = ExchangeStatus.CANCELLED; }
}
