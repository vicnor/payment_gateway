package com.paymentgateway.payment.api.domain;

import java.util.Currency;

public record Money(Integer amount, Currency currency) {

    public Money {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
    }

    public static Money of(Integer amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
}
