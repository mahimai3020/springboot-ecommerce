package com.springboot.springboot_ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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
