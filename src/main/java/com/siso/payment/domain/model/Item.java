package com.siso.payment.domain.model;

public enum Item {

    SUBSCRIPTION("구독");

    private final String paymentItem;

    Item(String paymentItem) {
        this.paymentItem = paymentItem;
    }

    public String getDescription() {
        return paymentItem;
    }
}
