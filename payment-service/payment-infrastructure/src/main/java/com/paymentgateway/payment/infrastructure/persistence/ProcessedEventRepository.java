package com.paymentgateway.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
    boolean existsByEventId(String eventId);
}
