package com.springboot.springboot_ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long cartId;

    private double totalAmount;

    private String status;
    private String paymentStatus;
    private String deliveryStatus;
    private String paymentMethod;

    private Long deliveryManId;
    private String deliveryManName;
    private String deliveryManContact;

    private String transactionId;
    private LocalDateTime paidAt;
}
