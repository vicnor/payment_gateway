package com.paymentgateway.provider.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.publishedAt IS NULL AND e.claimedAt IS NULL ORDER BY e.occurredAt ASC")
    List<OutboxEventEntity> findClaimable(Pageable pageable);

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.claimedAt = :claimedAt, e.claimedBy = :claimedBy WHERE e.id IN :ids AND e.claimedAt IS NULL")
    int claim(@Param("ids") List<String> ids, @Param("claimedAt") Instant claimedAt, @Param("claimedBy") String claimedBy);

    List<OutboxEventEntity> findByClaimedByAndPublishedAtIsNull(String claimedBy);

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.publishedAt = :publishedAt WHERE e.id = :id")
    void acknowledge(@Param("id") String id, @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Query("UPDATE OutboxEventEntity e SET e.claimedAt = NULL, e.claimedBy = NULL WHERE e.claimedAt < :staleThreshold AND e.publishedAt IS NULL")
    int unclaimStale(@Param("staleThreshold") Instant staleThreshold);
}
