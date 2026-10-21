package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.exceptions.ServiceException;
import com.scaler.harsh.paymentservice.model.PaymentRecord;
import com.scaler.harsh.paymentservice.repository.PaymentRecordRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Primary
public class SelfPaymentService implements PaymentService {

    private static final double ORGANIC_FAILURE_RATE = 0.10;

    private final PaymentRecordRepository paymentRecordRepository;
    private final Random random;

    public SelfPaymentService(PaymentRecordRepository paymentRecordRepository, Random random) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.random = random;
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

        return toResponseDTO(record);
    }

    @Override
    public PaymentResponseDTO getPaymentByOrderId(Integer orderId) {
        Optional<PaymentRecord> record = paymentRecordRepository.findByOrderId(orderId);
        if (record.isEmpty()) {
            throw new ServiceException("No payment found for order: " + orderId, HttpStatus.NOT_FOUND);
        }
        return toResponseDTO(record.get());
    }

    private PaymentResponseDTO toResponseDTO(PaymentRecord record) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId(record.getTransactionId());
        response.setOrderId(record.getOrderId());
        response.setAmount(record.getAmount());
        response.setStatus(record.getStatus());
        response.setPaymentMethod(record.getPaymentMethod());
        return response;
    }
}