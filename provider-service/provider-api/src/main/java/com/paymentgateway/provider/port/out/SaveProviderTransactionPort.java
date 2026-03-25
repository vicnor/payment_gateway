package com.paymentgateway.provider.port.out;

import com.paymentgateway.provider.domain.ProviderTransaction;

public interface SaveProviderTransactionPort {
    void save(ProviderTransaction transaction);
}
