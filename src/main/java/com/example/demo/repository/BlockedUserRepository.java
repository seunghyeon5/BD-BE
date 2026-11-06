package com.example.demo.repository;

import com.example.demo.entity.BlockedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUserEntity, Long> {
    boolean existsByOwnerUserIdAndBlockedUserId(Long ownerUserId, Long blockedUserId);
    Optional<BlockedUserEntity> findByOwnerUserIdAndBlockedUserId(Long ownerUserId, Long blockedUserId);

    // 차단한 사용자 정보를 함께 가져와 최신 차단순으로 조회
    @EntityGraph(attributePaths = "blockedUser")
    List<BlockedUserEntity> findAllByOwnerUserIdOrderByIdDesc(Long ownerUserId);
}
