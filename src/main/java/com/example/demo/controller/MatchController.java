package com.example.demo.controller;

import com.example.demo.dto.request.MatchCreateRequest;
import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.dto.response.MatchNotificationResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DiaryExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchController {
    private final DiaryExchangeService diaryExchangeService;

    // 읽지 않은 교환 알림 목록을 조회하는 API
    @GetMapping("/notifications")
    public ResponseEntity<List<MatchNotificationResponse>> notifications(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.findNotifications(loginUser.getUserId()));
    }

    // 교환 알림을 읽음 상태로 변경하는 API
    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        diaryExchangeService.readNotification(loginUser.getUserId(), notificationId);
        return ResponseEntity.noContent().build();
    }

    // 특정 일기에 대해 교환 가능한 상대 일기를 찾아 매칭하는 API
    @PostMapping
    public ResponseEntity<DiaryExchangeResponse> match(
            @Valid @RequestBody MatchCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        return ResponseEntity.ok(diaryExchangeService.matchDiary(loginUser.getUserId(), request.getDiaryId()));
    }

    // 현재 유지 중인 일기 교환 연결 목록을 조회하는 API
    @GetMapping
    public ResponseEntity<List<DiaryExchangeResponse>> findConnections(
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        return ResponseEntity.ok(diaryExchangeService.findActiveConnections(loginUser.getUserId()));
    }

    // 더 이상 이어가지 않을 교환 연결을 종료하는 API
    @DeleteMapping("/{exchangeId}")
    public ResponseEntity<Void> disconnect(
            @PathVariable Long exchangeId,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        diaryExchangeService.disconnect(loginUser.getUserId(), exchangeId);
        return ResponseEntity.noContent().build();
    }

    // 상대를 차단하고 현재 교환 연결도 종료하는 API
    @PostMapping("/blocks/{blockedUserId}")
    public ResponseEntity<Void> block(@PathVariable Long blockedUserId,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        diaryExchangeService.blockUser(loginUser.getUserId(), blockedUserId);
        return ResponseEntity.noContent().build();
    }

    // 차단한 상대를 다시 허용하는 API
    @DeleteMapping("/blocks/{blockedUserId}")
    public ResponseEntity<Void> unblock(@PathVariable Long blockedUserId,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        diaryExchangeService.unblockUser(loginUser.getUserId(), blockedUserId);
        return ResponseEntity.noContent().build();
    }

    // 내가 차단한 사용자 목록을 조회하는 API
    @GetMapping("/blocks")
    public ResponseEntity<List<Long>> blocks(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.findBlockedUsers(loginUser.getUserId()));
    }
}
