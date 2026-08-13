package com.example.flashsale.repository;

import com.example.flashsale.domain.Product;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String productId);
    Iterable<Product> findAll();
}
