package com.siso.call.application;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AgoraChannelNameService {
    // 채널 이름 생성
    public String generateChannelName(Long callerId, Long receiverId) {
        return callerId + "_" + receiverId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
