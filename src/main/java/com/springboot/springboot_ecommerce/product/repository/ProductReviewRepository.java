package com.springboot.springboot_ecommerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot.springboot_ecommerce.product.entity.ProductReview;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {
}
