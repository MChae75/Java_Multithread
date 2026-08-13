package com.example.flashsale.service;

import java.time.LocalDateTime;

public class PaymentResult {
    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    private String paymentId;
    private String orderId;
    private PaymentStatus status;
    private String message;
    private LocalDateTime processedAt;

    public PaymentResult(String paymentId, String orderId, PaymentStatus status, String message) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    @Override
    public String toString() {
        return "PaymentResult{" +
                "paymentId='" + paymentId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
