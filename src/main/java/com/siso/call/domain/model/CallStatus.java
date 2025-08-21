package com.siso.call.domain.model;

public enum CallStatus {

    Accept("승낙"),
    Deny("거절");

    private String callStatus;

    CallStatus(String callStatus) {
        this.callStatus = callStatus;
    }
}
