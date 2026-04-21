package com.springboot.springboot_ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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