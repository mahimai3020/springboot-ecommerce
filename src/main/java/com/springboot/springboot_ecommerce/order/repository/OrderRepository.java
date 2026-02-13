package com.springboot.springboot_ecommerce.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.springboot.springboot_ecommerce.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
           SELECT o FROM Order o
           WHERE (:userId IS NULL OR o.userId = :userId)
           AND (:sellerId IS NULL OR o.sellerId = :sellerId)
           AND (:productId IS NULL OR o.productId = :productId)
           AND (:status IS NULL OR o.status = :status)
           """)
    List<Order> search(Long userId, Long sellerId, Long productId, String status);
}
