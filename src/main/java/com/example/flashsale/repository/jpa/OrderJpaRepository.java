package com.example.flashsale.repository.jpa;

import com.example.flashsale.entity.OrderEntity;
import com.example.flashsale.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByUserId(String userId);
    
    List<OrderEntity> findByStatus(Order.OrderStatus status);
    
    List<OrderEntity> findByProductId(String productId);
}
