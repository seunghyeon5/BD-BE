package com.example.demo.repository;

import com.example.demo.entity.MatchNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MatchNotificationRepository extends JpaRepository<MatchNotificationEntity, Long> {
    List<MatchNotificationEntity> findAllByUserIdAndReadYnFalseOrderByCreatedAtDesc(Long userId);
    Optional<MatchNotificationEntity> findByIdAndUserId(Long id, Long userId);
}
