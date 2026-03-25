package com.paymentgateway.provider.domain;

/**
 * @param status             outcome of the authorization attempt
 * @param code               decline/action code for DECLINED and REQUIRES_ACTION, null for AUTHORIZED
 * @param providerPaymentRef provider-assigned payment reference, non-null for AUTHORIZED only
 * @param cardDetails        card details returned by the provider, null for REQUIRES_ACTION
 */
public record AuthorizationResult(AuthorizationStatus status, AuthorizationCode code,
                                  String providerPaymentRef, CardDetails cardDetails) {

    public static AuthorizationResult authorized(String providerPaymentRef, CardDetails cardDetails) {
        return new AuthorizationResult(AuthorizationStatus.AUTHORIZED, null, providerPaymentRef, cardDetails);
    }

    public static AuthorizationResult declined(AuthorizationCode code, CardDetails cardDetails) {
        return new AuthorizationResult(AuthorizationStatus.DECLINED, code, null, cardDetails);
    }

    public static AuthorizationResult requiresAction(AuthorizationCode code) {
        return new AuthorizationResult(AuthorizationStatus.REQUIRES_ACTION, code, null, null);
    }
}
