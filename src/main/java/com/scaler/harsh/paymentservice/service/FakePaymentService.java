package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakePaymentService implements PaymentService {

    @Override
    public PaymentResponseDTO processPayment(CreatePaymentRequestDTO request) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setTransactionId("TXN-" + UUID.randomUUID());
        response.setOrderId(request.getOrderId());
        response.setAmount(request.getAmount());
        response.setStatus("SUCCESS");
        return response;
    }

    @Override
    public PaymentResponseDTO getPaymentByOrderId(Integer orderId) {
        throw new UnsupportedOperationException("FakePaymentService does not support receipt lookup");
    }
}