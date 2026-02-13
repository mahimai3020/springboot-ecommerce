package com.springboot.springboot_ecommerce.product.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // basic
    @Column(nullable = false)
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    private double price;

    @Column(nullable = false)
    @NotNull
    @PositiveOrZero
    private int stock;

    @Column(nullable = false)
    private Long seller;

    private String category;

    // ⭐ relations (child owns FK)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductReview> reviews;

    // analytics
    private double rating = 0.0;
    private int numOfReviews = 0;

    // audit
    private Long createdBy;
    private String createdByName;
    private Long updatedBy;
    private String updatedByName;
    private Long deletedBy;
    private String deletedByName;

    private boolean deleted = false;
    private LocalDateTime deletedAt;

    // timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {}

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
