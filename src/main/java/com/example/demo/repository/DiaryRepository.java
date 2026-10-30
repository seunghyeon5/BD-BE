package com.example.demo.repository;

import com.example.demo.entity.DiaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {
    boolean existsByUserIdAndDiaryDateAndDelYn(Long userId, LocalDate diaryDate, String delYn);

    Optional<DiaryEntity> findByIdAndUserIdAndDelYn(Long id, Long userId, String delYn);

    List<DiaryEntity> findAllByUserIdAndDelYnOrderByDiaryDateDescCreatedAtDesc(Long userId, String delYn);

    // 사용자의 활성 일기를 날짜와 기분 조건으로 나누어 조회
    @Query("""
            select d from DiaryEntity d
            where d.user.id = :userId and d.delYn = 'N'
              and (:fromDate is null or d.diaryDate >= :fromDate)
              and (:toDate is null or d.diaryDate <= :toDate)
              and (:mood is null or d.mood = :mood)
            """)
    Page<DiaryEntity> findHistory(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate, @Param("mood") String mood, Pageable pageable);

    @Query("""
            select d
            from DiaryEntity d
            where d.diaryDate = :diaryDate
              and d.user.id <> :userId
              and d.delYn = 'N'
              and d.user.delYn = 'N'
              and not exists (select b.id from BlockedUserEntity b
                  where (b.ownerUser.id = :userId and b.blockedUser = d.user)
                     or (b.blockedUser.id = :userId and b.ownerUser = d.user))
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
