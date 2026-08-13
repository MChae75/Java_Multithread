package com.example.flashsale.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishOrderCreated(Map<String, Object> orderEvent) {
        try {
            String payload = objectMapper.writeValueAsString(orderEvent);
            kafkaTemplate.send("order-events", orderEvent.get("orderId").toString(), payload);
            System.out.println("[Kafka Producer] Order published: " + orderEvent.get("orderId"));
        } catch (Exception e) {
            System.err.println("[Kafka Producer] Failed to publish order event: " + e.getMessage());
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}
