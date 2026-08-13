package com.example.flashsale;

import com.example.flashsale.domain.OrderRequest;
import com.example.flashsale.service.FlashSaleService;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlashSaleServiceTest {

    private final FlashSaleService flashSaleService = new FlashSaleService();

    @Test
    void placeOrder_shouldReduceStock_whenEnoughInventoryExists() {
        OrderRequest request = new OrderRequest("SKU-1001", 2, "user-1");

        var response = flashSaleService.placeOrder(request);

        assertEquals("ACCEPTED", response.get("status"));
        assertEquals(2, response.get("quantity"));
    }

    @Test
    void placeOrder_shouldFail_whenNotEnoughInventoryExists() {
        OrderRequest request = new OrderRequest("SKU-1001", 999, "user-2");

        assertThrows(IllegalStateException.class, () -> flashSaleService.placeOrder(request));
    }

    @Test
    void placeOrder_shouldPreventRaceCondition_underConcurrentRequests() throws InterruptedException {
        String productId = "SKU-2001";
        FlashSaleService concurrentService = new FlashSaleService(Map.of(productId, 100));

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    concurrentService.placeOrder(new OrderRequest(productId, 2, "user-" + userIndex));
                    successCount.incrementAndGet();
                } catch (IllegalStateException ignored) {
                    // expected when stock is exhausted
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        int remainingStock = concurrentService.getInventorySnapshot().getOrDefault(productId, 0);
        assertEquals(0, remainingStock);
        assertEquals(50, successCount.get());
    }
}
