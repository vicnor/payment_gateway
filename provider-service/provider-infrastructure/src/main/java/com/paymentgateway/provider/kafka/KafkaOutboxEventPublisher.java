package com.paymentgateway.provider.kafka;

import com.paymentgateway.provider.persistence.OutboxEventEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaOutboxEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaOutboxEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     @Value("${provider.kafka.results-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(OutboxEventEntity event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.getPaymentId(), event.getPayload());
        record.headers().add(new RecordHeader("eventId", event.getId().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventType", event.getEventType().getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("eventVersion", String.valueOf(event.getEventVersion()).getBytes(StandardCharsets.UTF_8)));
        try {
            kafkaTemplate.send(record).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while publishing outbox event " + event.getId(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to publish outbox event " + event.getId(), e.getCause());
        }
    }
}
