package com.spring.soft.ordermanagement.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer {

    @KafkaListener(topics = "user-events", groupId = "order-service-group")
    public void consumeUserEvent(String message) {
        log.info("Received user event: {}", message);
    }
}