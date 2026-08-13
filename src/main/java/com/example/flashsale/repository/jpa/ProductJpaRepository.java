package com.example.flashsale.repository.jpa;

import com.example.flashsale.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
    ProductEntity findByName(String name);
}
