package com.example.demo.service;

import com.example.demo.dto.BoardDto;
import com.example.demo.entity.BoardEntity;
import com.example.demo.repository.BoardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public void save(BoardDto boardDto) {
        BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDto);
        boardRepository.save(boardEntity);
    }

    public List<BoardDto> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDto> boardDtoList = new ArrayList<>();
        for (BoardEntity boardEntity : boardEntityList) {
            boardDtoList.add(BoardDto.toBoardDTO(boardEntity));
        }
        return boardDtoList;
    }

    @Transactional
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    public BoardDto findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            return BoardDto.toBoardDTO(boardEntity);
        } else {
            return null;
        }
    }

    public void delete(long id) {
        boardRepository.deleteById(id);
    }

    @Transactional
    public void update(BoardDto boardDto) {
        BoardEntity boardEntity = boardRepository.findById(boardDto.getId()).orElseThrow(() ->
                    new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
        if (boardEntity.getBoardPass().equals(boardDto.getUpdatePass())) {
            boardEntity.update(boardDto);
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
}
