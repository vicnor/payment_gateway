package com.paymentgateway.provider.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderTransactionRepository extends JpaRepository<ProviderTransactionEntity, String> {}
