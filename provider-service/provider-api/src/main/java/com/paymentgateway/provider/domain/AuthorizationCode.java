package com.paymentgateway.provider.domain;

public enum AuthorizationCode {
    CARD_DECLINED,
    INSUFFICIENT_FUNDS,
    THREE_DS_REQUIRED;

    public String description() {
        return switch (this) {
            case CARD_DECLINED      -> "Card was declined";
            case INSUFFICIENT_FUNDS -> "Insufficient funds";
            case THREE_DS_REQUIRED  -> "3DS authentication required";
        };
    }
}
