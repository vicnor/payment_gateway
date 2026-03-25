package com.paymentgateway.provider.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.port.in.HandlePaymentAuthorizationUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuthorizationKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final HandlePaymentAuthorizationUseCase handlePaymentAuthorizationUseCase;

    public PaymentAuthorizationKafkaConsumer(ObjectMapper objectMapper,
                                             HandlePaymentAuthorizationUseCase handlePaymentAuthorizationUseCase) {
        this.objectMapper = objectMapper;
        this.handlePaymentAuthorizationUseCase = handlePaymentAuthorizationUseCase;
    }

    @KafkaListener(topics = "${provider.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) throws JsonProcessingException {
        PaymentAuthorizationEvent event = objectMapper.readValue(record.value(), PaymentAuthorizationEvent.class);
        handlePaymentAuthorizationUseCase.authorize(event);
    }
}
