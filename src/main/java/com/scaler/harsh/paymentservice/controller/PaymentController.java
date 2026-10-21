package com.scaler.harsh.paymentservice.controller;

import com.scaler.harsh.paymentservice.dto.CreatePaymentRequestDTO;
import com.scaler.harsh.paymentservice.dto.PaymentResponseDTO;
import com.scaler.harsh.paymentservice.exceptions.ServiceException;
import com.scaler.harsh.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class PaymentController {

    private static final Set<String> ALLOWED_METHODS = Set.of("CARD", "UPI", "NETBANKING", "WALLET");

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
        if (request.getPaymentMethod() == null
                || !ALLOWED_METHODS.contains(request.getPaymentMethod().trim().toUpperCase())) {
            throw new ServiceException(
                    "Valid paymentMethod is required. Must be one of: CARD, UPI, NETBANKING, WALLET",
                    HttpStatus.BAD_REQUEST);
        }

        // Always 201 — the request to process a payment succeeded; the outcome (SUCCESS/FAILED)
        // is in the response body's status field, not the HTTP status. A decline is a normal
        // business outcome here, not an exceptional one.
        PaymentResponseDTO response = svc.processPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Receipt lookup — lets a client retrieve a payment's outcome after the fact instead of
    // only ever seeing it in the original POST /payments response (PRD 5.3).
    @GetMapping("/payments/order/{orderId}")
    public PaymentResponseDTO getPaymentByOrderId(@PathVariable("orderId") Integer orderId) {
        if (orderId == null || orderId <= 0) {
            throw new ServiceException("Invalid orderId", HttpStatus.BAD_REQUEST);
        }
        return svc.getPaymentByOrderId(orderId);
    }
}