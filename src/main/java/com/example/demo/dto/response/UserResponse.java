package com.example.demo.dto.response;

import com.example.demo.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
    private String delYn;
    private String gender;
    private String phoneNum;

    public static UserResponse from(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .delYn(user.getDelYn())
                .gender(user.getGender())
                .phoneNum(user.getPhoneNum())
                .build();
    }
}
