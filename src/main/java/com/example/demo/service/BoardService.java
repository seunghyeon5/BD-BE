package com.example.demo.service;

import com.example.demo.dto.request.BoardCreateRequest;
import com.example.demo.dto.request.BoardUpdateRequest;
import com.example.demo.dto.response.BoardResponse;
import com.example.demo.entity.BoardEntity;
import com.example.demo.exception.BoardNotFoundException;
import com.example.demo.exception.InvalidBoardPasswordException;
import com.example.demo.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    // 요청값을 엔티티로 바꿔 저장한 뒤 응답 DTO로 반환
    public BoardResponse save(BoardCreateRequest request) {
        BoardEntity boardEntity = BoardEntity.create(
                request.getBoardWriter(),
                request.getBoardPass(),
                request.getBoardTitle(),
                request.getBoardContents()
        );
        return BoardResponse.from(boardRepository.save(boardEntity));
    }

    public List<BoardResponse> findAll() {
        return boardRepository.findAllByOrderByIdDesc().stream()
                .map(BoardResponse::from)
                .toList();
    }

    @Transactional
    // 상세 조회 시 필요하면 조회수를 함께 증가
    public BoardResponse findById(Long id, boolean increaseHits) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));
        if (increaseHits) {
            boardEntity.increaseHits();
        }
        return BoardResponse.from(boardEntity);
    }

    public void delete(long id) {
        if (!boardRepository.existsById(id)) {
            throw new BoardNotFoundException(id);
        }
        boardRepository.deleteById(id);
    }

    @Transactional
    // 저장된 비밀번호와 비교한 뒤 제목/내용만 수정
    public BoardResponse update(Long id, BoardUpdateRequest request) {
        BoardEntity boardEntity = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));
        if (boardEntity.getBoardPass().equals(request.getBoardPass())) {
            boardEntity.update(request.getBoardTitle(), request.getBoardContents());
            return BoardResponse.from(boardEntity);
        } else {
            throw new InvalidBoardPasswordException();
        }
    }
}
