package com.paymentgateway.payment.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationActionRequiredCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationFailedCommand;
import com.paymentgateway.payment.api.port.in.HandleAuthorizationResultUseCase.AuthorizationSucceededCommand;
import com.paymentgateway.payment.infrastructure.kafka.dto.CardDetailsDto;
import com.paymentgateway.payment.infrastructure.kafka.dto.ProviderActionRequiredDto;
import com.paymentgateway.payment.infrastructure.kafka.dto.ProviderAuthorizationFailedDto;
import com.paymentgateway.payment.infrastructure.kafka.dto.ProviderAuthorizationSucceededDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class ProviderEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProviderEventKafkaConsumer.class);

    private final HandleAuthorizationResultUseCase useCase;
    private final ObjectMapper objectMapper;

    public ProviderEventKafkaConsumer(HandleAuthorizationResultUseCase useCase, ObjectMapper objectMapper) {
        this.useCase = useCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${payment.kafka.provider-events-topic}", groupId = "payment-service")
    void consume(ConsumerRecord<String, String> record) {
        try {
            String eventType = header(record, "eventType");
            String eventId = header(record, "eventId");
            switch (eventType) {
                case "ProviderAuthorizationSucceeded" -> {
                    var dto = objectMapper.readValue(record.value(), ProviderAuthorizationSucceededDto.class);
                    CardDetailsDto cd = dto.cardDetails();
                    useCase.handleSucceeded(new AuthorizationSucceededCommand(
                            UUID.fromString(dto.paymentId()), UUID.fromString(dto.attemptId()),
                            eventId, dto.providerPaymentRef(),
                            cd != null ? cd.brand() : null,
                            cd != null ? cd.last4() : null,
                            cd != null ? cd.expiryMonth() : null,
                            cd != null ? cd.expiryYear() : null));
                }
                case "ProviderAuthorizationFailed" -> {
                    var dto = objectMapper.readValue(record.value(), ProviderAuthorizationFailedDto.class);
                    CardDetailsDto cd = dto.cardDetails();
                    useCase.handleFailed(new AuthorizationFailedCommand(
                            UUID.fromString(dto.paymentId()), UUID.fromString(dto.attemptId()),
                            eventId, dto.failureCode(), dto.failureReason(),
                            cd != null ? cd.brand() : null,
                            cd != null ? cd.last4() : null,
                            cd != null ? cd.expiryMonth() : null,
                            cd != null ? cd.expiryYear() : null));
                }
                case "ProviderActionRequired" -> {
                    var dto = objectMapper.readValue(record.value(), ProviderActionRequiredDto.class);
                    useCase.handleActionRequired(new AuthorizationActionRequiredCommand(
                            UUID.fromString(dto.paymentId()), UUID.fromString(dto.attemptId()),
                            eventId, dto.actionPayload()));
                }
                default -> log.warn("Unknown provider event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process provider event from topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset(), e);
            throw new RuntimeException(e);
        }
    }

    private String header(ConsumerRecord<?, ?> record, String key) {
        Header header = record.headers().lastHeader(key);
        if (header == null) {
            throw new IllegalArgumentException("Missing required header: " + key);
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
