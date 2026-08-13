package com.example.flashsale;

import com.example.flashsale.domain.Inventory;
import com.example.flashsale.service.PaymentRequest;
import com.example.flashsale.service.PaymentService;
import com.example.flashsale.service.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentServiceTest {

    private PaymentService paymentService;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(5);
        paymentService = new PaymentService(executorService);
    }

    @Test
    void testCreditCardPaymentInitiation() {
        PaymentRequest request = new PaymentRequest(
                "order-123",
                "user-1",
                new BigDecimal("1299.99"),
                "CREDIT_CARD"
        );

        PaymentResult result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals("order-123", result.getOrderId());
        assertEquals(PaymentResult.PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    void testWalletPaymentInitiation() {
        PaymentRequest request = new PaymentRequest(
                "order-456",
                "user-2",
                new BigDecimal("199.99"),
                "WALLET"
        );

        PaymentResult result = paymentService.processPayment(request);

        assertNotNull(result);
        assertEquals("order-456", result.getOrderId());
        assertEquals(PaymentResult.PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    void testPaymentStatusCheck() throws InterruptedException {
        PaymentRequest request = new PaymentRequest(
                "order-789",
                "user-3",
                new BigDecimal("299.99"),
                "DEBIT_CARD"
        );

        PaymentResult initialResult = paymentService.processPayment(request);
        String paymentId = initialResult.getPaymentId();

        // Wait for async processing
        Thread.sleep(500);

        PaymentResult statusCheck = paymentService.checkPaymentStatus(paymentId);
        assertNotNull(statusCheck);
    }
}
