package com.paymentgateway.provider.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.provider.SuperTest;
import com.paymentgateway.provider.domain.ProviderActionRequired;
import com.paymentgateway.provider.domain.ProviderAuthorizationFailed;
import com.paymentgateway.provider.domain.ProviderAuthorizationSucceeded;
import com.paymentgateway.provider.persistence.OutboxEventEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(partitions = 1, topics = "${provider.kafka.results-topic:provider-events}")
@Import(KafkaOutboxEventPublisherTest.TestResultConsumer.class)
class KafkaOutboxEventPublisherTest extends SuperTest {

    @Autowired
    KafkaOutboxEventPublisher publisher;

    @Autowired
    TestResultConsumer testConsumer;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void publish_succeeded_sendsJsonWithExpectedFields() throws Exception {
        OutboxEventEntity entity = new OutboxEventEntity(
                "out-1",
                ProviderAuthorizationSucceeded.EVENT_TYPE,
                "pay-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                objectMapper.writeValueAsString(new ProviderAuthorizationSucceeded(
                        "out-1", Instant.parse("2026-01-01T00:00:00Z"),
                        "pay-1", "att-1", "stripe", "pi_fake_abc123")),
                1);

        publisher.publish(entity);

        ConsumerRecord<String, String> record = testConsumer.records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("pay-1");
        assertThat(header(record, "eventId")).isEqualTo("out-1");
        assertThat(header(record, "eventType")).isEqualTo("ProviderAuthorizationSucceeded");
        assertThat(header(record, "eventVersion")).isEqualTo("1");
        JsonNode json = objectMapper.readTree(record.value());
        assertThat(json.get("eventId").asText()).isEqualTo("out-1");
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-1");
        assertThat(json.get("attemptId").asText()).isEqualTo("att-1");
        assertThat(json.get("provider").asText()).isEqualTo("stripe");
        assertThat(json.get("providerPaymentRef").asText()).isEqualTo("pi_fake_abc123");
    }

    @Test
    void publish_failed_sendsJsonWithExpectedFields() throws Exception {
        OutboxEventEntity entity = new OutboxEventEntity(
                "out-2",
                ProviderAuthorizationFailed.EVENT_TYPE,
                "pay-2",
                Instant.parse("2026-01-01T00:00:00Z"),
                objectMapper.writeValueAsString(new ProviderAuthorizationFailed(
                        "out-2", Instant.parse("2026-01-01T00:00:00Z"),
                        "pay-2", "att-2", "stripe",
                        com.paymentgateway.provider.domain.AuthorizationCode.INSUFFICIENT_FUNDS,
                        "Insufficient funds")),
                1);

        publisher.publish(entity);

        ConsumerRecord<String, String> record = testConsumer.records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("pay-2");
        assertThat(header(record, "eventId")).isEqualTo("out-2");
        assertThat(header(record, "eventType")).isEqualTo("ProviderAuthorizationFailed");
        assertThat(header(record, "eventVersion")).isEqualTo("1");
        JsonNode json = objectMapper.readTree(record.value());
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-2");
        assertThat(json.get("failureCode").asText()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(json.get("failureReason").asText()).isEqualTo("Insufficient funds");
    }

    @Test
    void publish_actionRequired_sendsJsonWithExpectedFields() throws Exception {
        OutboxEventEntity entity = new OutboxEventEntity(
                "out-3",
                ProviderActionRequired.EVENT_TYPE,
                "pay-3",
                Instant.parse("2026-01-01T00:00:00Z"),
                objectMapper.writeValueAsString(new ProviderActionRequired(
                        "out-3", Instant.parse("2026-01-01T00:00:00Z"),
                        "pay-3", "att-3", "stripe", "")),
                1);

        publisher.publish(entity);

        ConsumerRecord<String, String> record = testConsumer.records.poll(5, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("pay-3");
        assertThat(header(record, "eventId")).isEqualTo("out-3");
        assertThat(header(record, "eventType")).isEqualTo("ProviderActionRequired");
        assertThat(header(record, "eventVersion")).isEqualTo("1");
        JsonNode json = objectMapper.readTree(record.value());
        assertThat(json.get("paymentId").asText()).isEqualTo("pay-3");
        assertThat(json.get("actionPayload").asText()).isEqualTo("");
    }

    private String header(ConsumerRecord<?, ?> record, String key) {
        var header = record.headers().lastHeader(key);
        assertThat(header).as("header '%s'", key).isNotNull();
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    @Component
    static class TestResultConsumer {
        final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

        @KafkaListener(topics = "${provider.kafka.results-topic:provider-events}",
                groupId = "test-outbox-consumer")
        void consume(ConsumerRecord<String, String> record) {
            records.add(record);
        }
    }
}
