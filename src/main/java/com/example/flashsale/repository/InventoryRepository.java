package com.example.flashsale.repository;

import com.example.flashsale.domain.Inventory;

import java.util.Optional;

public interface InventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findByProductId(String productId);
    Iterable<Inventory> findAll();
}
