package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.model.PaymentRecord;
import com.scaler.harsh.paymentservice.repository.PaymentRecordRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@Primary
public class SelfPaymentService implements PaymentService {

    // Mock gateway only — no real money movement. ~10% organic decline rate so
    // Order Service's failure-handling path gets exercised in normal use, plus
    // an explicit simulateFailure override for deterministic testing.
    private static final double ORGANIC_FAILURE_RATE = 0.10;

    private final PaymentRecordRepository paymentRecordRepository;
    private final Random random = new Random();

    public SelfPaymentService(PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @Override
    public PaymentResponseDTO processPayment(CreatePaymentRequestDTO request) {
        boolean forcedFailure = Boolean.TRUE.equals(request.getSimulateFailure());
        boolean organicFailure = !forcedFailure && random.nextDouble() < ORGANIC_FAILURE_RATE;
        String status = (forcedFailure || organicFailure) ? "FAILED" : "SUCCESS";

        PaymentRecord record = new PaymentRecord();
        record.setOrderId(request.getOrderId());
        record.setUserId(request.getUserId());
        record.setAmount(request.getAmount());
        record.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase());
        record.setStatus(status);
        // Real gateways return a reference id even on a decline — keep that here too.
        record.setTransactionId("TXN-" + UUID.randomUUID());

        record = paymentRecordRepository.save(record);

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId(record.getTransactionId());
        response.setOrderId(record.getOrderId());
        response.setAmount(record.getAmount());
        response.setStatus(record.getStatus());
        response.setPaymentMethod(record.getPaymentMethod());
        return response;
    }
}