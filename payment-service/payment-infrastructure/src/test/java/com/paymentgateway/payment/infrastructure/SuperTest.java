package com.paymentgateway.payment.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = "${payment.kafka.provider-events-topic:provider-events}")
public abstract class SuperTest {
}
