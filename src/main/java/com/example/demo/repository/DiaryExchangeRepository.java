package com.example.demo.repository;

import com.example.demo.entity.DiaryExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryExchangeRepository extends JpaRepository<DiaryExchangeEntity, Long> {
    @Query("""
            select e from DiaryExchangeEntity e
            where e.ownerUser.id = :ownerUserId and e.myDiary.id = :myDiaryId
              and e.active = true and e.myDiary.delYn = 'N' and e.partnerDiary.delYn = 'N'
              and e.ownerUser.delYn = 'N' and e.partnerUser.delYn = 'N'
            """)
    Optional<DiaryExchangeEntity> findByOwnerUserIdAndMyDiaryIdAndActiveTrue(
            @Param("ownerUserId") Long ownerUserId, @Param("myDiaryId") Long myDiaryId);

    boolean existsByMyDiaryIdAndActiveTrue(Long myDiaryId);

    @Query("""
            select e from DiaryExchangeEntity e
            where e.ownerUser.id = :ownerUserId and e.active = true
              and e.myDiary.delYn = 'N' and e.partnerDiary.delYn = 'N'
              and e.ownerUser.delYn = 'N' and e.partnerUser.delYn = 'N'
            order by e.exchangeDate desc, e.createdAt desc
            """)
    List<DiaryExchangeEntity> findAllByOwnerUserIdAndActiveTrueOrderByExchangeDateDescCreatedAtDesc(
            @Param("ownerUserId") Long ownerUserId);

    Optional<DiaryExchangeEntity> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    // 한쪽 사용자가 연결을 종료하면 양쪽 교환 정보도 함께 비활성화
    @Modifying
    @Query("""
            update DiaryExchangeEntity e set e.active = false
            where e.active = true and
              ((e.myDiary.id = :myDiaryId and e.partnerDiary.id = :partnerDiaryId)
               or (e.myDiary.id = :partnerDiaryId and e.partnerDiary.id = :myDiaryId))
            """)
    int deactivatePair(@Param("myDiaryId") Long myDiaryId, @Param("partnerDiaryId") Long partnerDiaryId);

    // 차단한 사용자와의 양쪽 연결을 종료
    @Modifying
    @Query("""
            update DiaryExchangeEntity e set e.active = false
            where e.active = true and
              ((e.ownerUser.id = :userId and e.partnerUser.id = :blockedUserId)
                or (e.ownerUser.id = :blockedUserId and e.partnerUser.id = :userId))
            """)
    int deactivateByUsers(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);

    // 삭제된 일기가 교환 정보에 계속 노출되지 않도록 연결을 비활성화
    @Modifying
    @Query("""
            update DiaryExchangeEntity e
            set e.active = false
            where e.active = true
              and (e.myDiary.id = :diaryId or e.partnerDiary.id = :diaryId)
            """)
    int deactivateByDiaryId(@Param("diaryId") Long diaryId);
}
