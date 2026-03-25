package com.paymentgateway.payment.infrastructure.kafka;

import com.paymentgateway.payment.api.port.in.CreatePaymentUseCase;
import com.paymentgateway.payment.api.port.in.GetPaymentUseCase;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationActionRequiredCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationFailedCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationSucceededCommand;
import com.paymentgateway.payment.infrastructure.SuperTest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class ProviderEventKafkaConsumerTest extends SuperTest {

    @MockitoBean
    HandleAuthorizationResultUseCase handleAuthorizationResultUseCase;

    @MockitoBean
    CreatePaymentUseCase createPaymentUseCase;

    @MockitoBean
    GetPaymentUseCase getPaymentUseCase;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Value("${payment.kafka.provider-events-topic:provider-events}")
    String topic;

    @Test
    void consume_ProviderAuthorizationSucceeded_callsHandleSucceeded() {
        UUID paymentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        String eventId = UUID.randomUUID().toString();
        String payload = """
                {"paymentId":"%s","attemptId":"%s","providerPaymentRef":"prov-ref-abc"}
                """.formatted(paymentId, attemptId);

        var record = new ProducerRecord<String, String>(topic, null, null, payload);
        record.headers().add(new RecordHeader("eventType", "ProviderAuthorizationSucceeded".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventId", eventId.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);

        var captor = ArgumentCaptor.forClass(AuthorizationSucceededCommand.class);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(handleAuthorizationResultUseCase).handleSucceeded(captor.capture()));

        assertThat(captor.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(captor.getValue().attemptId()).isEqualTo(attemptId);
        assertThat(captor.getValue().eventId()).isEqualTo(eventId);
        assertThat(captor.getValue().providerPaymentRef()).isEqualTo("prov-ref-abc");
    }

    @Test
    void consume_ProviderAuthorizationFailed_callsHandleFailed() {
        UUID paymentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        String eventId = UUID.randomUUID().toString();
        String payload = """
                {"paymentId":"%s","attemptId":"%s","failureCode":"CARD_DECLINED","failureReason":"Insufficient funds"}
                """.formatted(paymentId, attemptId);

        var record = new ProducerRecord<String, String>(topic, null, null, payload);
        record.headers().add(new RecordHeader("eventType", "ProviderAuthorizationFailed".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventId", eventId.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);

        var captor = ArgumentCaptor.forClass(AuthorizationFailedCommand.class);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(handleAuthorizationResultUseCase).handleFailed(captor.capture()));

        assertThat(captor.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(captor.getValue().attemptId()).isEqualTo(attemptId);
        assertThat(captor.getValue().eventId()).isEqualTo(eventId);
        assertThat(captor.getValue().failureCode()).isEqualTo("CARD_DECLINED");
        assertThat(captor.getValue().failureReason()).isEqualTo("Insufficient funds");
    }

    @Test
    void consume_ProviderActionRequired_callsHandleActionRequired() {
        UUID paymentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        String eventId = UUID.randomUUID().toString();
        String payload = """
                {"paymentId":"%s","attemptId":"%s","actionPayload":"{\\"type\\":\\"3ds\\"}"}
                """.formatted(paymentId, attemptId);

        var record = new ProducerRecord<String, String>(topic, null, null, payload);
        record.headers().add(new RecordHeader("eventType", "ProviderActionRequired".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventId", eventId.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);

        var captor = ArgumentCaptor.forClass(AuthorizationActionRequiredCommand.class);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                verify(handleAuthorizationResultUseCase).handleActionRequired(captor.capture()));

        assertThat(captor.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(captor.getValue().attemptId()).isEqualTo(attemptId);
        assertThat(captor.getValue().eventId()).isEqualTo(eventId);
        assertThat(captor.getValue().actionPayload()).isEqualTo("{\"type\":\"3ds\"}");
    }
}
