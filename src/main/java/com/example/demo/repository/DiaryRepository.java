package com.example.demo.repository;

import com.example.demo.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {
    boolean existsByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);

    Optional<DiaryEntity> findByIdAndUserId(Long id, Long userId);

    List<DiaryEntity> findAllByUserIdOrderByDiaryDateDescCreatedAtDesc(Long userId);

    @Query("""
            select d
            from DiaryEntity d
            where d.diaryDate = :diaryDate
              and d.user.id <> :userId
              and not exists (
                  select e.id
                  from DiaryExchangeEntity e
                  where e.active = true
                    and (e.myDiary = d or e.partnerDiary = d)
              )
            order by d.createdAt asc
            """)
    List<DiaryEntity> findMatchCandidates(@Param("userId") Long userId, @Param("diaryDate") LocalDate diaryDate);
}
