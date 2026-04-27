package com.example.demo.repository;

import com.example.demo.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    @Modifying
    @Query("update BoardEntity b set b.boardHits = b.boardHits + 1 where b.id = :id ") //특수하게 설정하고 싶을때
    void updateHits(@Param("id") Long id);
}
