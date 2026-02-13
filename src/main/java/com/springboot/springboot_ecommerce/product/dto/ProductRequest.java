package com.springboot.springboot_ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    private String description;

    @PositiveOrZero
    private double price;

    @PositiveOrZero
    private int stock;

    private String category;
}
