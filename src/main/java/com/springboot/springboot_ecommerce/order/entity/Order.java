package com.springboot.springboot_ecommerce.order.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long cartId;

    private Long productId;
    private String productName;

    private Long sellerId;
    private String sellerName;

    private int quantity;
    private double price;
    private double totalAmount;

    private String status; // CREATED, SHIPPED, DELIVERED

    // ✅ DELIVERY FIELDS (ADD THESE)
    private Long deliveryManId;
    private String deliveryManName;
    private String deliveryManContact;
    private String deliveryStatus; // ASSIGNED, PICKED, DELIVERED

    // payment
    private String paymentMethod; // UPI, CARD, COD
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    private String transactionId;
    private LocalDateTime paidAt;

}
