package com.example.flashsale.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class PaymentService {

    private final ExecutorService orderProcessingExecutor;
    private final ConcurrentHashMap<String, PaymentResult> paymentCache;

    public PaymentService(ExecutorService orderProcessingExecutor) {
        this.orderProcessingExecutor = orderProcessingExecutor;
        this.paymentCache = new ConcurrentHashMap<>();
    }

    /**
     * Process payment asynchronously using different payment methods
     */
    public PaymentResult processPayment(PaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();

        // Process payment asynchronously
        orderProcessingExecutor.submit(() -> {
            PaymentResult result;
            try {
                if ("CREDIT_CARD".equalsIgnoreCase(request.getPaymentMethod())) {
                    result = processCreditCardPayment(paymentId, request);
                } else if ("DEBIT_CARD".equalsIgnoreCase(request.getPaymentMethod())) {
                    result = processDebitCardPayment(paymentId, request);
                } else if ("WALLET".equalsIgnoreCase(request.getPaymentMethod())) {
                    result = processWalletPayment(paymentId, request);
                } else {
                    result = new PaymentResult(paymentId, request.getOrderId(), 
                            PaymentResult.PaymentStatus.FAILED, 
                            "Unsupported payment method: " + request.getPaymentMethod());
                }
                paymentCache.put(paymentId, result);
                System.out.println("[Payment Service] Payment " + paymentId + " result: " + result);
            } catch (Exception e) {
                PaymentResult errorResult = new PaymentResult(paymentId, request.getOrderId(),
                        PaymentResult.PaymentStatus.FAILED, "Payment processing error: " + e.getMessage());
                paymentCache.put(paymentId, errorResult);
            }
        });

        // Return pending result immediately
        return new PaymentResult(paymentId, request.getOrderId(), 
                PaymentResult.PaymentStatus.PENDING, "Payment processing initiated");
    }

    private PaymentResult processCreditCardPayment(String paymentId, PaymentRequest request) {
        // Simulate payment processing
        try {
            Thread.sleep(100); // Simulate API call
            System.out.println("[Payment Service] Processing credit card payment for order: " + request.getOrderId());
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.SUCCESS, "Credit card payment successful");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.FAILED, "Payment interrupted");
        }
    }

    private PaymentResult processDebitCardPayment(String paymentId, PaymentRequest request) {
        try {
            Thread.sleep(80);
            System.out.println("[Payment Service] Processing debit card payment for order: " + request.getOrderId());
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.SUCCESS, "Debit card payment successful");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.FAILED, "Payment interrupted");
        }
    }

    private PaymentResult processWalletPayment(String paymentId, PaymentRequest request) {
        try {
            Thread.sleep(50);
            System.out.println("[Payment Service] Processing wallet payment for order: " + request.getOrderId());
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.SUCCESS, "Wallet payment successful");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new PaymentResult(paymentId, request.getOrderId(),
                    PaymentResult.PaymentStatus.FAILED, "Payment interrupted");
        }
    }

    /**
     * Check payment status by payment ID
     */
    public PaymentResult checkPaymentStatus(String paymentId) {
        return paymentCache.getOrDefault(paymentId,
                new PaymentResult(paymentId, "", PaymentResult.PaymentStatus.PENDING, "Payment status not found"));
    }
}
