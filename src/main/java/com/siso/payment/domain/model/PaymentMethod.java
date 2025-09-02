package com.siso.payment.domain.model;

public enum PaymentMethod {

    TOSS("토스"),
    KAKAOPAY("카카오 페이");

    private final String paymentMethod;

    PaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return paymentMethod;
    }
}
