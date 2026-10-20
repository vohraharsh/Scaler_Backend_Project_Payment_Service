package com.scaler.harsh.paymentservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter @Entity
public class PaymentRecord implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer orderId;
    private Integer userId;
    private double amount;
    private String status;
    private String transactionId;
    private String paymentMethod;
}