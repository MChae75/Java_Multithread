package com.example.flashsale.domain;

public record OrderRequest(String productId, int quantity, String userId) {
}
