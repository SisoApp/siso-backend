package com.siso.chat.presentation;

import com.siso.chat.application.ChatService;
import com.siso.chat.dto.request.ChatMessageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

//    @MessageMapping("/chat.send") // 클라: /app/chat.send
//    public void send(ChatMessageRequestDto msg) {
//        chatService.convertAndSend("/topic/chat/" + msg.getRoomId(), msg);
//    }
}
