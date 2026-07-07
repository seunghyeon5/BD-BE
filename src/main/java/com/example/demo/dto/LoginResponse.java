package com.example.demo.dto;

import com.example.demo.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String tokenType;
    private String token;

    private Long id;
    private String email;
    private String nickname;
    private String gender;
    private String phoneNum;

    public static LoginResponse of(String token, UserEntity user) {
        return LoginResponse.builder()
                .tokenType("Bearer")
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .phoneNum(user.getPhoneNum())
                .build();
    }
}