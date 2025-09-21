package com.siso.chat.presentation;

import com.siso.chat.application.ChatMessageService;
import com.siso.chat.application.ChatRoomLimitService;
import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.application.ChatRoomService;
import com.siso.chat.dto.request.*;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.chat.dto.response.ChatRoomLimitResponseDto;
import com.siso.chat.dto.response.ChatRoomMemberResponseDto;
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

    // ----------------- 메세지 관리 -----------------
    // 메시지 수정
    @PatchMapping(value = "/messages", produces = "application/json; charset=UTF-8")
    public SisoResponse<ChatMessageResponseDto> editMessage(@CurrentUser User user,
                                                            @Valid @RequestBody EditMessageRequestDto requestDto) {
        ChatMessageResponseDto response = chatMessageService.editMessage(requestDto, user);
        return SisoResponse.success(response);
    }

    // 메시지 삭제
    @DeleteMapping(value = "/messages/{messageId}", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> deleteMessage(@PathVariable Long messageId,
                                            @CurrentUser User user) {
        chatMessageService.deleteMessage(messageId, user);
        return SisoResponse.success(null);
    }

    // 특정 채팅방 메시지 조회
    @GetMapping(value = "/rooms/{chatRoomId}/messages", produces = "application/json; charset=UTF-8")
    public SisoResponse<List<ChatMessageResponseDto>> getMessages(@PathVariable Long chatRoomId,
                                                                  @RequestParam(required = false) Long lastMessageId, // 마지막 메시지 기준
                                                                  @RequestParam(defaultValue = "30") int size) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessages(chatRoomId, lastMessageId, size);
        return SisoResponse.success(messages);
    }

    // ----------------- 채팅방 관리 -----------------
    // 사용자의 채팅방 조회
    @GetMapping(value = "/rooms", produces = "application/json; charset=UTF-8")
    public SisoResponse<List<ChatRoomResponseDto>> getChatRooms(@CurrentUser User user) {
        List<ChatRoomResponseDto> chatRooms = chatRoomService.getChatRoomsForUser(user);
        return SisoResponse.success(chatRooms);
    }

    // 채팅 이어나가기
    @PostMapping(value = "/accept", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> acceptChatRoom(@CurrentUser User user,
                                             @Valid @RequestBody ChatRoomRequestDto requestDto) {
        chatRoomService.acceptChatRoom(requestDto, user);
        return SisoResponse.success(null);
    }

    // 채팅방 나가기
    @PostMapping(value = "/leave", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> leaveChatRoom(@CurrentUser User user,
                                            @Valid @RequestBody ChatRoomRequestDto requestDto) {
        chatRoomService.leaveChatRoom(requestDto, user);
        return SisoResponse.success(null);
    }

    // ----------------- 채팅방 멤버 / 제한 -----------------
    // 채팅방 멤버 조회
    @GetMapping(value = "/rooms/{chatRoomId}/members", produces = "application/json; charset=UTF-8")
    public SisoResponse<List<ChatRoomMemberResponseDto>> getMembers(@PathVariable Long chatRoomId) {
        List<ChatRoomMemberResponseDto> members = chatRoomMemberService.getMembers(chatRoomId);
        return SisoResponse.success(members);
    }

    // 사용자가 채팅방에서 메시지를 얼마나 보낼 수 있는지 제한 정보를 조회
    @GetMapping(value = "/rooms/limits", produces = "application/json; charset=UTF-8")
    public SisoResponse<ChatRoomLimitResponseDto> getChatRoomLimit(@CurrentUser User user,
                                                                   @RequestParam(name = "chatRoomId") Long chatRoomId) {
        ChatRoomLimitResponseDto response = chatRoomLimitService.getLimit(chatRoomId, user);
        return SisoResponse.success(response);
    }
}


