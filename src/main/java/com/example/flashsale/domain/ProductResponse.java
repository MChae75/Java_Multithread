package com.example.flashsale.domain;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal originalPrice,
        BigDecimal price,
        int stock
) {}
