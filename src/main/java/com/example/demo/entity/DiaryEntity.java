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

    // 일기 삭제 시 실제 데이터를 지우지 않고 삭제 여부를 표시하기 위한 컬럼
    @Column(name = "DEL_YN", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String delYn;

    // 일기 등록 시 DiaryEntity를 만들기 위한 생성 메서드
    public static DiaryEntity create(UserEntity user, LocalDate diaryDate ,String title, String content, String mood) {
        DiaryEntity diary = new DiaryEntity();
        diary.user = user;
        diary.diaryDate = (diaryDate != null) ? diaryDate : LocalDate.now();;
        diary.title = title;
        diary.content = content;
        diary.mood = mood;
        diary.delYn = "N";
        return diary;
    }

    // 수정 허용 항목만 변경
    public void update(String title, String content, String mood) {
        this.title = title;
        this.content = content;
        this.mood = mood;
    }

    // 일기 삭제 시 실제 삭제 대신 삭제 상태로 변경
    public void delete() {
        this.delYn = "Y";
    }
}
