package com.example.flashsale;

import com.example.flashsale.entity.OrderEntity;
import com.example.flashsale.entity.ProductEntity;
import com.example.flashsale.entity.InventoryEntity;
import com.example.flashsale.domain.Order;
import com.example.flashsale.repository.jpa.OrderJpaRepository;
import com.example.flashsale.repository.jpa.ProductJpaRepository;
import com.example.flashsale.repository.jpa.InventoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class JpaRepositoryIntegrationTest {

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private InventoryJpaRepository inventoryJpaRepository;

    @BeforeEach
    void setUp() {
        orderJpaRepository.deleteAll();
        inventoryJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        
        // Create sample products for testing
        productJpaRepository.save(new ProductEntity(
                "SKU-1001",
                "Premium Laptop",
                new BigDecimal("1299.99"),
                "High-performance laptop",
                100
        ));
        productJpaRepository.save(new ProductEntity(
                "SKU-1002",
                "Wireless Headphones",
                new BigDecimal("199.99"),
                "Noise-cancelling headphones",
                50
        ));
        productJpaRepository.save(new ProductEntity(
                "SKU-1003",
                "Smart Watch",
                new BigDecimal("299.99"),
                "Fitness tracking smartwatch",
                75
        ));
    }

    @Test
    void testSaveAndRetrieveProduct() {
        ProductEntity product = new ProductEntity(
                "SKU-1001",
                "Premium Laptop",
                new BigDecimal("1299.99"),
                "High-performance laptop",
                100
        );

        productJpaRepository.save(product);

        Optional<ProductEntity> retrieved = productJpaRepository.findById("SKU-1001");
        assertTrue(retrieved.isPresent());
        assertEquals("Premium Laptop", retrieved.get().getName());
        assertEquals(new BigDecimal("1299.99"), retrieved.get().getPrice());
    }

    @Test
    void testSaveAndRetrieveOrder() {
        OrderEntity order = new OrderEntity(
                "order-123",
                "user-1",
                "SKU-1001",
                2,
                new BigDecimal("2599.98")
        );

        orderJpaRepository.save(order);

        Optional<OrderEntity> retrieved = orderJpaRepository.findById("order-123");
        assertTrue(retrieved.isPresent());
        assertEquals("user-1", retrieved.get().getUserId());
        assertEquals("SKU-1001", retrieved.get().getProductId());
        assertEquals(2, retrieved.get().getQuantity());
    }

    @Test
    void testSaveAndRetrieveInventory() {
        InventoryEntity inventory = new InventoryEntity("SKU-1001", 100);

        inventoryJpaRepository.save(inventory);

        Optional<InventoryEntity> retrieved = inventoryJpaRepository.findById("SKU-1001");
        assertTrue(retrieved.isPresent());
        assertEquals(100, retrieved.get().getAvailableQuantity());
        assertEquals(0, retrieved.get().getReservedQuantity());
        assertEquals(0, retrieved.get().getSoldQuantity());
    }

    @Test
    void testFindOrdersByUserId() {
        orderJpaRepository.save(new OrderEntity("order-1", "user-1", "SKU-1001", 1, BigDecimal.ONE));
        orderJpaRepository.save(new OrderEntity("order-2", "user-1", "SKU-1002", 2, BigDecimal.TEN));
        orderJpaRepository.save(new OrderEntity("order-3", "user-2", "SKU-1003", 1, BigDecimal.ONE));

        List<OrderEntity> user1Orders = orderJpaRepository.findByUserId("user-1");
        List<OrderEntity> user2Orders = orderJpaRepository.findByUserId("user-2");

        assertEquals(2, user1Orders.size());
        assertEquals(1, user2Orders.size());
        assertTrue(user1Orders.stream().allMatch(o -> o.getUserId().equals("user-1")));
    }

    @Test
    void testFindOrdersByStatus() {
        OrderEntity order1 = new OrderEntity("order-1", "user-1", "SKU-1001", 1, BigDecimal.ONE);
        order1.setStatus(Order.OrderStatus.QUEUED);

        OrderEntity order2 = new OrderEntity("order-2", "user-2", "SKU-1002", 1, BigDecimal.TEN);
        order2.setStatus(Order.OrderStatus.CONFIRMED);

        orderJpaRepository.save(order1);
        orderJpaRepository.save(order2);

        List<OrderEntity> queuedOrders = orderJpaRepository.findByStatus(Order.OrderStatus.QUEUED);
        List<OrderEntity> confirmedOrders = orderJpaRepository.findByStatus(Order.OrderStatus.CONFIRMED);

        assertEquals(1, queuedOrders.size());
        assertEquals(1, confirmedOrders.size());
        assertEquals(Order.OrderStatus.QUEUED, queuedOrders.get(0).getStatus());
        assertEquals(Order.OrderStatus.CONFIRMED, confirmedOrders.get(0).getStatus());
    }

    @Test
    void testInventoryReservation() {
        InventoryEntity inventory = new InventoryEntity("SKU-1001", 100);
        inventoryJpaRepository.save(inventory);

        // Reserve
        InventoryEntity retrieved = inventoryJpaRepository.findById("SKU-1001").get();
        boolean reserved = retrieved.reserve(30);

        assertTrue(reserved);
        assertEquals(70, retrieved.getAvailableQuantity());
        assertEquals(30, retrieved.getReservedQuantity());

        inventoryJpaRepository.save(retrieved);

        // Verify in database
        InventoryEntity verified = inventoryJpaRepository.findById("SKU-1001").get();
        assertEquals(70, verified.getAvailableQuantity());
        assertEquals(30, verified.getReservedQuantity());
    }

    @Test
    void testFindProductByName() {
        productJpaRepository.save(new ProductEntity(
                "SKU-1001",
                "Premium Laptop",
                new BigDecimal("1299.99"),
                "High-performance laptop",
                100
        ));

        ProductEntity found = productJpaRepository.findByName("Premium Laptop");

        assertNotNull(found);
        assertEquals("SKU-1001", found.getProductId());
    }

    @Test
    void testUpdateOrderStatus() {
        OrderEntity order = new OrderEntity("order-1", "user-1", "SKU-1001", 1, BigDecimal.ONE);
        orderJpaRepository.save(order);

        OrderEntity retrieved = orderJpaRepository.findById("order-1").get();
        retrieved.setStatus(Order.OrderStatus.CONFIRMED);
        orderJpaRepository.save(retrieved);

        OrderEntity verified = orderJpaRepository.findById("order-1").get();
        assertEquals(Order.OrderStatus.CONFIRMED, verified.getStatus());
    }
}
