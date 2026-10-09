package com.example.demo.controller;

import com.example.demo.dto.request.DiaryCreateRequest;
import com.example.demo.dto.request.DiaryUpdateRequest;
import com.example.demo.dto.response.DiaryCreateResponse;
import com.example.demo.dto.response.DiaryExchangeResponse;
import com.example.demo.dto.response.DiaryResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
public class DiaryController {
    private final DiaryService diaryService;

    // 오늘의 일기를 저장하고, 교환 가능한 상대가 있으면 바로 매칭까지 시도하는 API
    @PostMapping
    public ResponseEntity<DiaryCreateResponse> create(
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        DiaryCreateResponse response = diaryService.create(loginUser.getUserId(), request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{diaryId}/exchange")
                .buildAndExpand(response.getDiary().getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // 내가 쓴 일기와 교환된 상대방의 일기를 함께 조회하는 API
    @GetMapping("/{diaryId}/exchange")
    public ResponseEntity<DiaryExchangeResponse> findExchangedDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        return ResponseEntity.ok(diaryService.findExchangedDiary(loginUser.getUserId(), diaryId));
    }

    // 사용자가 과거에 작성한 일기 목록을 최신순으로 조회하는 API
    @GetMapping("/history")
    public ResponseEntity<List<DiaryResponse>> findHistory(
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        return ResponseEntity.ok(diaryService.findHistory(loginUser.getUserId()));
    }

    // 내가 작성한 일기의 제목, 내용, 기분을 수정하는 API
    @PutMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> update(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails loginUser,
            @Valid @RequestBody DiaryUpdateRequest request
    ) {
        return ResponseEntity.ok(diaryService.update(loginUser.getUserId(), diaryId, request));
    }

    // 일기 삭제 시 실제 삭제 대신 삭제 여부를 Y로 변경하는 API
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        diaryService.delete(loginUser.getUserId(), diaryId);
        return ResponseEntity.noContent().build();
    }
}
