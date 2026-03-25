package com.paymentgateway.payment.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.payment.infrastructure.SuperTest;
import com.paymentgateway.payment.infrastructure.persistence.OutboxEventJpaEntity;
import com.paymentgateway.payment.infrastructure.persistence.OutboxEventJpaRepository;
import com.paymentgateway.payment.infrastructure.persistence.OutboxEventPublisher;
import com.paymentgateway.payment.infrastructure.persistence.OutboxEventRelayScheduler;
import com.paymentgateway.payment.infrastructure.persistence.PaymentAttemptJpaRepository;
import com.paymentgateway.payment.infrastructure.persistence.PaymentIdempotencyKeyJpaRepository;
import com.paymentgateway.payment.infrastructure.persistence.PaymentJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "outbox.relay.enabled=true",
        "outbox.relay.instance-id=test-instance",
        "outbox.relay.fixed-delay-ms=3600000"
})
class PaymentControllerTest extends SuperTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PaymentJpaRepository paymentJpaRepository;

    @Autowired
    PaymentAttemptJpaRepository paymentAttemptJpaRepository;

    @Autowired
    OutboxEventJpaRepository outboxEventJpaRepository;

    @Autowired
    PaymentIdempotencyKeyJpaRepository paymentIdempotencyKeyJpaRepository;

    @Autowired
    OutboxEventRelayScheduler scheduler;

    @AfterEach
    void cleanup() {
        outboxEventJpaRepository.deleteAll();
        paymentAttemptJpaRepository.deleteAll();
        paymentIdempotencyKeyJpaRepository.deleteAll();
        paymentJpaRepository.deleteAll();
    }

    @MockitoBean
    OutboxEventPublisher outboxEventPublisher;

    private static final String VALID_REQUEST = """
            {
                "merchantId": "merchant-1",
                "orderId": "order-1",
                "amount": 1000,
                "currencyCode": "SEK",
                "captureMode": "IMMEDIATE",
                "provider": "stripe",
                "paymentMethod": {
                    "type": "CARD",
                    "token": "tok_visa"
                }
            }
            """;

    @Test
    void createPayment_validRequest_returns201AndPersistsPaymentAttemptAndOutboxEvent() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        var result = mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.merchantId").value("merchant-1"))
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.currencyCode").value("SEK"))
                .andExpect(jsonPath("$.captureMode").value("IMMEDIATE"))
                .andExpect(jsonPath("$.provider").value("stripe"))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andReturn();

        UUID paymentId = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        assertThat(paymentJpaRepository.findById(paymentId)).isPresent();

        assertThat(paymentAttemptJpaRepository.findTopByPaymentIdOrderByCreatedAtDesc(paymentId)).isPresent();

        assertThat(outboxEventJpaRepository.findAll())
                .anyMatch(e -> "PaymentAuthorizationRequested".equals(e.getEventType())
                        && paymentId.equals(e.getAggregateId()));

        scheduler.relay();

        var captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(outboxEventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo("PaymentAuthorizationRequested");
        assertThat(captor.getValue().getAggregateId()).isEqualTo(paymentId);
        assertThat(captor.getValue().getPayload()).isNotBlank();
    }

    @Test
    void createPayment_missingIdempotencyKeyHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_invalidIdempotencyKey_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", "not-a-valid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPayment_missingRequiredFields_returns400() throws Exception {
        String incompleteRequest = """
                {
                    "amount": 1000,
                    "currencyCode": "SEK"
                }
                """;

        mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_existingPayment_returns200WithPaymentDetails() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        var createResult = mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andReturn();

        String paymentId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.merchantId").value("merchant-1"))
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.paymentMethod.type").value("CARD"));
    }

    @Test
    void getPayment_nonExistentPayment_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/payments/{paymentId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
