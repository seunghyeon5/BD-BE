package com.example.demo.controller;

import com.example.demo.dto.request.MatchCreateRequest;
import com.example.demo.dto.response.DiaryExchangeResponse;
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
}
