package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "diary_exchanges")
public class DiaryExchangeEntity extends BaseTimeEntity {
    // 내 일기와 상대방 일기가 교환된 연결 정보
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity ownerUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_user_id", nullable = false)
    private UserEntity partnerUser;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "my_diary_id", nullable = false)
    private DiaryEntity myDiary;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_diary_id", nullable = false)
    private DiaryEntity partnerDiary;

    @Column(nullable = false)
    private LocalDate exchangeDate;

    @Column(nullable = false)
    private boolean active;

    // 한 사용자 관점에서 보이는 일기 교환 정보를 생성
    public static DiaryExchangeEntity create(
            UserEntity ownerUser,
            UserEntity partnerUser,
            DiaryEntity myDiary,
            DiaryEntity partnerDiary,
            LocalDate exchangeDate
    ) {
        DiaryExchangeEntity exchange = new DiaryExchangeEntity();
        exchange.ownerUser = ownerUser;
        exchange.partnerUser = partnerUser;
        exchange.myDiary = myDiary;
        exchange.partnerDiary = partnerDiary;
        exchange.exchangeDate = exchangeDate;
        exchange.active = true;
        return exchange;
    }

    // 연결 종료 시 실제 삭제 대신 비활성 상태로 변경
    public void deactivate() {
        this.active = false;
    }
}
