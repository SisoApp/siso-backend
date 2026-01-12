package com.siso.chat.application;

import com.siso.chat.domain.model.*;
import com.siso.chat.domain.repository.ChatMessageRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import com.siso.chat.dto.request.EditMessageRequestDto;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChatMessageService 단위 테스트
 *
 * 테스트 대상:
 * - 메시지 전송 성공 (LIMITED 상태에서 제한 확인)
 * - 메시지 전송 실패 (메시지 제한 초과)
 * - 메시지 전송 성공 (UNLIMITED 상태)
 * - 메시지 조회 (페이징)
 * - 메시지 수정
 * - 메시지 삭제 (soft delete)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatMessageService 단위 테스트")
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private User sender;
    private ChatRoom chatRoom;
    private ChatRoomMember member;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .provider(Provider.KAKAO)
                .email("sender@test.com")
                .phoneNumber("010-1234-5678")
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        // ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sender, 1L);
        } catch (Exception e) {
        }

        chatRoom = mock(ChatRoom.class);
        member = mock(ChatRoomMember.class);
    }

    @Test
    @DisplayName("메시지 전송 성공 - LIMITED 상태에서 제한 내")
    void sendMessage_whenLimitedAndUnderLimit_shouldSendMessage() {
        // Given
        Long chatRoomId = 1L;
        String content = "안녕하세요";
        ChatMessageRequestDto requestDto = new ChatMessageRequestDto();

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getId()).thenReturn(chatRoomId);
        when(chatRoom.getChatRoomStatus()).thenReturn(ChatRoomStatus.LIMITED);

        when(chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(chatRoomId, 1L))
                .thenReturn(Optional.of(member));
        when(member.canSendMessage()).thenReturn(true);
        when(member.getId()).thenReturn(1L);

        ChatMessage savedMessage = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content(content)
                .build();

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        // When
        ChatMessageResponseDto result = chatMessageService.sendMessage(requestDto, sender);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(content);

        verify(chatRoomRepository).findById(chatRoomId);
        verify(chatRoomMemberRepository).findMemberByChatRoomIdAndUserId(chatRoomId, 1L);
        verify(member).canSendMessage();
        verify(member).increaseMessageCount();
        verify(chatRoomMemberRepository).save(member);
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("메시지 전송 실패 - LIMITED 상태에서 메시지 제한 초과")
    void sendMessage_whenLimitedAndExceedLimit_shouldThrowException() {
        // Given
        Long chatRoomId = 1L;
        String content = "안녕하세요";
        ChatMessageRequestDto requestDto = new ChatMessageRequestDto();

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getId()).thenReturn(chatRoomId);
        when(chatRoom.getChatRoomStatus()).thenReturn(ChatRoomStatus.LIMITED);

        when(chatRoomMemberRepository.findMemberByChatRoomIdAndUserId(chatRoomId, 1L))
                .thenReturn(Optional.of(member));
        when(member.canSendMessage()).thenReturn(false);  // 메시지 제한 초과

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(requestDto, sender))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MESSAGE_LIMIT_EXCEEDED);

        verify(chatRoomRepository).findById(chatRoomId);
        verify(chatRoomMemberRepository).findMemberByChatRoomIdAndUserId(chatRoomId, 1L);
        verify(member).canSendMessage();
        verify(member, never()).increaseMessageCount();
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("메시지 전송 성공 - UNLIMITED 상태")
    void sendMessage_whenUnlimited_shouldSendMessageWithoutLimitCheck() {
        // Given
        Long chatRoomId = 1L;
        String content = "안녕하세요";
        ChatMessageRequestDto requestDto = new ChatMessageRequestDto();

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getId()).thenReturn(chatRoomId);
        when(chatRoom.getChatRoomStatus()).thenReturn(ChatRoomStatus.MATCHED);

        ChatMessage savedMessage = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content(content)
                .build();

        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        // When
        ChatMessageResponseDto result = chatMessageService.sendMessage(requestDto, sender);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(content);

        verify(chatRoomRepository).findById(chatRoomId);
        verify(chatRoomMemberRepository, never()).findMemberByChatRoomIdAndUserId(any(), any());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("메시지 전송 실패 - 채팅방을 찾을 수 없음")
    void sendMessage_whenChatRoomNotFound_shouldThrowException() {
        // Given
        Long chatRoomId = 999L;
        ChatMessageRequestDto requestDto = new ChatMessageRequestDto();

        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatMessageService.sendMessage(requestDto, sender))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHATROOM_NOT_FOUND);

        verify(chatRoomRepository).findById(chatRoomId);
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("메시지 조회 성공 - 최신 메시지 조회")
    void getMessages_whenLastMessageIdIsNull_shouldReturnLatestMessages() {
        // Given
        Long chatRoomId = 1L;
        int size = 20;

        ChatMessage message1 = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("메시지 1")
                .build();

        ChatMessage message2 = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("메시지 2")
                .build();

        List<ChatMessage> messages = List.of(message2, message1);  // 내림차순
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(chatMessageRepository.findByChatRoomId(chatRoomId, pageable)).thenReturn(messages);

        // When
        List<ChatMessageResponseDto> result = chatMessageService.getMessages(chatRoomId, null, size);

        // Then: 역순 정렬되어 반환됨 (올림차순)
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);

        verify(chatMessageRepository).findByChatRoomId(eq(chatRoomId), any(Pageable.class));
    }

    @Test
    @DisplayName("메시지 조회 성공 - lastMessageId 이전 메시지 조회")
    void getMessages_whenLastMessageIdExists_shouldReturnMessagesBeforeIt() {
        // Given
        Long chatRoomId = 1L;
        Long lastMessageId = 10L;
        int size = 20;

        ChatMessage message1 = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("이전 메시지 1")
                .build();

        List<ChatMessage> messages = List.of(message1);
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        when(chatMessageRepository.findByChatRoomIdAndIdLessThan(chatRoomId, lastMessageId, pageable))
                .thenReturn(messages);

        // When
        List<ChatMessageResponseDto> result = chatMessageService.getMessages(chatRoomId, lastMessageId, size);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(chatMessageRepository).findByChatRoomIdAndIdLessThan(eq(chatRoomId), eq(lastMessageId), any(Pageable.class));
    }

    @Test
    @DisplayName("메시지 수정 성공")
    void editMessage_whenOwner_shouldUpdateMessage() {
        // Given
        Long messageId = 1L;
        String newContent = "수정된 메시지";
        EditMessageRequestDto requestDto = new EditMessageRequestDto();

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("원본 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

        // When
        ChatMessageResponseDto result = chatMessageService.editMessage(requestDto, sender);

        // Then
        assertThat(result).isNotNull();
        verify(chatMessageRepository).findById(messageId);
        verify(chatMessageRepository).save(message);
    }

    @Test
    @DisplayName("메시지 수정 실패 - 소유자가 아님")
    void editMessage_whenNotOwner_shouldThrowException() {
        // Given
        Long messageId = 1L;
        EditMessageRequestDto requestDto = new EditMessageRequestDto();

        User otherUser = User.builder()
                .provider(Provider.KAKAO)
                .email("other@test.com")
                .phoneNumber("010-9999-8888")
                .build();

        // ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(otherUser, 999L);
        } catch (Exception e) {
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("원본 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // When & Then
        assertThatThrownBy(() -> chatMessageService.editMessage(requestDto, otherUser))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_MESSAGE);

        verify(chatMessageRepository).findById(messageId);
        verify(chatMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("메시지 삭제 성공 (soft delete)")
    void deleteMessage_whenOwner_shouldSoftDelete() {
        // Given
        Long messageId = 1L;

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("삭제할 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

        // When
        chatMessageService.deleteMessage(messageId, sender);

        // Then
        verify(chatMessageRepository).findById(messageId);
        verify(chatMessageRepository).save(message);
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 소유자가 아님")
    void deleteMessage_whenNotOwner_shouldThrowException() {
        // Given
        Long messageId = 1L;

        User otherUser = User.builder()
                .provider(Provider.KAKAO)
                .email("other@test.com")
                .phoneNumber("010-9999-8888")
                .build();

        // ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(otherUser, 999L);
        } catch (Exception e) {
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content("삭제할 메시지")
                .build();

        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(message));

        // When & Then
        assertThatThrownBy(() -> chatMessageService.deleteMessage(messageId, otherUser))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_YOUR_MESSAGE);

        verify(chatMessageRepository).findById(messageId);
        verify(chatMessageRepository, never()).save(any());
    }
}
