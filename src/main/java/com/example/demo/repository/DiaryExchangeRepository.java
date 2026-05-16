package com.example.demo.repository;

import com.example.demo.entity.DiaryExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryExchangeRepository extends JpaRepository<DiaryExchangeEntity, Long> {
    Optional<DiaryExchangeEntity> findByOwnerUserIdAndMyDiaryIdAndActiveTrue(Long ownerUserId, Long myDiaryId);

    boolean existsByMyDiaryIdAndActiveTrue(Long myDiaryId);

    List<DiaryExchangeEntity> findAllByOwnerUserIdAndActiveTrueOrderByExchangeDateDescCreatedAtDesc(Long ownerUserId);

    Optional<DiaryExchangeEntity> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
