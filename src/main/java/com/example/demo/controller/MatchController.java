package com.example.demo.controller;

import com.example.demo.dto.request.MatchCreateRequest;
import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.dto.response.BlockedUserResponse;
import com.example.demo.dto.request.DiaryCreateRequest;
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

    // 처음 받은 일기와 내가 답장할 차례가 된 교환 목록을 조회하는 API
    @GetMapping("/inbox")
    public ResponseEntity<List<DiaryExchangeResponse>> inbox(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.findInbox(loginUser.getUserId()));
    }

    // 내 차례의 일기에 답장하고 상대에게 다음 24시간 기한을 주는 API
    @PostMapping("/{exchangeId}/reply")
    public ResponseEntity<DiaryExchangeResponse> reply(@PathVariable Long exchangeId,
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.reply(loginUser.getUserId(), exchangeId, request));
    }

    // 받은 일기 교환 요청을 거절하는 API
    @PostMapping("/{exchangeId}/reject")
    public ResponseEntity<DiaryExchangeResponse> reject(@PathVariable Long exchangeId,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.reject(loginUser.getUserId(), exchangeId));
    }

    // 상대가 없었거나 답장이 오지 않은 내 일기를 다시 전달하는 API
    @PostMapping
    public ResponseEntity<DiaryExchangeResponse> match(
            @Valid @RequestBody MatchCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        return ResponseEntity.ok(diaryExchangeService.matchDiary(loginUser.getUserId(), request.getDiaryId()));
    }

    // 내가 보냈거나 받은 교환 요청의 상태를 조회하는 API
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

    // 차단 목록에서 선택한 사용자의 차단을 해제하는 API
    @DeleteMapping("/blocks/{blockedUserId}")
    public ResponseEntity<Void> unblock(@PathVariable Long blockedUserId,
            @AuthenticationPrincipal CustomUserDetails loginUser) {
        diaryExchangeService.unblockUser(loginUser.getUserId(), blockedUserId);
        return ResponseEntity.noContent().build();
    }

    // 내가 차단한 사용자 목록을 ID와 닉네임으로 조회하는 API
    @GetMapping("/blocks")
    public ResponseEntity<List<BlockedUserResponse>> blocks(@AuthenticationPrincipal CustomUserDetails loginUser) {
        return ResponseEntity.ok(diaryExchangeService.findBlockedUsers(loginUser.getUserId()));
    }
}
