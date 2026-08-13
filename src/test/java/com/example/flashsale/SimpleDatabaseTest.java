package com.example.flashsale;

import com.example.flashsale.entity.ProductEntity;
import com.example.flashsale.repository.jpa.ProductJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
public class SimpleDatabaseTest {

    @Autowired
    private ProductJpaRepository productRepository;

    @Test
    void testProductRepositoryInjection() {
        assertNotNull(productRepository);
    }

    @Test
    void testSaveProduct() {
        ProductEntity product = new ProductEntity(
                "TEST-001",
                "Test Product",
                new BigDecimal("99.99"),
                "A test product",
                10
        );

        ProductEntity saved = productRepository.save(product);
        assertNotNull(saved);
        assertEquals("TEST-001", saved.getProductId());
        assertEquals("Test Product", saved.getName());
    }

    @Test
    void testFindProduct() {
        ProductEntity product = new ProductEntity(
                "TEST-002",
                "Test Product 2",
                new BigDecimal("199.99"),
                "Another test product",
                20
        );

        productRepository.save(product);

        assertTrue(productRepository.existsById("TEST-002"));
    }
}
