package com.scaler.harsh.paymentservice.controller;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.exceptions.ServiceException;
import com.scaler.harsh.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService svc;

    public PaymentController(PaymentService svc) {
        this.svc = svc;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody CreatePaymentRequestDTO request) {
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new ServiceException("Valid orderId is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getAmount() <= 0) {
            throw new ServiceException("Amount must be positive", HttpStatus.BAD_REQUEST);
        }

        PaymentResponseDTO response = svc.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}