package com.paymentgateway.payment.infrastructure;

import com.paymentgateway.payment.infrastructure.persistence.OutboxEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PaymentServiceApplicationTest extends SuperTest {

    @MockitoBean
    OutboxEventPublisher publisher;

    @Test
    void contextLoads() {
    }
}
