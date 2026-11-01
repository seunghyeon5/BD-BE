package com.example.demo.repository;

import com.example.demo.entity.DiaryExchangeRequestEntity;
import com.example.demo.entity.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryExchangeRequestRepository extends JpaRepository<DiaryExchangeRequestEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DiaryExchangeRequestEntity e where e.id = :id")
    Optional<DiaryExchangeRequestEntity> findForUpdate(@Param("id") Long id);

    List<DiaryExchangeRequestEntity> findAllBySenderUserIdOrRecipientUserIdOrderByIdDesc(Long senderId, Long recipientId);
    List<DiaryExchangeRequestEntity> findAllByRecipientUserIdAndStatusOrderByIdDesc(Long recipientId, ExchangeStatus status);
    List<DiaryExchangeRequestEntity> findAllByStatusAndExpiresAtLessThanEqual(ExchangeStatus status, LocalDateTime now);
    List<DiaryExchangeRequestEntity> findAllBySenderDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    List<DiaryExchangeRequestEntity> findAllBySenderDiaryIdOrReplyDiaryId(Long senderDiaryId, Long replyDiaryId);
    boolean existsBySenderDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    boolean existsByReplyDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    long countByRecipientUserIdAndStatusAndExpiresAtAfter(Long recipientId, ExchangeStatus status, LocalDateTime now);
}
