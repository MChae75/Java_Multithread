package com.example.flashsale.config;

import com.example.flashsale.entity.InventoryEntity;
import com.example.flashsale.entity.ProductEntity;
import com.example.flashsale.repository.jpa.InventoryJpaRepository;
import com.example.flashsale.repository.jpa.ProductJpaRepository;
import com.example.flashsale.repository.jpa.OrderJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ProductJpaRepository productRepo, InventoryJpaRepository inventoryRepo, OrderJpaRepository orderRepo) {
        return args -> {
            System.out.println("Clearing and seeding database with realistic products...");
            orderRepo.deleteAll();
            inventoryRepo.deleteAll();
            productRepo.deleteAll();

            ProductEntity p1 = new ProductEntity("SKU-1001", "Refurbished 15.6\" Gaming Laptop (Mystery Brand)", new BigDecimal("499.99"), "Might have a scratch, might run Crysis. Who knows?", 50);
                ProductEntity p2 = new ProductEntity("SKU-1002", "100-Pack AA Batteries", new BigDecimal("12.99"), "You will lose 90 of them in a drawer.", 500);
                ProductEntity p3 = new ProductEntity("SKU-1003", "Noise Cancelling Headphones", new BigDecimal("59.99"), "Perfect for ignoring your coworkers.", 150);
                ProductEntity p4 = new ProductEntity("SKU-1004", "Giant Inflatable T-Rex Costume", new BigDecimal("29.99"), "Be the life of the party, or get stuck in a doorway.", 25);
                ProductEntity p5 = new ProductEntity("SKU-1005", "Smart Rice Cooker", new BigDecimal("89.99"), "It cooks rice. It is smart. What else do you want?", 75);
                ProductEntity p6 = new ProductEntity("SKU-1006", "Woot! BOC (Bag of Crap)", new BigDecimal("10.00"), "Prepare for disappointment.", 5); // Very limited stock

                productRepo.saveAll(List.of(p1, p2, p3, p4, p5, p6));

                // Initialize inventory for all
                inventoryRepo.save(new InventoryEntity("SKU-1001", 50));
                inventoryRepo.save(new InventoryEntity("SKU-1002", 500));
                inventoryRepo.save(new InventoryEntity("SKU-1003", 150));
                inventoryRepo.save(new InventoryEntity("SKU-1004", 25));
                inventoryRepo.save(new InventoryEntity("SKU-1005", 75));
                inventoryRepo.save(new InventoryEntity("SKU-1006", 5));

                System.out.println("Database seeded successfully!");
        };
    }
}
