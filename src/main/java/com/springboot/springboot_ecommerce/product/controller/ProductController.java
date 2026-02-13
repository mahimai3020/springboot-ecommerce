package com.springboot.springboot_ecommerce.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService service;

    // ================= CREATE =================
    @PostMapping("/add")
    public ApiResponse<Product> create(@Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.createProduct(request, user);
    }

    // ================= GET ALL =================
    @GetMapping
    public ApiResponse<List<ProductResponse>> getAllProducts(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long seller,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) String createdByName) {

        List<ProductResponse> products = service.getAllProducts(user, id, name, category, seller, rating,
                createdByName);

        return new ApiResponse<>(
                200,
                "Products fetched successfully",
                products);
    }

    // ================= UPDATE =================
    @PutMapping("/update/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal UserEntity user) {

        ProductResponse product = service.updateProduct(id, request, user);

        if (product == null) {
            return new ApiResponse<>(200, "Nothing updated", null);
        }

        return new ApiResponse<>(200, "Product updated successfully", product);
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<ProductImage> upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserEntity user) {

        return service.uploadImage(productId, file, user);
    }

    @PostMapping("/review/{productId}")
    public ApiResponse<Product> review(
            @PathVariable Long productId,
            @RequestBody ProductReviewRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.addReview(productId, request, user);
    }

    // ================= DELETE =================
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserEntity user) {

        service.deleteProduct(id, user);

        return new ApiResponse<>(200, "Product deleted successfully", null);
    }
}
