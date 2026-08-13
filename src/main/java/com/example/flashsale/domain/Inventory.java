package com.example.flashsale.domain;

import java.time.LocalDateTime;

public class Inventory {

    private String productId;
    private int availableQuantity;
    private int reservedQuantity;
    private int soldQuantity;
    private LocalDateTime lastUpdated;

    public Inventory() {
    }

    public Inventory(String productId, int availableQuantity) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.soldQuantity = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public synchronized boolean reserve(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            reservedQuantity += quantity;
            lastUpdated = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public synchronized void confirmSale(int quantity) {
        reservedQuantity -= quantity;
        soldQuantity += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public synchronized void releaseReservation(int quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public synchronized int getTotalStock() {
        return availableQuantity + reservedQuantity + soldQuantity;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "productId='" + productId + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", soldQuantity=" + soldQuantity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
