package com.siso.payment.domain.model;

public enum PaymentStatus {

    PENDING("보류 중"),
    SUCCESS("성공"),
    FAILED("실패"),
    REFUNDED("환불");

    private final String paymentDescription;

    PaymentStatus(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }

    public String getDescription() {
        return paymentDescription;
    }
}