package com.springboot.springboot_ecommerce.order.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String paymentMethod;
    private boolean success;
}
