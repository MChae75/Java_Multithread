package com.example.flashsale.repository.impl;

import com.example.flashsale.domain.Order;
import com.example.flashsale.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(storage.get(orderId));
    }

    @Override
    public Iterable<Order> findByUserId(String userId) {
        return new HashMap<>(storage).values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Order> findByStatus(Order.OrderStatus status) {
        return new HashMap<>(storage).values().stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());
    }
}
