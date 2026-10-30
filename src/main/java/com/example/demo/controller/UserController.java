package com.example.demo.controller;

import com.example.demo.dto.request.UserLoginRequest;
import com.example.demo.dto.request.UserSignupRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserDuplicateCheckResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    // 새 사용자를 가입시키는 API
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/me")
                .build()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // 이메일과 비밀번호로 사용자를 확인하는 로그인 API
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }

    // 이메일 중복 여부를 확인 API
    @GetMapping("/exist-email")
    public ResponseEntity<UserDuplicateCheckResponse> checkEmailDuplicate(
            @Email @NotBlank @RequestParam String email
    ) {
        return ResponseEntity.ok(userService.checkEmailDuplicate(email));
    }

    // 닉네임 중복 여부를 확인 API
    @GetMapping("/exist-nickname")
    public ResponseEntity<UserDuplicateCheckResponse> checkNicknameDuplicate(
            @NotBlank @RequestParam String nickname
    ) {
        return ResponseEntity.ok(userService.checkNicknameDuplicate(nickname));
    }

    // 마이페이지에서 사용할 내 정보 조회 API
    @GetMapping("/me")
    public ResponseEntity<UserResponse> myPage(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(userService.findMyPage(loginUser.getUserId()));
    }

    //사용자 회원탈퇴 API
    @PostMapping("/me/withdraw")
    public ResponseEntity<UserResponse> delete(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(userService.withdrawUser(loginUser.getUserId()));
    }
}
