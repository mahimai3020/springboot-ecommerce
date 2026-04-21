package com.springboot.springboot_ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductCreateResponse {

    private String message;
    private ProductResponse product;
    
}
