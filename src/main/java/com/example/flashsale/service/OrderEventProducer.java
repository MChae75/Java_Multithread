package com.example.flashsale.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderEventProducer {

    public void publishOrderCreated(Map<String, Object> orderEvent) {
        System.out.println("[Kafka Producer] order queued: " + orderEvent);
    }
}
