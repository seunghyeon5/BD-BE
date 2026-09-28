package com.example.demo.repository;

import com.example.demo.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {
    boolean existsByUserIdAndDiaryDateAndDelYn(Long userId, LocalDate diaryDate, String delYn);

    Optional<DiaryEntity> findByIdAndUserIdAndDelYn(Long id, Long userId, String delYn);

    List<DiaryEntity> findAllByUserIdAndDelYnOrderByDiaryDateDescCreatedAtDesc(Long userId, String delYn);

    @Query("""
            select d
            from DiaryEntity d
            where d.diaryDate = :diaryDate
              and d.user.id <> :userId
              and d.delYn = 'N'
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
