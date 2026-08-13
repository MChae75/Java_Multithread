package com.example.flashsale.repository;

import com.example.flashsale.domain.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String orderId);
    Iterable<Order> findByUserId(String userId);
    Iterable<Order> findByStatus(Order.OrderStatus status);
}
