package com.springboot.springboot_ecommerce.product.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.product.dto.ProductImageResponse;
import com.springboot.springboot_ecommerce.product.dto.ProductRequest;
import com.springboot.springboot_ecommerce.product.dto.ProductResponse;
import com.springboot.springboot_ecommerce.product.dto.ProductReviewRequest;
import com.springboot.springboot_ecommerce.product.dto.ProductUpdateRequest;
import com.springboot.springboot_ecommerce.product.entity.Product;
import com.springboot.springboot_ecommerce.product.entity.ProductImage;
import com.springboot.springboot_ecommerce.product.entity.ProductReview;
import com.springboot.springboot_ecommerce.product.repository.ProductImageRepository;
import com.springboot.springboot_ecommerce.product.repository.ProductRepository;
import com.springboot.springboot_ecommerce.product.repository.ProductReviewRepository;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.exception.ApiException;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repo;

    @Autowired
    private ProductReviewRepository reviewRepo;

    @Autowired
    private ProductImageRepository imageRepo;

    // ================= CREATE =================
    public ApiResponse<Product> createProduct(ProductRequest request, UserEntity user) {

        // ⭐ authentication check
        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        String role = user.getRole();

        // ⭐ authorization check
        if (!"seller".equalsIgnoreCase(role) &&
                !"admin".equalsIgnoreCase(role) &&
                !"super_admin".equalsIgnoreCase(role)) {

            return new ApiResponse<>(403, "You are not allowed to create product", null);
        }

        // ⭐ extra safety (DTO should handle, but double check)
        if (request.getName() == null || request.getName().isBlank()) {
            return new ApiResponse<>(400, "Product name is required", null);
        }

        if (request.getPrice() < 0) {
            return new ApiResponse<>(400, "Price cannot be negative", null);
        }

        if (request.getStock() < 0) {
            return new ApiResponse<>(400, "Stock cannot be negative", null);
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());

        // ⭐ seller snapshot from JWT
        product.setSeller(user.getId());
        product.setCreatedByName(user.getName());

        Product saved = repo.save(product);

        return new ApiResponse<>(201, "Product created successfully", saved);
    }

    // ================= GET ALL WITH FILTER =================
    public List<ProductResponse> getAllProducts(UserEntity user,
            Long id,
            String name,
            String category,
            Long seller,
            Double rating,
            String createdByName) {

        validateUser(user); // only token validation

        return repo.findAll()
                .stream()
                .filter(p -> !p.isDeleted())
                .filter(p -> id == null || p.getId().equals(id))
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> category == null || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> seller == null || p.getSeller().equals(seller))
                .filter(p -> rating == null || p.getRating() >= rating)
                .filter(p -> createdByName == null ||
                        p.getCreatedByName().toLowerCase().contains(createdByName.toLowerCase()))
                .map(this::mapToResponse)
                .toList();
    }

    // ================= UPDATE =================
    public ProductResponse updateProduct(Long productId,
            ProductUpdateRequest request,
            UserEntity user) {

        validateUser(user);

        Product product = repo.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.isDeleted()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product already deleted");
        }

        validateUpdatePermission(user, product);

        boolean updated = false;

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            product.setName(request.getName());
            updated = true;
        }

        if (request.getDescription() != null &&
                !request.getDescription().equals(product.getDescription())) {
            product.setDescription(request.getDescription());
            updated = true;
        }

        if (request.getPrice() != null && request.getPrice() != product.getPrice()) {
            product.setPrice(request.getPrice());
            updated = true;
        }

        if (request.getStock() != null && request.getStock() != product.getStock()) {
            product.setStock(request.getStock());
            updated = true;
        }

        if (request.getCategory() != null &&
                !request.getCategory().equals(product.getCategory())) {
            product.setCategory(request.getCategory());
            updated = true;
        }

        if (!updated) {
            return null; // controller → "Nothing updated"
        }

        product.setUpdatedBy(user.getId());
        product.setUpdatedByName(user.getUserName());

        Product saved = repo.save(product);

        return mapToResponse(saved);
    }

    // ================= DELETE (SOFT) =================
    public void deleteProduct(Long productId, UserEntity user) {

        validateUser(user);

        if (!(user.getRole().equals("admin") ||
                user.getRole().equals("super_admin"))) {
            throw new AccessDeniedException("You are not allowed to delete product");
        }

        Product product = repo.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.isDeleted()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product already deleted");
        }

        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setDeletedBy(user.getId());
        product.setDeletedByName(user.getUserName());

        repo.save(product);
    }

    // ================= PERMISSION =================
    private void validateCreatePermission(UserEntity user) {

        if (user == null || user.getRole() == null) {
            throw new AccessDeniedException("Please login again");
        }
        validateCreatePermission(user);

        String role = user.getRole().toLowerCase();

        if (!role.equals("seller") &&
                !role.equals("admin") &&
                !role.equals("super_admin")) {

            throw new AccessDeniedException("You are not allowed to create product");
        }
    }

    private void validateUpdatePermission(UserEntity user, Product product) {

        String role = user.getRole();

        if (role.equals("super_admin") || role.equals("admin")) {
            return;
        }

        if (role.equals("seller")) {
            if (!product.getCreatedBy().equals(user.getId())) {
                throw new AccessDeniedException("You can update only your products");
            }
            return;
        }

        throw new AccessDeniedException("You are not allowed to update product");
    }

    public ApiResponse<Product> addReview(Long productId,
            ProductReviewRequest request,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only customers can review", null);
        }

        // ⭐ use repo (not productRepo)
        Product product = repo.findById(productId).orElse(null);

        if (product == null) {
            return new ApiResponse<>(404, "Product not found", null);
        }

        ProductReview review = new ProductReview();
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review.setCreatedBy(user.getId());
        review.setCreatedByName(user.getName());
        review.setCreatedByEmail(user.getEmail());
        review.setCreatedByContact(user.getPhoneNumber());

        review.setProduct(product);

        reviewRepo.save(review);

        double totalRating = product.getRating() * product.getNumOfReviews() + request.getRating();

        int newCount = product.getNumOfReviews() + 1;

        double newAverage = totalRating / newCount;

        product.setRating(newAverage);
        product.setNumOfReviews(newCount);

        repo.save(product); // ⭐ fix

        return new ApiResponse<>(200, "Review added successfully", product);
    }

    // ================= COMMON =================
    private void validateUser(UserEntity user) {
        if (user == null) {
            throw new AccessDeniedException("Please login again");
        }
    }

    public ApiResponse<ProductImage> uploadImage(Long productId,
            MultipartFile file,
            UserEntity user) {

        // ================= AUTH =================
        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Product product = repo.findById(productId).orElse(null);

        if (product == null) {
            return new ApiResponse<>(404, "Product not found", null);
        }

        // ================= PERMISSION =================
        String role = user.getRole();

        if ("seller".equalsIgnoreCase(role)) {

            // seller can upload only to his product
            if (!product.getSeller().equals(user.getId())) {
                return new ApiResponse<>(403,
                        "You can upload images only for your product",
                        null);
            }

        } else if ("admin".equalsIgnoreCase(role) ||
                "super_admin".equalsIgnoreCase(role)) {
            // allowed
        } else {
            return new ApiResponse<>(403, "You are not allowed", null);
        }

        try {
            // ================= NAME =================
            String slug = product.getName()
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]", "-");

            int count = product.getImages() == null ? 0 : product.getImages().size();
            int next = count + 1;

            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf('.'));

            String fileName = slug + "-" + product.getId() + "-" + next + ext;

            // ================= STORAGE =================
            String folder = "uploads/products/" + product.getId() + "/";
            java.nio.file.Path path = java.nio.file.Paths.get(folder + fileName);

            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, file.getBytes());

            // ================= URL =================
            String imageUrl = "http://localhost:8080/" + folder + fileName;

            // ================= SAVE =================
            ProductImage image = new ProductImage();
            image.setUrl(imageUrl);
            image.setProduct(product);

            imageRepo.save(image);

            return new ApiResponse<>(200, "Image uploaded successfully", image);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Upload failed", null);
        }
    }

   private ProductResponse mapToResponse(Product product) {

    List<ProductImageResponse> images = null;

    if (product.getImages() != null) {
        images = product.getImages().stream()
                .map(img -> new ProductImageResponse(
                        img.getId(),
                        img.getUrl()))
                .toList();
    }

    return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .category(product.getCategory())
            .seller(product.getSeller())
            .rating(product.getRating())
            .numOfReviews(product.getNumOfReviews())
            .createdBy(product.getCreatedBy())
            .createdByName(product.getCreatedByName())
            .updatedBy(product.getUpdatedBy())
            .updatedByName(product.getUpdatedByName())
            .deletedBy(product.getDeletedBy())
            .deletedByName(product.getDeletedByName())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .images(images)   // ⭐ here
            .build();
}
}
