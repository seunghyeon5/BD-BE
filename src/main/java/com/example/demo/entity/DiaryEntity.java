package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "diaries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_diary_user_date",
                columnNames = {"user_id", "diary_date"}
        )
)
public class DiaryEntity extends BaseTimeEntity {
    // 사용자가 하루에 하나씩 작성하는 일기 정보
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate diaryDate;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 30)
    private String mood;

    // 일기 등록 시 DiaryEntity를 만들기 위한 생성 메서드
    public static DiaryEntity create(UserEntity user, LocalDate diaryDate ,String title, String content, String mood) {
        DiaryEntity diary = new DiaryEntity();
        diary.user = user;
        diary.diaryDate = (diaryDate != null) ? diaryDate : LocalDate.now();;
        diary.title = title;
        diary.content = content;
        diary.mood = mood;
        return diary;
    }
}
