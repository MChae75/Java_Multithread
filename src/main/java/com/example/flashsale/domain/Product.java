package com.example.flashsale.domain;

import java.math.BigDecimal;

public class Product {

    private String productId;
    private String name;
    private BigDecimal price;
    private String description;
    private int totalStock;

    public Product(String productId, String name, BigDecimal price, String description, int totalStock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.totalStock = totalStock;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", totalStock=" + totalStock +
                '}';
    }
}
