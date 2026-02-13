package com.springboot.springboot_ecommerce.product.dto;

import lombok.Data;

@Data
public class ProductReviewRequest {

    private Integer rating;
    private String comment;
}
