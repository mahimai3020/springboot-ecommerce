package com.springboot.springboot_ecommerce.product.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductListResponse {

    private String message;
    private List<ProductResponse> products;
}

