package com.springboot.springboot_ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Integer quantity;

    private Long sellerId;
    private String sellerName;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}