package com.example.demo.service;

import com.example.demo.entity.DiaryEntity;
import com.example.demo.entity.DiaryExchangeRequestEntity;
import com.example.demo.entity.ExchangeStatus;
import com.example.demo.entity.UserEntity;
import com.example.demo.dto.request.DiaryCreateRequest;
import com.example.demo.repository.BlockedUserRepository;
import com.example.demo.repository.DiaryExchangeRequestRepository;
import com.example.demo.repository.DiaryRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(exchangeRepository.countByRecipientUserIdAndStatusAndExpiresAtAfter(eq(2L), eq(ExchangeStatus.PENDING), any()))
                .thenReturn(1L);

        assertNull(service.createExchangeIfPossible(diary));
        verify(exchangeRepository, never()).save(any());
    }

    @Test
    void recipientReplyCompletesExchange() {
        UserEntity sender = mock(UserEntity.class);
        UserEntity recipient = mock(UserEntity.class);
        DiaryEntity sent = mock(DiaryEntity.class);
        DiaryEntity reply = mock(DiaryEntity.class);
        when(sender.getId()).thenReturn(1L);
        when(recipient.getId()).thenReturn(2L);
        when(sent.getDiaryDate()).thenReturn(LocalDate.of(2026, 10, 31));
        when(sent.getUser()).thenReturn(sender);
        when(reply.getUser()).thenReturn(recipient);
        DiaryExchangeRequestEntity exchange = DiaryExchangeRequestEntity.create(sender, recipient, sent,
                LocalDateTime.now().plusHours(24));
        when(exchangeRepository.findForUpdate(5L)).thenReturn(Optional.of(exchange));
        when(diaryRepository.save(any(DiaryEntity.class))).thenReturn(reply);

        DiaryCreateRequest body = new DiaryCreateRequest();
        body.setDiaryDate(LocalDate.of(2026, 10, 31));
        body.setTitle("답장");
        body.setContent("잘 받았습니다.");

        var response = service.reply(2L, 5L, body);

        assertEquals(ExchangeStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getMyDiary());
        assertNotNull(response.getPartnerDiary());
    }
}
