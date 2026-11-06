package com.example.demo.dto.response;

import com.example.demo.entity.BlockedUserEntity;

public record BlockedUserResponse(Long userId, String nickname) {
    // 차단 목록 화면에 필요한 사용자 ID와 닉네임만 응답으로 변환
    public static BlockedUserResponse from(BlockedUserEntity block) {
        return new BlockedUserResponse(block.getBlockedUser().getId(), block.getBlockedUser().getNickname());
    }
}
