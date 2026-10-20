package com.scaler.harsh.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentResponseDTO {
    private String transactionId;
    private Integer orderId;
    private double amount;
    private String status;
    private String paymentMethod;
}