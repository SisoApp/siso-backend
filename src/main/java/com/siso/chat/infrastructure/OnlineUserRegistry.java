package com.siso.chat.infrastructure;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserRegistry {
    private final Map<String, Boolean> onlineUsers = new ConcurrentHashMap<>();

    public void addOnlineUser(String userId) {
        onlineUsers.put(userId, true);
    }

    public void removeOnlineUser(String userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }
}
