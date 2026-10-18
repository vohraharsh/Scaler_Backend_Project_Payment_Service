package com.scaler.harsh.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreatePaymentRequestDTO {
    private Integer orderId;
    private Integer userId;
    private double amount;
}