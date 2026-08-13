package com.example.flashsale.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final RedisLockService redisLockService;

    public OrderEventConsumer(ObjectMapper objectMapper, RedisLockService redisLockService) {
        this.objectMapper = objectMapper;
        this.redisLockService = redisLockService;
    }

    @KafkaListener(topics = "order-events", groupId = "order-processing-group")
    public void handleOrderCreated(String message) {
        try {
            Map<String, Object> orderEvent = objectMapper.readValue(message, Map.class);
            String orderId = (String) orderEvent.get("orderId");
            String productId = (String) orderEvent.get("productId");

            System.out.println("[Kafka Consumer] Processing order: " + orderId);

            // Acquire distributed lock for the product
            redisLockService.withLock("process:" + orderId, () -> {
                // Process order: deduct inventory, update order status in DB, etc.
                System.out.println("[Kafka Consumer] Order " + orderId + " processed successfully");
                return null;
            });
        } catch (Exception e) {
            System.err.println("[Kafka Consumer] Failed to process order event: " + e.getMessage());
            throw new RuntimeException("Failed to process order event", e);
        }
    }
}
