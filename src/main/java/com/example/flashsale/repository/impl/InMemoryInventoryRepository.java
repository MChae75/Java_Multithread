package com.example.flashsale.repository.impl;

import com.example.flashsale.domain.Inventory;
import com.example.flashsale.repository.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private final ConcurrentHashMap<String, Inventory> storage = new ConcurrentHashMap<>();

    @Override
    public Inventory save(Inventory inventory) {
        storage.put(inventory.getProductId(), inventory);
        return inventory;
    }

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return Optional.ofNullable(storage.get(productId));
    }

    @Override
    public Iterable<Inventory> findAll() {
        return new HashMap<>(storage).values();
    }
}
