package com.spring.soft.usermanagement.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    public void consumeOrderEvent(String message) {
        log.info("Received order event: {}", message);
    }
}
