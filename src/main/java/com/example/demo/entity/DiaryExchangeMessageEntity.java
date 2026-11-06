package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary_exchange_messages")
public class DiaryExchangeMessageEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private DiaryExchangeRequestEntity exchange;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private UserEntity authorUser;

    @Column(nullable = false)
    private LocalDate diaryDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 30)
    private String mood;

    // 교환 중 작성한 답장 일기를 별도로 저장
    public static DiaryExchangeMessageEntity create(DiaryExchangeRequestEntity exchange, UserEntity author,
            LocalDate diaryDate, String title, String content, String mood) {
        DiaryExchangeMessageEntity message = new DiaryExchangeMessageEntity();
        message.exchange = exchange;
        message.authorUser = author;
        message.diaryDate = diaryDate;
        message.title = title;
        message.content = content;
        message.mood = mood;
        return message;
    }
}
