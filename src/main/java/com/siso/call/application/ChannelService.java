package com.siso.call.application;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChannelService {
    // 채널 이름 생성
    public String generateChannelName(Long callerId, Long calleeId) {
        return "room_" + callerId + "_" + calleeId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
