package com.springboot.springboot_ecommerce.product.dto;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;
}

