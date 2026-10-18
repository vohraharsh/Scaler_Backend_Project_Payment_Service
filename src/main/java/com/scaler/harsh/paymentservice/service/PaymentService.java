package com.scaler.harsh.paymentservice.service;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO processPayment(CreatePaymentRequestDTO request);
}