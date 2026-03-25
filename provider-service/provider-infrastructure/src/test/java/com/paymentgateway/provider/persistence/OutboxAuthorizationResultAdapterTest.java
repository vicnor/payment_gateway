package com.paymentgateway.provider.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.provider.domain.AuthorizationCode;
import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({JacksonAutoConfiguration.class, OutboxAuthorizationResultAdapter.class})
class OutboxAuthorizationResultAdapterTest {

    @Autowired
    OutboxAuthorizationResultAdapter adapter;

    @Autowired
    OutboxEventRepository repository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void saveSucceeded_persistsEntityWithExpectedFields() throws Exception {
        adapter.saveSucceeded(new ProviderAuthorizationSucceeded(
                "out-1", Instant.parse("2026-01-01T00:00:00Z"),
                "pay-1", "att-1", "stripe", "pi_fake_abc123"));

        OutboxEventEntity entity = repository.findById("out-1").orElseThrow();
        assertThat(entity.getId()).isEqualTo("out-1");
        assertThat(entity.getEventType()).isEqualTo(ProviderAuthorizationSucceeded.EVENT_TYPE);
        assertThat(entity.getPaymentId()).isEqualTo("pay-1");
        assertThat(entity.getEventVersion()).isEqualTo(1);
        assertThat(entity.getPublishedAt()).isNull();
        JsonNode json = objectMapper.readTree(entity.getPayload());
        assertThat(json.get("eventId").asText()).isEqualTo("out-1");
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-1");
        assertThat(json.get("providerPaymentRef").asText()).isEqualTo("pi_fake_abc123");
    }

    @Test
    void saveFailed_persistsEntityWithExpectedFields() throws Exception {
        adapter.saveFailed(new ProviderAuthorizationFailed(
                "out-2", Instant.parse("2026-01-01T00:00:00Z"),
                "pay-2", "att-2", "stripe",
                AuthorizationCode.INSUFFICIENT_FUNDS, "Insufficient funds"));

        OutboxEventEntity entity = repository.findById("out-2").orElseThrow();
        assertThat(entity.getId()).isEqualTo("out-2");
        assertThat(entity.getEventType()).isEqualTo(ProviderAuthorizationFailed.EVENT_TYPE);
        assertThat(entity.getPaymentId()).isEqualTo("pay-2");
        assertThat(entity.getEventVersion()).isEqualTo(1);
        assertThat(entity.getPublishedAt()).isNull();
        JsonNode json = objectMapper.readTree(entity.getPayload());
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-2");
        assertThat(json.get("failureCode").asText()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(json.get("failureReason").asText()).isEqualTo("Insufficient funds");
    }

    @Test
    void saveActionRequired_persistsEntityWithExpectedFields() throws Exception {
        adapter.saveActionRequired(new ProviderActionRequired(
                "out-3", Instant.parse("2026-01-01T00:00:00Z"),
                "pay-3", "att-3", "stripe", ""));

        OutboxEventEntity entity = repository.findById("out-3").orElseThrow();
        assertThat(entity.getId()).isEqualTo("out-3");
        assertThat(entity.getEventType()).isEqualTo(ProviderActionRequired.EVENT_TYPE);
        assertThat(entity.getPaymentId()).isEqualTo("pay-3");
        assertThat(entity.getEventVersion()).isEqualTo(1);
        assertThat(entity.getPublishedAt()).isNull();
        JsonNode json = objectMapper.readTree(entity.getPayload());
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-3");
        assertThat(json.get("actionPayload").asText()).isEqualTo("");
    }
}
