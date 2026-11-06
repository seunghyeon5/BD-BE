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
    // 만료 처리와 답장 작성이 동시에 일어나지 않도록 대상 요청을 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<DiaryExchangeRequestEntity> findAllByStatusInAndExpiresAtLessThanEqual(List<ExchangeStatus> statuses, LocalDateTime now);
    List<DiaryExchangeRequestEntity> findAllBySenderDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    List<DiaryExchangeRequestEntity> findAllBySenderDiaryIdOrReplyDiaryId(Long senderDiaryId, Long replyDiaryId);
    boolean existsBySenderDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    boolean existsByReplyDiaryIdAndStatus(Long diaryId, ExchangeStatus status);
    // 답장 차례를 기다리는 요청은 PENDING과 COMPLETED 모두 수신 한도에 포함
    @Query("""
            select count(e) from DiaryExchangeRequestEntity e
            where e.nextReplyUser.id = :userId and e.status in :statuses and e.expiresAt > :now
            """)
    long countWaitingForUser(@Param("userId") Long userId, @Param("statuses") List<ExchangeStatus> statuses,
                             @Param("now") LocalDateTime now);
}
