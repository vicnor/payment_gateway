package com.paymentgateway.payment.infrastructure.persistence;

import com.paymentgateway.payment.api.domain.WebhookDeliveryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WebhookOutboxJpaRepository extends JpaRepository<WebhookOutboxJpaEntity, UUID> {

    @Query("SELECT e FROM WebhookOutboxJpaEntity e WHERE e.status = :pending AND e.nextRetryAt <= :now ORDER BY e.nextRetryAt ASC")
    List<WebhookOutboxJpaEntity> findDue(@Param("now") Instant now,
                                         @Param("pending") WebhookDeliveryStatus pending,
                                         Pageable pageable);
}
