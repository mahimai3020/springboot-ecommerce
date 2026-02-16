package com.springboot.springboot_ecommerce.product.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.product.dto.*;
import com.springboot.springboot_ecommerce.product.entity.*;
import com.springboot.springboot_ecommerce.product.repository.*;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.exception.ApiException;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final ProductReviewRepository reviewRepo;
    private final ProductImageRepository imageRepo;

    // ================= CREATE =================
    @Transactional
    public ApiResponse<Product> createProduct(ProductRequest request, UserEntity user) {

        validateUser(user);
        validateCreatePermission(user);

        if (request.getPrice() < 0)
            return new ApiResponse<>(400, "Price cannot be negative", null);

        if (request.getStock() < 0)
            return new ApiResponse<>(400, "Stock cannot be negative", null);

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setSeller(user.getId());
        product.setCreatedBy(user.getId());
        product.setCreatedByName(user.getName());

        return new ApiResponse<>(201, "Product created", repo.save(product));
    }

    // ================= GET ALL =================
    public Page<ProductResponse> getAllProducts(
            UserEntity user,
            Long id,
            String name,
            String category,
            Long seller,
            Double rating,
            String createdByName,
            int page,
            int size) {

        validateUser(user);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());

        Specification<Product> spec = (root, query, cb) -> {

            List<Predicate> p = new ArrayList<>();
            p.add(cb.isFalse(root.get("deleted")));

            if (id != null)
                p.add(cb.equal(root.get("id"), id));

            if (name != null && !name.isBlank())
                p.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));

            if (category != null && !category.isBlank())
                p.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));

            if (seller != null)
                p.add(cb.equal(root.get("seller"), seller));

            if (rating != null)
                p.add(cb.greaterThanOrEqualTo(root.get("rating"), rating));

            if (createdByName != null && !createdByName.isBlank())
                p.add(cb.like(cb.lower(root.get("createdByName")),
                        "%" + createdByName.toLowerCase() + "%"));

            return cb.and(p.toArray(new Predicate[0]));
        };

        return repo.findAll(spec, pageable).map(this::mapToResponse);
    }

    // ================= UPDATE =================
    @Transactional
    public ProductResponse updateProduct(Long productId,
            ProductUpdateRequest request,
            UserEntity user) {

        validateUser(user);

        Product product = repo.findById(productId)
                .orElseThrow(() -> new ApiException(null, "Product not found"));

        if (product.isDeleted())
            throw new ApiException(null, "Product already deleted");

        validateUpdatePermission(user, product);

        boolean updated = false;

        if (request.getName() != null) {
            product.setName(request.getName());
            updated = true;
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
            updated = true;
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
            updated = true;
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
            updated = true;
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
            updated = true;
        }

        if (!updated)
            return null;

        product.setUpdatedBy(user.getId());
        product.setUpdatedByName(user.getUserName());

        return mapToResponse(repo.save(product));
    }

    // ================= DELETE =================
    @Transactional
    public void deleteProduct(Long productId, UserEntity user) {

        validateUser(user);

        if (!List.of("admin", "super_admin").contains(user.getRole()))
            throw new AccessDeniedException("You are not allowed");

        Product product = repo.findById(productId)
                .orElseThrow(() -> new ApiException(null, "Product not found"));

        if (product.isDeleted())
            throw new ApiException(null, "Already deleted");

        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(user.getId());
        product.setDeletedByName(user.getUserName());

        repo.save(product);
    }

    // ================= REVIEW =================
    @Transactional
    public ApiResponse<Product> addReview(Long productId,
            ProductReviewRequest request,
            UserEntity user) {

        validateUser(user);

        if (!"customer".equalsIgnoreCase(user.getRole()))
            return new ApiResponse<>(403, "Only customers can review", null);

        Product product = repo.findById(productId).orElse(null);
        if (product == null)
            return new ApiResponse<>(404, "Product not found", null);

        ProductReview review = new ProductReview();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setProduct(product);
        review.setCreatedBy(user.getId());
        review.setCreatedByName(user.getName());

        reviewRepo.save(review);

        double total = product.getRating() * product.getNumOfReviews() + request.getRating();
        int count = product.getNumOfReviews() + 1;

        product.setNumOfReviews(count);
        product.setRating(total / count);

        repo.save(product);

        return new ApiResponse<>(200, "Review added", product);
    }

    // ================= IMAGE =================
    @Transactional
    public ApiResponse<ProductImage> uploadImage(Long productId,
            MultipartFile file,
            UserEntity user) {

        validateUser(user);

        if (file == null || file.isEmpty())
            return new ApiResponse<>(400, "File is required", null);

        Product product = repo.findById(productId).orElse(null);
        if (product == null)
            return new ApiResponse<>(404, "Product not found", null);

        if ("seller".equalsIgnoreCase(user.getRole())
                && !product.getSeller().equals(user.getId()))
            return new ApiResponse<>(403, "Not your product", null);

        try {
            // store inside container folder
            String folder = "uploads/products/" + productId + "/";
            Files.createDirectories(Paths.get(folder));

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(folder + fileName);
            Files.write(path, file.getBytes());

            ProductImage image = new ProductImage();

            // IMPORTANT: no localhost
            image.setUrl("/" + folder + fileName);
            image.setProduct(product);

            return new ApiResponse<>(200, "Uploaded", imageRepo.save(image));

        } catch (Exception e) {
            return new ApiResponse<>(500, "Upload failed", null);
        }
    }

    // ================= COMMON =================
    private void validateUser(UserEntity user) {
        if (user == null)
            throw new AccessDeniedException("Please login again");
    }

    private void validateCreatePermission(UserEntity user) {
        String role = user.getRole().toLowerCase();
        if (!role.equals("seller") && !role.equals("admin") && !role.equals("super_admin"))
            throw new AccessDeniedException("You are not allowed");
    }

    private void validateUpdatePermission(UserEntity user, Product product) {

        String role = user.getRole();

        if ("admin".equals(role) || "super_admin".equals(role))
            return;

        if ("seller".equals(role) && product.getSeller().equals(user.getId()))
            return;

        throw new AccessDeniedException("You cannot update this product");
    }

    private ProductResponse mapToResponse(Product p) {

        List<ProductImageResponse> images = p.getImages() == null ? null
                : p.getImages().stream()
                        .map(i -> new ProductImageResponse(i.getId(), i.getUrl()))
                        .toList();

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .category(p.getCategory())
                .seller(p.getSeller())
                .rating(p.getRating())
                .numOfReviews(p.getNumOfReviews())
                .createdBy(p.getCreatedBy())
                .createdByName(p.getCreatedByName())
                .updatedBy(p.getUpdatedBy())
                .updatedByName(p.getUpdatedByName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .images(images)
                .build();
    }
}
