package com.paymentgateway.payment.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("SELECT e FROM OutboxEventJpaEntity e WHERE e.publishedAt IS NULL AND e.claimedAt IS NULL ORDER BY e.occurredAt ASC")
    List<OutboxEventJpaEntity> findClaimable(Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxEventJpaEntity e SET e.claimedAt = :claimedAt, e.claimedBy = :claimedBy WHERE e.eventId IN :ids AND e.claimedAt IS NULL")
    int claim(@Param("ids") List<UUID> ids, @Param("claimedAt") Instant claimedAt, @Param("claimedBy") String claimedBy);

    List<OutboxEventJpaEntity> findByClaimedByAndPublishedAtIsNull(String claimedBy);

    @Modifying
    @Query("UPDATE OutboxEventJpaEntity e SET e.publishedAt = :publishedAt WHERE e.eventId = :eventId")
    void acknowledge(@Param("eventId") UUID eventId, @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Query("UPDATE OutboxEventJpaEntity e SET e.claimedAt = NULL, e.claimedBy = NULL WHERE e.claimedAt < :staleThreshold AND e.publishedAt IS NULL")
    int unclaimStale(@Param("staleThreshold") Instant staleThreshold);
}
