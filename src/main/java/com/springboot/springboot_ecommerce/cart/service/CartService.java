package com.springboot.springboot_ecommerce.cart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.springboot_ecommerce.cart.dto.CartAddRequest;
import com.springboot.springboot_ecommerce.cart.entity.Cart;
import com.springboot.springboot_ecommerce.cart.entity.CartItem;
import com.springboot.springboot_ecommerce.cart.repository.CartRepository;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.product.entity.Product;
import com.springboot.springboot_ecommerce.product.repository.ProductRepository;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo,
            ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    //  ADD TO CART 
    @Transactional
    public ApiResponse<CartItem> addToCart(UserEntity user, CartAddRequest request) {

        // AUTH
        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only customers can use cart", null);
        }

        // PRODUCT
        Product product = productRepo.findById(request.getProductId())
                .orElse(null);

        if (product == null) {
            return new ApiResponse<>(404, "Product not found", null);
        }

        // GET OR CREATE CART
        Cart cart = cartRepo.findByUserIdAndCancelledFalse(user.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(user.getId());
                    c.setItems(new ArrayList<>());
                    return c;
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // CHECK EXISTING
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst();

        CartItem item;

        if (existing.isPresent()) {
            item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem();
            item.setProductId(product.getId());
            item.setQuantity(request.getQuantity());
            item.setSellerId(product.getSeller());
            item.setSellerName(product.getCreatedByName());
            item.setCart(cart);

            cart.getItems().add(item);
        }

        Cart savedCart = cartRepo.save(cart);

        CartItem savedItem = savedCart.getItems().stream()
                .filter(i -> i.getProductId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        return new ApiResponse<>(200, "Product added to cart successfully", savedItem);
    }

    //  GET CART 

    public ApiResponse<Cart> getCartById(Long cartId, UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Cart cart = cartRepo.findById(cartId).orElse(null);

        if (cart == null) {
            return new ApiResponse<>(404, "Cart not found", null);
        }

        String role = user.getRole();

        if ("admin".equalsIgnoreCase(role) ||
                "super_admin".equalsIgnoreCase(role)) {
            return new ApiResponse<>(200, "Cart fetched successfully", cart);
        }

        if ("customer".equalsIgnoreCase(role) &&
                cart.getUserId().equals(user.getId())) {
            return new ApiResponse<>(200, "Cart fetched successfully", cart);
        }

        return new ApiResponse<>(403, "Access denied", null);
    }

    //  CANCEL CART 
    @Transactional
    public ApiResponse<Cart> cancelCart(Long cartId, UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Cart cart = cartRepo.findById(cartId).orElse(null);

        if (cart == null || cart.isCancelled()) {
            return new ApiResponse<>(404, "Cart not found", null);
        }

        if ("customer".equalsIgnoreCase(user.getRole()) &&
                !cart.getUserId().equals(user.getId())) {
            return new ApiResponse<>(403, "You can cancel only your cart", null);
        }

        cart.setCancelled(true);
        cart.setCancelledAt(LocalDateTime.now());

        cartRepo.save(cart);

        return new ApiResponse<>(200, "Cart cancelled successfully", cart);
    }
}
