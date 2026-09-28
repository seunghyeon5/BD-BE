package com.example.demo.repository;

import com.example.demo.entity.DiaryExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryExchangeRepository extends JpaRepository<DiaryExchangeEntity, Long> {
    Optional<DiaryExchangeEntity> findByOwnerUserIdAndMyDiaryIdAndActiveTrue(Long ownerUserId, Long myDiaryId);

    boolean existsByMyDiaryIdAndActiveTrue(Long myDiaryId);

    List<DiaryExchangeEntity> findAllByOwnerUserIdAndActiveTrueOrderByExchangeDateDescCreatedAtDesc(Long ownerUserId);

    Optional<DiaryExchangeEntity> findByIdAndOwnerUserId(Long id, Long ownerUserId);

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
