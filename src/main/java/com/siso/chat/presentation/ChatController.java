package com.siso.chat.presentation;

import com.siso.chat.application.ChatMessageService;
import com.siso.chat.application.ChatRoomLimitService;
import com.siso.chat.application.ChatRoomMemberService;
import com.siso.chat.application.ChatRoomService;
import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.dto.request.*;
import com.siso.chat.dto.response.ChatMessageResponseDto;
import com.siso.chat.dto.response.ChatRoomLimitResponseDto;
import com.siso.chat.dto.response.ChatRoomResponseDto;
import com.siso.common.response.SisoResponse;
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

    // 1. 메시지 전송
    @PostMapping("/messages")
    public SisoResponse<ChatMessageResponseDto> sendMessage(@Valid @RequestBody ChatMessageRequestDto requestDto) {
        ChatMessageResponseDto response = chatMessageService.sendMessage(requestDto);
        return SisoResponse.success(response);
    }

    // 2. 특정 채팅방 메시지 조회
    @GetMapping("/rooms/{chatRoomId}/messages")
    public SisoResponse<List<ChatMessageResponseDto>> getMessages(@PathVariable Long chatRoomId) {
        List<ChatMessageResponseDto> messages = chatMessageService.getMessages(chatRoomId);
        return SisoResponse.success(messages);
    }

    // 3. 메시지 수정
    @PatchMapping("/messages")
    public SisoResponse<ChatMessageResponseDto> editMessage(@Valid @RequestBody EditMessageRequestDto requestDto) {
        ChatMessageResponseDto response = chatMessageService.editMessage(requestDto);
        return SisoResponse.success(response);
    }

    // 4. 메시지 삭제
    @DeleteMapping("/messages/{messageId}")
    public SisoResponse<Void> deleteMessage(@PathVariable Long messageId,
                                            @RequestParam Long senderId) {
        chatMessageService.deleteMessage(messageId, senderId);
        return SisoResponse.success(null);
    }

//    // 5. 채팅방 생성
//    @PostMapping("/rooms")
//    public SisoResponse<ChatRoom> createChatRoom(@Valid @RequestBody List<Long> userIds) {
//        ChatRoom chatRoom = chatRoomService.createChatRoom(userIds);
//        return SisoResponse.success(chatRoom);
//    }

    // 6. 사용자의 채팅방 조회
    @GetMapping("/users/{userId}/rooms")
    public SisoResponse<List<ChatRoomResponseDto>> getChatRoomsForUser(@Valid @PathVariable Long userId) {
        List<ChatRoomResponseDto> chatRooms = chatRoomService.getChatRoomsForUser(userId);
        return SisoResponse.success(chatRooms);
    }

    // 7. 채팅방 나가기
    @DeleteMapping("/rooms/{chatRoomId}/users/{userId}")
    public SisoResponse<Void> leaveChatRoom(@Valid ChatRoomRequestDto requestDto, @PathVariable Long userId) {
        chatRoomService.leaveChatRoom(requestDto, userId);
        return SisoResponse.success(null);
    }

    // 8. 메시지 제한 조회
    @GetMapping("/rooms/limits")
    public SisoResponse<ChatRoomLimitResponseDto> getChatRoomLimit(@Valid @RequestBody ChatRoomLimitRequestDto requestDto) {
        ChatRoomLimitResponseDto response = chatRoomLimitService.getLimit(requestDto);
        return SisoResponse.success(response);
    }

    // 9. 메시지 제한 증가
    @PostMapping("/rooms/limits/increment")
    public SisoResponse<ChatRoomLimitResponseDto> incrementMessageCount(@Valid @RequestBody ChatRoomLimitRequestDto requestDto) {
        ChatRoomLimitResponseDto response = chatRoomLimitService.incrementMessageCount(requestDto);
        return SisoResponse.success(response);
    }

    // 10. 메시지 제한 초기화
    @PostMapping("/rooms/limits/reset")
    public SisoResponse<Void> resetMessageCount(@Valid @RequestBody ChatRoomLimitRequestDto requestDto) {
        chatRoomLimitService.resetLimit(requestDto);
        return SisoResponse.success(null);
    }

    // 11. 마지막 읽은 메시지 업데이트
    @PostMapping("/rooms/read")
    public SisoResponse<Void> markAsRead(@Valid @RequestBody ChatReadRequestDto requestDto) {
        chatRoomMemberService.markAsRead(requestDto);
        return SisoResponse.success(null);
    }

    // 12. 채팅방 멤버 조회
    @GetMapping("/rooms/{chatRoomId}/members")
    public SisoResponse<List<ChatRoomMember>> getMembers(@PathVariable Long chatRoomId) {
        List<ChatRoomMember> members = chatRoomMemberService.getMembers(chatRoomId);
        return SisoResponse.success(members);
    }

    // 13. 채팅 이어나가기
    @PatchMapping("/{chatRoomId}/accept")
    public SisoResponse<Void> acceptChatRoom(@Valid @RequestBody ChatRoomRequestDto requestDto) {
        chatRoomService.acceptChatRoom(requestDto);
        return SisoResponse.success(null);
    }
}


