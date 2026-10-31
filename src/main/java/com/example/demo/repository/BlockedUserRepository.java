package com.example.demo.repository;

import com.example.demo.entity.BlockedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUserEntity, Long> {
    boolean existsByOwnerUserIdAndBlockedUserId(Long ownerUserId, Long blockedUserId);
    Optional<BlockedUserEntity> findByOwnerUserIdAndBlockedUserId(Long ownerUserId, Long blockedUserId);
    List<BlockedUserEntity> findAllByOwnerUserId(Long ownerUserId);
}
