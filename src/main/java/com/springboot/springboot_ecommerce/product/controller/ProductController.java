package com.springboot.springboot_ecommerce.product.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.product.dto.ProductRequest;
import com.springboot.springboot_ecommerce.product.dto.ProductResponse;
import com.springboot.springboot_ecommerce.product.dto.ProductReviewRequest;
import com.springboot.springboot_ecommerce.product.dto.ProductUpdateRequest;
import com.springboot.springboot_ecommerce.product.entity.Product;
import com.springboot.springboot_ecommerce.product.entity.ProductImage;
import com.springboot.springboot_ecommerce.product.service.ProductService;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // ================= CREATE =================
    @PostMapping
    public ApiResponse<Product> create(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.createProduct(request, user);
    }

    // ================= GET ALL =================
    @GetMapping
    public ApiResponse<Map<String, Object>> getAll(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long seller,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) String createdByName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<ProductResponse> productPage = service.getAllProducts(
                user, id, name, category, seller, rating, createdByName, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", productPage.getContent());
        response.put("page", productPage.getNumber());
        response.put("size", productPage.getSize());
        response.put("totalElements", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("last", productPage.isLast());

        return new ApiResponse<>(200, "Products fetched successfully", response);
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal UserEntity user) {

        ProductResponse product = service.updateProduct(id, request, user);

        if (product == null) {
            return new ApiResponse<>(404, "Product not found or access denied", null);
        }

        return new ApiResponse<>(200, "Product updated successfully", product);
    }

    // ================= UPLOAD IMAGE =================
    @PostMapping("/{productId}/images")
    public ApiResponse<ProductImage> upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserEntity user) {

        if (file == null || file.isEmpty()) {
            return new ApiResponse<>(400, "File is required", null);
        }

        return service.uploadImage(productId, file, user);
    }

    // ================= REVIEW =================
    @PostMapping("/{productId}/reviews")
    public ApiResponse<Product> review(
            @PathVariable Long productId,
            @Valid @RequestBody ProductReviewRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.addReview(productId, request, user);
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {

        service.deleteProduct(id, user);
        return new ApiResponse<>(200, "Product deleted successfully", null);
    }
}
