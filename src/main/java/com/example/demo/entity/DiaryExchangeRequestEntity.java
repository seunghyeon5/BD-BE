package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_reply_user_id")
    private UserEntity nextReplyUser;

    @OneToMany(mappedBy = "exchange")
    @OrderBy("id ASC")
    private List<DiaryExchangeMessageEntity> messages = new ArrayList<>();

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
        exchange.nextReplyUser = recipient;
        return exchange;
    }

    // 답장을 쓰면 다음 사람에게 차례를 넘기고 24시간 기한을 다시 설정
    public void completeTurn(UserEntity nextReplyUser, LocalDateTime expiresAt) {
        this.nextReplyUser = nextReplyUser;
        this.expiresAt = expiresAt;
        this.status = ExchangeStatus.COMPLETED;
    }

    // 저장한 답장을 교환 기록에 추가
    public void addMessage(DiaryExchangeMessageEntity message) {
        this.messages.add(message);
    }

    // 이전 교환 데이터도 다음 답장 차례를 확인할 수 있도록 사용자 ID를 반환
    public Long nextReplyUserId() {
        if (nextReplyUser != null) return nextReplyUser.getId();
        return status == ExchangeStatus.PENDING ? recipientUser.getId() : senderUser.getId();
    }

    // 이전 방식으로 완료된 교환은 첫 답장을 쓴 시각부터 A의 기한을 다시 계산
    public void initializeNextTurnForLegacy() {
        if (status == ExchangeStatus.COMPLETED && nextReplyUser == null && replyDiary != null) {
            nextReplyUser = senderUser;
            if (getUpdatedAt() != null) expiresAt = getUpdatedAt().plusHours(24);
        }
    }

    // 수신자가 교환을 원하지 않으면 요청을 거절
    public void reject() { this.status = ExchangeStatus.REJECTED; }

    // 답장 기한이 지나면 요청을 만료 처리
    public void expire() { this.status = ExchangeStatus.EXPIRED; }

    // 작성자가 일기를 삭제하거나 연결을 종료하면 요청을 취소
    public void cancel() { this.status = ExchangeStatus.CANCELLED; }
}
