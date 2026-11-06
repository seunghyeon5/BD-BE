package com.example.demo.service;

import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.DiaryExchangeRequestEntity;
import com.example.demo.entity.DiaryExchangeMessageEntity;
import com.example.demo.entity.ExchangeStatus;
import com.example.demo.entity.UserEntity;
import com.example.demo.dto.request.DiaryCreateRequest;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.BlockedUserRepository;
import com.example.demo.repository.DiaryExchangeRequestRepository;
import com.example.demo.repository.DiaryExchangeMessageRepository;
import com.example.demo.repository.DiaryRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryExchangeServiceTest {
    @Mock DiaryExchangeRequestRepository exchangeRepository;
    @Mock DiaryRepository diaryRepository;
    @Mock UserRepository userRepository;
    @Mock BlockedUserRepository blockedUserRepository;
    @Mock DiaryExchangeMessageRepository messageRepository;
    @InjectMocks DiaryExchangeService service;

    @Test
    void sendsDiaryToAvailableRecipientWithoutReplyDiary() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity diary = mock(DiaryEntity.class);
        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);
        when(diary.getId()).thenReturn(10L);
        when(diary.getUser()).thenReturn(sender);
        when(diary.getDiaryDate()).thenReturn(LocalDate.of(2026, 10, 31));
        when(userRepository.findForUpdate(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findForUpdate(2L)).thenReturn(Optional.of(recipient));
        when(userRepository.findAllByDelYn("N")).thenReturn(List.of(sender, recipient));
        when(exchangeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime before = LocalDateTime.now();
        var response = service.createExchangeIfPossible(diary);

        assertEquals(ExchangeStatus.PENDING, response.getStatus());
        assertNull(response.getPartnerDiary());
        assertEquals(2L, response.getRecipientUserId());
        assertTrue(response.getExpiresAt().isAfter(before.plusHours(23)));
    }

    @Test
    void doesNotSendWhenRecipientAlreadyHasPendingRequest() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity diary = mock(DiaryEntity.class);
        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);
        when(diary.getId()).thenReturn(10L);
        when(diary.getUser()).thenReturn(sender);
        when(userRepository.findForUpdate(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findForUpdate(2L)).thenReturn(Optional.of(recipient));
        when(userRepository.findAllByDelYn("N")).thenReturn(List.of(sender, recipient));
        when(exchangeRepository.countWaitingForUser(eq(2L), anyList(), any()))
                .thenReturn(1L);

        assertNull(service.createExchangeIfPossible(diary));
        verify(exchangeRepository, never()).save(any());
    }

    @Test
    void usersCanReplyInTurnsAndDeadlineRenews() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity sent = mock(DiaryEntity.class);
        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);
        when(sent.getDiaryDate()).thenReturn(LocalDate.of(2026, 10, 31));
        when(sent.getUser()).thenReturn(sender);
        DiaryExchangeRequestEntity exchange = DiaryExchangeRequestEntity.create(sender, recipient, sent,
                LocalDateTime.now().plusHours(24));
        when(exchangeRepository.findForUpdate(5L)).thenReturn(Optional.of(exchange));
        when(messageRepository.save(any(DiaryExchangeMessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DiaryCreateRequest body = new DiaryCreateRequest();
        body.setDiaryDate(LocalDate.of(2026, 10, 31));
        body.setTitle("답장");
        body.setContent("잘 받았습니다.");

        var firstReply = service.reply(2L, 5L, body);

        assertEquals(ExchangeStatus.COMPLETED, firstReply.getStatus());
        assertEquals(1L, firstReply.getNextReplyUserId());
        assertEquals(1, firstReply.getMessages().size());
        LocalDateTime firstDeadline = firstReply.getExpiresAt();
        assertThrows(ApiException.class, () -> service.reply(2L, 5L, body));

        var secondReply = service.reply(1L, 5L, body);
        assertEquals(ExchangeStatus.COMPLETED, secondReply.getStatus());
        assertEquals(2L, secondReply.getNextReplyUserId());
        assertEquals(2, secondReply.getMessages().size());
        assertTrue(secondReply.getExpiresAt().isAfter(firstDeadline));
    }

    @Test
    void completedExchangeAlsoExpiresWhenNextUserDoesNotReply() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity sent = mock(DiaryEntity.class);
        DiaryExchangeRequestEntity exchange = DiaryExchangeRequestEntity.create(sender, recipient, sent,
                LocalDateTime.now().minusHours(25));
        exchange.completeTurn(sender, LocalDateTime.now().minusMinutes(1));
        when(exchangeRepository.findAllByStatusInAndExpiresAtLessThanEqual(anyList(), any()))
                .thenReturn(List.of(exchange));

        service.expireDueRequests();

        assertEquals(ExchangeStatus.EXPIRED, exchange.getStatus());
    }

    @Test
    void legacyCompletedExchangeStartsNextDeadlineAtFirstReply() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity sent = mock(DiaryEntity.class);
        DiaryEntity oldReply = mock(DiaryEntity.class);
        LocalDateTime repliedAt = LocalDateTime.now().minusHours(2);
        DiaryExchangeRequestEntity exchange = DiaryExchangeRequestEntity.create(sender, recipient, sent,
                repliedAt.minusHours(1));
        ReflectionTestUtils.setField(exchange, "replyDiary", oldReply);
        ReflectionTestUtils.setField(exchange, "status", ExchangeStatus.COMPLETED);
        ReflectionTestUtils.setField(exchange, "nextReplyUser", null);
        ReflectionTestUtils.setField(exchange, "updatedAt", repliedAt);

        exchange.initializeNextTurnForLegacy();

        assertSame(sender, exchange.getNextReplyUser());
        assertEquals(repliedAt.plusHours(24), exchange.getExpiresAt());
    }
}
