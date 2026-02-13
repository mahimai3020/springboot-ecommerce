package com.springboot.springboot_ecommerce.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springboot.springboot_ecommerce.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
