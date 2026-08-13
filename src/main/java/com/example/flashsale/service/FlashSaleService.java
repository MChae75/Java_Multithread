package com.example.flashsale.service;

import com.example.flashsale.domain.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlashSaleService {

    private final ConcurrentHashMap<String, Integer> inventory;

    @Autowired(required = false)
    private RedisLockService redisLockService;

    @Autowired(required = false)
    private OrderEventProducer orderEventProducer;

    public FlashSaleService() {
        this(Map.of("SKU-1001", 100, "SKU-1002", 50));
    }

    public FlashSaleService(Map<String, Integer> initialInventory) {
        this.inventory = new ConcurrentHashMap<>();
        this.inventory.putAll(initialInventory);
    }

    public Map<String, Object> placeOrder(OrderRequest request) {
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

        String productId = request.productId();
        return withInventoryLock(productId, () -> {
            int currentStock = inventory.getOrDefault(productId, 0);

            if (currentStock < request.quantity()) {
                throw new IllegalStateException("Not enough stock for product: " + productId);
            }

            int remainingStock = currentStock - request.quantity();
            inventory.put(productId, remainingStock);

            if (orderEventProducer != null) {
                orderEventProducer.publishOrderCreated(
                        Map.of(
                                "orderId", UUID.randomUUID().toString(),
                                "productId", productId,
                                "userId", request.userId(),
                                "quantity", request.quantity(),
                                "status", "QUEUED"
                        )
                );
            }

            return Map.of(
                    "orderId", UUID.randomUUID().toString(),
                    "status", "ACCEPTED",
                    "productId", productId,
                    "quantity", request.quantity(),
                    "remainingStock", remainingStock,
                    "userId", request.userId()
            );
        });
    }

    public Map<String, Integer> getInventorySnapshot() {
        synchronized (this) {
            return new java.util.HashMap<>(inventory);
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
