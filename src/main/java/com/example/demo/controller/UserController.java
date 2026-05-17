package com.example.demo.controller;

import com.example.demo.dto.UserLoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import com.example.demo.exception.ApiException;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    // 새 사용자를 가입시키는 API
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/users/{userId}/me")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // 이메일과 비밀번호로 사용자를 확인하는 로그인 API
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {

        System.out.println("test");

        return ResponseEntity.ok(userService.login(request));
    }

    // 마이페이지에서 사용할 내 정보 조회 API
    @GetMapping("/{id}/me")
    public ResponseEntity<UserResponse> myPage(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findMyPage(id));
    }

    //사용자 회원탈퇴 API
    @PostMapping("/{id}/me/withdraw")
    public ResponseEntity<UserResponse> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails loginUser) {

        System.out.println("데이터 : " + loginUser.getUserId());
        if (!loginUser.getUserId().equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "본인의 계정만 탈퇴할 수 있습니다.");
        }

        return ResponseEntity.ok(userService.withdrawUser(id));
    }
}
