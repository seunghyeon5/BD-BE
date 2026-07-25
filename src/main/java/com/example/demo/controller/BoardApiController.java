package com.example.demo.controller;

import com.example.demo.dto.request.BoardCreateRequest;
import com.example.demo.dto.request.BoardUpdateRequest;
import com.example.demo.dto.response.BoardResponse;
import com.example.demo.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardApiController {
    private final BoardService boardService;

    // 게시글 등록 API
    @PostMapping
    public ResponseEntity<BoardResponse> save(@Valid @RequestBody BoardCreateRequest request) {
        BoardResponse savedBoard = boardService.save(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedBoard.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedBoard);
    }

    // 게시글 전체 조회 API
    @GetMapping
    public ResponseEntity<List<BoardResponse>> findAll() {
        return ResponseEntity.ok(boardService.findAll());
    }

    // 게시글 상세 조회 API, 조회수도 함께 증가
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.findById(id, true));
    }

    // 게시글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BoardUpdateRequest request
    ) {
        return ResponseEntity.ok(boardService.update(id, request));
    }

    // 게시글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
