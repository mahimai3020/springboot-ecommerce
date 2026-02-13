package com.springboot.springboot_ecommerce.product.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= REVIEW =================
    private String comment;

    @Min(1)
    @Max(5)
    private Integer rating;

    // ================= CUSTOMER SNAPSHOT =================
    private Long createdBy;          // customer id
    private String createdByName;
    private String createdByEmail;
    private String createdByContact;

    // ================= SOFT DELETE =================
    private Long deletedBy;
    private LocalDateTime deletedAt;

    // ================= TIMESTAMP =================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ================= RELATION =================
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductReview() {}

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
