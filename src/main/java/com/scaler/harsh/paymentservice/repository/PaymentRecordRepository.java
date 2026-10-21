package com.scaler.harsh.paymentservice.repository;

import com.scaler.harsh.paymentservice.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Integer> {
    Optional<PaymentRecord> findFirstByOrderIdOrderByIdDesc(Integer orderId);
}