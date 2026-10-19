package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.model.PaymentRecord;
import com.scaler.harsh.paymentservice.repository.PaymentRecordRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Primary
public class SelfPaymentService implements PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;

    public SelfPaymentService(PaymentRecordRepository paymentRecordRepository) {
        this.paymentRecordRepository = paymentRecordRepository;
    }

    @Override
    public PaymentResponseDTO processPayment(CreatePaymentRequestDTO request) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(request.getOrderId());
        record.setUserId(request.getUserId());
        record.setAmount(request.getAmount());
        record.setStatus("SUCCESS");
        record.setTransactionId("TXN-" + UUID.randomUUID());

        record = paymentRecordRepository.save(record);

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId(record.getTransactionId());
        response.setOrderId(record.getOrderId());
        response.setAmount(record.getAmount());
        response.setStatus(record.getStatus());
        return response;
    }
}