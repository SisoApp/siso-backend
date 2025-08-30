package com.siso.chat.application;

import com.siso.chat.domain.model.ChatRoom;
import com.siso.chat.domain.model.ChatRoomLimit;
import com.siso.chat.domain.model.ChatRoomMember;
import com.siso.chat.domain.repository.ChatRoomLimitRepository;
import com.siso.chat.domain.repository.ChatRoomMemberRepository;
import com.siso.chat.domain.repository.ChatRoomRepository;
import com.siso.chat.dto.request.ChatRoomLimitRequestDto;
import com.siso.chat.dto.response.ChatRoomLimitResponseDto;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomLimitService {

    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomLimitRepository chatRoomLimitRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 유저의 채팅방 메시지 제한 정보 조회
     */
    public ChatRoomLimitResponseDto getLimit(ChatRoomLimitRequestDto requestDto, User user) {
        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());

        ChatRoomLimit limit = getOrCreateChatRoomLimit(chatRoom, user);

        return toDto(limit);
    }

    /* ====================== Helper Methods ====================== */

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));
    }

    private ChatRoomLimit getOrCreateChatRoomLimit(ChatRoom chatRoom, User user) {
        return chatRoomLimitRepository.findLimitByChatRoomIdAndUserId(chatRoom.getId(), user.getId())
                .orElseGet(() -> {
                    // ChatRoomMember 생성 및 저장
                    ChatRoomMember member = ChatRoomMember.builder()
                            .user(user)
                            .build();
                    chatRoomMemberRepository.save(member);

                    // ChatRoomLimit 생성 및 저장
                    ChatRoomLimit newLimit = ChatRoomLimit.builder()
                            .chatRoomMember(member)
                            .build(); // Builder에서 user 자동 연결
                    return chatRoomLimitRepository.save(newLimit);
                });
    }

    private ChatRoomLimitResponseDto toDto(ChatRoomLimit limit) {
        return new ChatRoomLimitResponseDto(
                limit.getChatRoomMember().getId(),
                limit.getUser().getId(),
                limit.getSentCount()
        );
    }
}

//public class ChatRoomLimitService {
//    private final ChatRoomMemberRepository chatRoomMemberRepository;
//    private final ChatRoomLimitRepository chatRoomLimitRepository;
//    private final ChatRoomRepository chatRoomRepository;
//    private final UserRepository userRepository;
//
//    /**
//     * 유저의 채팅방 메시지 제한 정보 조회
//     */
//    public ChatRoomLimitResponseDto getLimit(ChatRoomLimitRequestDto requestDto) {
//        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
//        User user = getUser(requestDto.getUserId());
//
//        ChatRoomLimit limit = getOrCreateChatRoomLimit(chatRoom, user);
//
//        return toDto(limit);
//    }
//
//    /**
//     * 메시지 전송 횟수 1 증가
//     */
//    @Transactional
//    public ChatRoomLimitResponseDto incrementMessageCount(ChatRoomLimitRequestDto requestDto) {
//        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
//        User user = getUser(requestDto.getUserId());
//
//        ChatRoomLimit limit = getOrCreateChatRoomLimit(chatRoom, user);
//        limit.increaseCount();
//        chatRoomLimitRepository.save(limit);
//
//        return toDto(limit);
//    }
//
//    /**
//     * 메시지 제한 초기화
//     */
//    @Transactional
//    public void resetLimit(ChatRoomLimitRequestDto requestDto) {
//        ChatRoom chatRoom = getChatRoom(requestDto.getChatRoomId());
//        User user = getUser(requestDto.getUserId());
//
//        ChatRoomLimit limit = getOrCreateChatRoomLimit(chatRoom, user);
//        limit.resetSentCount();
//        chatRoomLimitRepository.save(limit);
//    }
//
//    private ChatRoom getChatRoom(Long chatRoomId) {
//        return chatRoomRepository.findById(chatRoomId)
//                .orElseThrow(() -> new ExpectedException(ErrorCode.CHATROOM_NOT_FOUND));
//    }
//
//    private User getUser(Long userId) {
//        return userRepository.findById(userId)
//                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
//    }
//
//    private ChatRoomLimit getOrCreateChatRoomLimit(ChatRoom chatRoom, User user) {
//        return chatRoomLimitRepository.findLimitByChatRoomIdAndUserId(chatRoom.getId(), user.getId())
//                .orElseGet(() -> {
//                    // ChatRoomMember 생성 및 저장
//                    ChatRoomMember member = ChatRoomMember.builder()
//                            .user(user)
//                            .build();
//                    chatRoomMemberRepository.save(member);
//
//                    // ChatRoomLimit 생성 및 저장
//                    ChatRoomLimit newLimit = ChatRoomLimit.builder()
//                            .chatRoomMember(member)
//                            .build(); // user와 연결은 Builder에서 자동 처리
//                    return chatRoomLimitRepository.save(newLimit);
//                });
//    }
//
//    private ChatRoomLimitResponseDto toDto(ChatRoomLimit limit) {
//        return new ChatRoomLimitResponseDto(
//                limit.getChatRoomMember().getId(),
//                limit.getUser().getId(),
//                limit.getSentCount()
//        );
//    }
//}