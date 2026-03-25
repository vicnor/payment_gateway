package com.paymentgateway.provider.port.out;

import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;

public interface SaveAuthorizationOutboxEventPort {
    void saveSucceeded(ProviderAuthorizationSucceeded event);
    void saveFailed(ProviderAuthorizationFailed event);
    void saveActionRequired(ProviderActionRequired event);
}
