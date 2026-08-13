package com.example.flashsale.service;

import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    public void handleOrderCreated(String message) {
        System.out.println("[Kafka Consumer] processing order: " + message);
    }
}
