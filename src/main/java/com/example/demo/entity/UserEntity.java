package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class UserEntity extends BaseTimeEntity {
    // 서비스 사용자의 기본 계정 정보
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String delYn;

    @Column(nullable = false, length = 30)
    private String gender;

    @Column(nullable = false, length = 11)
    private String phoneNum;

    // 회원가입 시 UserEntity를 만들기 위한 생성 메서드
    public static UserEntity create(String email, String password, String nickname, String gender, String phoneNum) {
        UserEntity user = new UserEntity();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        user.delYn = "N";
        user.gender = gender;
        user.phoneNum = phoneNum;
        return user;
    }

    // 회원탈퇴 메서드
    public void withdraw() {
        this.delYn = "Y";
    }
}
