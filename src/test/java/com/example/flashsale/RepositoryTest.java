package com.example.flashsale;

import com.example.flashsale.domain.Order;
import com.example.flashsale.repository.OrderRepository;
import com.example.flashsale.repository.impl.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {

    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new InMemoryOrderRepository();
    }

    @Test
    void testSaveAndFindOrder() {
        Order order = new Order("order-1", "user-1", "SKU-1001", 2, new BigDecimal("2599.98"));
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById("order-1");

        assertTrue(found.isPresent());
        assertEquals("order-1", found.get().getOrderId());
        assertEquals("user-1", found.get().getUserId());
    }

    @Test
    void testFindByUserId() {
        orderRepository.save(new Order("order-1", "user-1", "SKU-1001", 2, new BigDecimal("2599.98")));
        orderRepository.save(new Order("order-2", "user-1", "SKU-1002", 1, new BigDecimal("199.99")));
        orderRepository.save(new Order("order-3", "user-2", "SKU-1003", 1, new BigDecimal("299.99")));

        Iterable<Order> userOrders = orderRepository.findByUserId("user-1");
        
        int count = 0;
        for (Order order : userOrders) {
            count++;
            assertEquals("user-1", order.getUserId());
        }
        assertEquals(2, count);
    }

    @Test
    void testFindByStatus() {
        Order order1 = new Order("order-1", "user-1", "SKU-1001", 2, new BigDecimal("2599.98"));
        Order order2 = new Order("order-2", "user-2", "SKU-1002", 1, new BigDecimal("199.99"));
        
        orderRepository.save(order1);
        orderRepository.save(order2);
        
        order2.setStatus(Order.OrderStatus.CONFIRMED);
        orderRepository.save(order2);

        Iterable<Order> queuedOrders = orderRepository.findByStatus(Order.OrderStatus.QUEUED);
        Iterable<Order> confirmedOrders = orderRepository.findByStatus(Order.OrderStatus.CONFIRMED);

        int queuedCount = 0;
        for (Order ignored : queuedOrders) {
            queuedCount++;
        }
        assertEquals(1, queuedCount);

        int confirmedCount = 0;
        for (Order ignored : confirmedOrders) {
            confirmedCount++;
        }
        assertEquals(1, confirmedCount);
    }
}
