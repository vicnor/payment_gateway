package com.paymentgateway.payment.infrastructure.persistence;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaOutboxEventPublisher implements OutboxEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaOutboxEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${outbox.relay.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OutboxEventJpaEntity event) {
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8));
        headers.add("eventVersion", String.valueOf(event.getEventVersion()).getBytes(StandardCharsets.UTF_8));
        headers.add("eventId", String.valueOf(event.getEventId()).getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, event.getAggregateId().toString(), event.getPayload(), headers);
        try {
            kafkaTemplate.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while publishing outbox event " + event.getEventId(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to publish outbox event " + event.getEventId(), e.getCause());
        }
    }
}
