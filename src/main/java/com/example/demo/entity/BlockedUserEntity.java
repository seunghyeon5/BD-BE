package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blocked_users", uniqueConstraints = @UniqueConstraint(columnNames = {"owner_user_id", "blocked_user_id"}))
public class BlockedUserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserEntity ownerUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private UserEntity blockedUser;

    // 사용자가 더 이상 교환하고 싶지 않은 상대를 등록
    public static BlockedUserEntity create(UserEntity ownerUser, UserEntity blockedUser) {
        BlockedUserEntity block = new BlockedUserEntity();
        block.ownerUser = ownerUser;
        block.blockedUser = blockedUser;
        return block;
    }
}
