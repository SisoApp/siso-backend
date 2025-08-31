package com.siso.notification.domain.model;

public enum NotificationType {
    MATCHING("매칭"),
    MESSAGE("채팅"),
    CALL("통화");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}