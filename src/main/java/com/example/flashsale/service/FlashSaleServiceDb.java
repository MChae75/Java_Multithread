package com.example.flashsale.service;

import com.example.flashsale.domain.Order;
import com.example.flashsale.domain.OrderRequest;
import com.example.flashsale.entity.OrderEntity;
import com.example.flashsale.entity.InventoryEntity;
import com.example.flashsale.repository.jpa.OrderJpaRepository;
import com.example.flashsale.repository.jpa.InventoryJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.example.flashsale.domain.ProductResponse;
import com.example.flashsale.entity.ProductEntity;
import com.example.flashsale.repository.jpa.ProductJpaRepository;

/**
 * Enhanced Flash Sale Service with Database Support
 * Integrates JPA repositories for order and inventory management
 */
@Service
public class FlashSaleServiceDb {

    private final ConcurrentHashMap<String, Integer> inventory;
    
    @Autowired(required = false)
    private RedisLockService redisLockService;

    @Autowired(required = false)
    private OrderEventProducer orderEventProducer;

    @Autowired(required = false)
    private OrderJpaRepository orderJpaRepository;

    @Autowired(required = false)
    private InventoryJpaRepository inventoryJpaRepository;

    @Autowired(required = false)
    private ProductJpaRepository productJpaRepository;

    public FlashSaleServiceDb() {
        this(Map.of("SKU-1001", 100, "SKU-1002", 50, "SKU-1003", 75));
    }

    public FlashSaleServiceDb(Map<String, Integer> initialInventory) {
        this.inventory = new ConcurrentHashMap<>();
        this.inventory.putAll(initialInventory);
    }

    /**
     * Place an order with inventory validation and database persistence
     */
    @Transactional
    public Map<String, Object> placeOrder(OrderRequest request) {
        validateOrderRequest(request);
        String productId = request.productId();

        return withInventoryLock(productId, () -> {
            // Get current stock
            int currentStock = inventory.getOrDefault(productId, 0);

            // Check if inventory from database exists
            InventoryEntity dbInventory = inventoryJpaRepository != null ?
                    inventoryJpaRepository.findById(productId).orElse(null) : null;

            if (dbInventory != null) {
                currentStock = dbInventory.getAvailableQuantity();
            }

            if (currentStock < request.quantity()) {
                throw new IllegalStateException("Not enough stock for product: " + productId);
            }

            // Deduct from in-memory inventory
            int remainingStock = currentStock - request.quantity();
            inventory.put(productId, remainingStock);

            // Update database inventory
            if (dbInventory != null && inventoryJpaRepository != null) {
                dbInventory.reserve(request.quantity());
                inventoryJpaRepository.save(dbInventory);
            }

            // Create order entity
            String orderId = UUID.randomUUID().toString();
            OrderEntity orderEntity = new OrderEntity(
                    orderId,
                    request.userId(),
                    productId,
                    request.quantity(),
                    new java.math.BigDecimal("0.00") // Price will be set later
            );
            orderEntity.setStatus(Order.OrderStatus.QUEUED);

            // Persist order to database
            if (orderJpaRepository != null) {
                orderJpaRepository.save(orderEntity);
            }

            // Publish to Kafka
            if (orderEventProducer != null) {
                orderEventProducer.publishOrderCreated(
                        Map.of(
                                "orderId", orderId,
                                "productId", productId,
                                "userId", request.userId(),
                                "quantity", request.quantity(),
                                "status", "QUEUED"
                        )
                );
            }

            return Map.of(
                    "orderId", orderId,
                    "status", "ACCEPTED",
                    "productId", productId,
                    "quantity", request.quantity(),
                    "remainingStock", remainingStock,
                    "userId", request.userId()
            );
        });
    }

    /**
     * Get current inventory snapshot
     */
    public Map<String, Integer> getInventorySnapshot() {
        synchronized (this) {
            return new HashMap<>(inventory);
        }
    }

    /**
     * Get order history for a user
     */
    public java.util.List<OrderEntity> getUserOrders(String userId) {
        if (orderJpaRepository != null) {
            return orderJpaRepository.findByUserId(userId);
        }
        return new java.util.ArrayList<>();
    }

    /**
     * Get orders by status
     */
    public java.util.List<OrderEntity> getOrdersByStatus(Order.OrderStatus status) {
        if (orderJpaRepository != null) {
            return orderJpaRepository.findByStatus(status);
        }
        return new java.util.ArrayList<>();
    }

    /**
     * Get all orders
     */
    public java.util.List<OrderEntity> getAllOrders() {
        if (orderJpaRepository != null) {
            return orderJpaRepository.findAll();
        }
        return new java.util.ArrayList<>();
    }

    /**
     * Delete order
     */
    @Transactional
    public void deleteOrder(String orderId) {
        if (orderJpaRepository != null) {
            orderJpaRepository.deleteById(orderId);
        }
    }

    /**
     * Update order status
     */
    @Transactional
    public void updateOrderStatus(String orderId, Order.OrderStatus newStatus) {
        if (orderJpaRepository != null) {
            orderJpaRepository.findById(orderId).ifPresent(order -> {
                order.setStatus(newStatus);
                orderJpaRepository.save(order);
            });
        }
    }

    /**
     * Get inventory from database
     */
    public InventoryEntity getInventory(String productId) {
        if (inventoryJpaRepository != null) {
            return inventoryJpaRepository.findById(productId).orElse(null);
        }
        return null;
    }

    /**
     * Get all products with inventory
     */
    public List<ProductResponse> getAllProducts() {
        if (productJpaRepository == null) return new ArrayList<>();
        
        List<ProductEntity> products = productJpaRepository.findAll();
        List<ProductResponse> responses = new ArrayList<>();
        
        for (ProductEntity p : products) {
            int stock = 0;
            if (inventoryJpaRepository != null) {
                InventoryEntity inv = inventoryJpaRepository.findById(p.getProductId()).orElse(null);
                if (inv != null) {
                    stock = inv.getAvailableQuantity();
                }
            } else {
                stock = inventory.getOrDefault(p.getProductId(), 0);
            }
            
            // Generate a fake original price (+20% or flat +$30)
            java.math.BigDecimal originalPrice = p.getPrice().add(new java.math.BigDecimal("30.00"));
            
            responses.add(new ProductResponse(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                originalPrice,
                p.getPrice(),
                stock
            ));
        }
        return responses;
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request cannot be null");
        }
        if (request.productId() == null || request.productId().isBlank()) {
            throw new IllegalArgumentException("Product id is required");
        }
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private <T> T withInventoryLock(String productId, java.util.function.Supplier<T> action) {
        if (redisLockService != null) {
            return redisLockService.withLock("inventory:" + productId, action);
        }
        synchronized (this) {
            return action.get();
        }
    }
}
