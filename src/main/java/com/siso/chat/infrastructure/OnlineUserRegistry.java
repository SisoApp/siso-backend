package com.siso.chat.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class OnlineUserRegistry {
    // userId -> set of sessionIds
    private final Map<String, Set<String>> onlineUsers = new ConcurrentHashMap<>();
    // sessionId -> userId (역색인)
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    public synchronized void addOnlineUser(String userId, String sessionId) {
        onlineUsers.computeIfAbsent(userId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(sessionId);
        sessionToUser.put(sessionId, userId);
        log.info("[REGISTRY] userId={} sessionId={} 추가됨 (현재 온라인 사용자 수={})", userId, sessionId, onlineUsers.size());
    }

    public synchronized void removeOnlineUser(String userId, String sessionId) {
        Set<String> sessions = onlineUsers.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            sessionToUser.remove(sessionId);
            if (sessions.isEmpty()) {
                onlineUsers.remove(userId);
            }
            log.info("[REGISTRY] userId={} sessionId={} 제거됨 (현재 온라인 사용자 수={})", userId, sessionId, onlineUsers.size());
        } else {
            log.info("[REGISTRY] userId={} 제거 시도했지만 존재하지 않음", userId);
        }
    }

    public synchronized void removeBySessionId(String sessionId) {
        String userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = onlineUsers.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    onlineUsers.remove(userId);
                }
            }
            log.info("[REGISTRY] sessionId={} 기반 제거 완료 userId={}", sessionId, userId);
        } else {
            log.info("[REGISTRY] sessionId={} 기반 제거 시도했지만 매핑없음", sessionId);
        }
    }

    public boolean isOnline(String userId) {
        Set<String> sessions = onlineUsers.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Map<String, Set<String>> getOnlineUsers() {
        return Collections.unmodifiableMap(onlineUsers);
    }
}


