package com.example.flashsale;

import com.example.flashsale.domain.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory("SKU-1001", 100);
    }

    @Test
    void testReserveSuccessfully() {
        boolean reserved = inventory.reserve(10);

        assertTrue(reserved);
        assertEquals(90, inventory.getAvailableQuantity());
        assertEquals(10, inventory.getReservedQuantity());
    }

    @Test
    void testReserveFailsWhenInsufficientStock() {
        boolean reserved = inventory.reserve(150);

        assertFalse(reserved);
        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void testConfirmSale() {
        inventory.reserve(10);
        inventory.confirmSale(10);

        assertEquals(90, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(10, inventory.getSoldQuantity());
    }

    @Test
    void testReleaseReservation() {
        inventory.reserve(10);
        inventory.releaseReservation(10);

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void testConcurrentReservations() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    if (inventory.reserve(2)) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(50, successCount.get());
        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(100, inventory.getReservedQuantity());
    }
}
