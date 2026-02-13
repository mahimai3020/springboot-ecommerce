package com.springboot.springboot_ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductUpdateResponse {

    private String message;
    private ProductResponse product;
}
