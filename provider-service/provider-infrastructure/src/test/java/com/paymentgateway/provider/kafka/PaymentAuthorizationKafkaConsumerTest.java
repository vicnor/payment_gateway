package com.paymentgateway.provider.kafka;

import com.paymentgateway.provider.SuperTest;
import com.paymentgateway.provider.domain.PaymentAuthorizationEvent;
import com.paymentgateway.provider.port.in.HandlePaymentAuthorizationUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@EmbeddedKafka(partitions = 1, topics = "${provider.kafka.topic:payment-requests}")
class PaymentAuthorizationKafkaConsumerTest extends SuperTest {

    @MockitoBean
    HandlePaymentAuthorizationUseCase useCase;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void consume_deserializesAndDelegatesEvent() throws Exception {
        String payload = """
                {"eventId":"evt-1","eventType":"PaymentAuthorizationRequested",
                 "occurredAt":"2026-01-01T00:00:00Z","paymentId":"pay-1",
                 "attemptId":"att-1","provider":"stripe","amount":1000,
                 "currency":"SEK","paymentMethodType":"CARD","paymentMethodRef":"tok_visa"}
                """;
        kafkaTemplate.send("payment-requests", payload).get();

        var captor = ArgumentCaptor.forClass(PaymentAuthorizationEvent.class);
        verify(useCase, timeout(5000)).authorize(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo("PaymentAuthorizationRequested");
        assertThat(captor.getValue().provider()).isEqualTo("stripe");
        assertThat(captor.getValue().amount()).isEqualTo(1000);
    }
}
