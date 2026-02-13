package com.springboot.springboot_ecommerce.cart.dto;

import lombok.Data;

@Data
public class CartAddRequest {
    private Long productId;
    private int quantity;
}
