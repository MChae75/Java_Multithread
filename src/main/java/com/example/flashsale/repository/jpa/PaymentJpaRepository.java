package com.example.flashsale.repository.jpa;

import com.example.flashsale.entity.PaymentEntity;
import com.example.flashsale.service.PaymentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByOrderId(String orderId);
    
    List<PaymentEntity> findByStatus(PaymentResult.PaymentStatus status);
}
