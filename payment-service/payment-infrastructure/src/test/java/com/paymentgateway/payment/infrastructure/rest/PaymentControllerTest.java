package com.paymentgateway.payment.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.payment.infrastructure.SuperTest;
import com.paymentgateway.payment.infrastructure.persistence.OutboxEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PaymentControllerTest extends SuperTest {

    @MockitoBean
    OutboxEventPublisher publisher;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createPayment_returnsCreatedPayment() throws Exception {
        var request = Map.of(
                "merchantId", "merchant-1",
                "orderId", "order-1",
                "amount", 1000,
                "currencyCode", "SEK",
                "captureMode", "IMMEDIATE",
                "provider", "stripe",
                "paymentMethod", Map.of("type", "CARD", "token", "tok_visa")
        );

        mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.merchantId").value("merchant-1"))
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.currencyCode").value("SEK"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void getPayment_afterCreating_returnsExpectedPayment() throws Exception {
        var request = Map.of(
                "merchantId", "merchant-2",
                "orderId", "order-2",
                "amount", 2500,
                "currencyCode", "USD",
                "captureMode", "IMMEDIATE",
                "provider", "stripe",
                "paymentMethod", Map.of("type", "CARD", "token", "tok_visa_2")
        );

        var createResult = mockMvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        var responseBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        var paymentId = responseBody.get("id").asText();

        mockMvc.perform(get("/api/v1/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId))
                .andExpect(jsonPath("$.merchantId").value("merchant-2"))
                .andExpect(jsonPath("$.orderId").value("order-2"))
                .andExpect(jsonPath("$.amount").value(2500))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
}
