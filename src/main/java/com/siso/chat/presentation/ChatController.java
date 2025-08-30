package com.siso.chat.presentation;

import com.siso.chat.application.ChatMessageService;
import com.siso.chat.application.ChatRoomLimitService;
import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.application.ChatRoomService;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.dto.request.*;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.chat.dto.response.ChatRoomLimitResponseDto;
import com.siso.chat.dto.response.ChatRoomResponseDto;
import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomLimitService chatRoomLimitService;
    private final ChatRoomMemberService chatRoomMemberService;

    // 메시지 전송
    @PostMapping("/messages")
    public SisoResponse<ChatMessageResponseDto> sendMessage(@CurrentUser User user,
                                                            @Valid @RequestBody ChatMessageRequestDto requestDto) {
        ChatMessageResponseDto response = chatMessageService.sendMessage(requestDto, user);
        return SisoResponse.success(response);
    }

    // 메시지 수정
    @PatchMapping("/messages")
    public SisoResponse<ChatMessageResponseDto> editMessage(@CurrentUser User user,
                                                            @Valid @RequestBody EditMessageRequestDto requestDto) {
        ChatMessageResponseDto response = chatMessageService.editMessage(requestDto, user);
        return SisoResponse.success(response);
    }

    // 메시지 삭제
    @DeleteMapping("/messages/{messageId}")
    public SisoResponse<Void> deleteMessage(@PathVariable Long messageId,
                                            @CurrentUser User user) {
        chatMessageService.deleteMessage(messageId, user);
        return SisoResponse.success(null);
    }

    // 특정 채팅방 메시지 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    public SisoResponse<List<ChatMessageResponseDto>> getMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessages(chatRoomId);
        return SisoResponse.success(messages);
    }


    // 채팅방 멤버 조회
    @GetMapping("/rooms/{chatRoomId}/members")
    public SisoResponse<List<ChatRoomMember>> getMembers(@PathVariable Long chatRoomId) {
        List<ChatRoomMember> members = chatRoomMemberService.getMembers(chatRoomId);
        return SisoResponse.success(members);
    }

    // 사용자의 채팅방 조회
    @GetMapping("/rooms")
    public List<ChatRoomResponseDto> getChatRooms(@CurrentUser User user) {
        return chatRoomService.getChatRoomsForUser(user);
    }

    // 채팅 이어나가기
    @PostMapping("/accept")
    public void acceptChatRoom(@CurrentUser User user,
                               @RequestBody ChatRoomRequestDto requestDto) {
        chatRoomService.acceptChatRoom(requestDto, user);
    }

    // 채팅방 나가기
    @PostMapping("/leave")
    public void leaveChatRoom(@CurrentUser User user,
                              @RequestBody ChatRoomRequestDto requestDto) {
        chatRoomService.leaveChatRoom(requestDto, user);
    }

    // 마지막 읽은 메시지 업데이트
    @PostMapping("/read")
    public void markAsRead(@CurrentUser User user,
                           @RequestBody ChatReadRequestDto requestDto) {
        chatRoomMemberService.markAsRead(requestDto, user);
    }

    // 메시지 제한 조회
    @GetMapping("/rooms/limits")
    public SisoResponse<ChatRoomLimitResponseDto> getChatRoomLimit(@CurrentUser User user,
                                                                   @Valid @RequestBody ChatRoomLimitRequestDto requestDto) {
        ChatRoomLimitResponseDto response = chatRoomLimitService.getLimit(requestDto, user);
        return SisoResponse.success(response);
    }
}


