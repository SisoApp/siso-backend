package com.siso.chat.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OnlineUserRegistry {
    private final Map<String, Boolean> onlineUsers = new ConcurrentHashMap<>();

    public void addOnlineUser(String userId) {
        boolean alreadyOnline = onlineUsers.containsKey(userId);
        onlineUsers.put(userId, true);
        if (alreadyOnline) {
            log.info("[REGISTRY] userId={} 이미 온라인 상태 (중복 추가 시도)", userId);
        } else {
            log.info("[REGISTRY] userId={} 추가됨 (현재 온라인 사용자 수 = {})", userId, onlineUsers.size());
        }
    }

    public void removeOnlineUser(String userId) {
        if (onlineUsers.remove(userId) != null) {
            log.info("[REGISTRY] userId={} 제거됨 (현재 온라인 사용자 수 = {})", userId, onlineUsers.size());
        } else {
            log.info("[REGISTRY] userId={} 제거 시도했지만 존재하지 않음", userId);
        }
    }

    public boolean isOnline(String userId) {
        boolean online = onlineUsers.containsKey(userId);
        log.debug("[REGISTRY] userId={} 온라인 여부 = {}", userId, online);
        return online;
    }

    public Map<String, Boolean> getOnlineUsers() {
        return Collections.unmodifiableMap(onlineUsers); // 읽기 전용 반환
    }
}
