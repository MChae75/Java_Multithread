package com.example.flashsale.repository.impl;

import com.example.flashsale.domain.Product;
import com.example.flashsale.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductRepository implements ProductRepository {

    private final ConcurrentHashMap<String, Product> storage = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        storage.put(product.getProductId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(storage.get(productId));
    }

    @Override
    public Iterable<Product> findAll() {
        return new HashMap<>(storage).values();
    }
}
