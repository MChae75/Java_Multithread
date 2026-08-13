package com.example.flashsale.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class InventoryEntity {

    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public InventoryEntity() {
    }

    public InventoryEntity(String productId, int availableQuantity) {
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

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(Integer soldQuantity) {
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
        return "InventoryEntity{" +
                "productId='" + productId + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", soldQuantity=" + soldQuantity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
