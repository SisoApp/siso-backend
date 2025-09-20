package com.siso.chat.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class OnlineUserRegistry {
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>(); // userId -> sessionId

    public void addOnlineUser(String userId, String sessionId) {
        boolean alreadyOnline = onlineUsers.containsKey(userId);
        onlineUsers.put(userId, sessionId);
        if (alreadyOnline) {
            log.info("[REGISTRY] userId={} 이미 온라인 (중복 추가)", userId);
        } else {
            log.info("[REGISTRY] userId={} 추가됨 (현재 온라인 수={})", userId, onlineUsers.size());
        }
    }

    public void removeOnlineUser(String userId) {
        if (onlineUsers.remove(userId) != null) {
            log.info("[REGISTRY] userId={} 제거됨 (현재 온라인 수={})", userId, onlineUsers.size());
        } else {
            log.info("[REGISTRY] userId={} 제거 시도했지만 존재하지 않음", userId);
        }
    }

    public boolean isOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    public Map<String, String> getOnlineUsers() {
        return Collections.unmodifiableMap(onlineUsers);
    }
}

