package com.springboot.springboot_ecommerce.cart.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.springboot_ecommerce.cart.dto.CartAddRequest;
import com.springboot.springboot_ecommerce.cart.entity.Cart;
import com.springboot.springboot_ecommerce.cart.entity.CartItem;
import com.springboot.springboot_ecommerce.cart.repository.CartRepository;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.order.entity.Order; // ✅ FIXED
import com.springboot.springboot_ecommerce.order.repository.OrderRepository;
import com.springboot.springboot_ecommerce.product.entity.Product;
import com.springboot.springboot_ecommerce.product.repository.ProductRepository;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OrderRepository orderRepo;

    // ================= ADD TO CART =================
    public ApiResponse<CartItem> addToCart(UserEntity user, CartAddRequest request) {

        // ================= AUTH =================
        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only customers can use cart", null);
        }

        // ================= VALIDATION =================
        if (request.getProductId() == null) {
            return new ApiResponse<>(400, "Product id is required", null);
        }

        if (request.getQuantity() <= 0) {
            return new ApiResponse<>(400, "Quantity must be greater than zero", null);
        }

        Optional<Product> productOpt = productRepo.findById(request.getProductId());

        if (productOpt.isEmpty()) {
            return new ApiResponse<>(404, "Product not found", null);
        }

        Product product = productOpt.get();

        // ================= GET OR CREATE CART =================
        Cart cart = cartRepo.findByUserIdAndCancelledFalse(user.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(user.getId());
                    return c;
                });

        if (cart.getItems() == null) {
            cart.setItems(new java.util.ArrayList<>());
        }

        // ================= CHECK EXISTING =================
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst();

        CartItem item;

        if (existing.isPresent()) {

            // increase quantity
            item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());

        } else {

            // create new item
            item = new CartItem();
            item.setProductId(product.getId());
            item.setQuantity(request.getQuantity());

            // seller snapshot
            item.setSellerId(product.getSeller());
            item.setSellerName(product.getCreatedByName());

            item.setCart(cart);
            cart.getItems().add(item);
        }

        // ================= SAVE =================
        Cart savedCart = cartRepo.save(cart);

        // ================= FETCH SAVED ITEM (WITH ID) =================
        CartItem savedItem = savedCart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        return new ApiResponse<>(200, "Product added to cart successfully", savedItem);
    }

    // ================= SEARCH ORDERS =================
    public ApiResponse<List<Order>> searchOrders(
            Long userId,
            Long sellerId,
            Long productId,
            String status,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        String role = user.getRole();

        if (!"admin".equalsIgnoreCase(role) &&
                !"super_admin".equalsIgnoreCase(role)) {
            return new ApiResponse<>(403, "Only admin can view orders", null);
        }

        List<Order> orders = orderRepo.search(userId, sellerId, productId, status);

        return new ApiResponse<>(200, "Orders fetched successfully", orders);
    }

    // ================= GET CART =================
    public ApiResponse<Cart> getCartById(Long cartId, UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Optional<Cart> cartOpt = cartRepo.findById(cartId);

        if (cartOpt.isEmpty()) {
            return new ApiResponse<>(404, "Cart not found", null);
        }

        Cart cart = cartOpt.get();

        String role = user.getRole();

        if ("admin".equalsIgnoreCase(role) || "super_admin".equalsIgnoreCase(role)) {
            return new ApiResponse<>(200, "Cart fetched successfully", cart);
        }

        if ("customer".equalsIgnoreCase(role)) {
            if (!cart.getUserId().equals(user.getId())) {
                return new ApiResponse<>(403, "You cannot view other user's cart", null);
            }

            return new ApiResponse<>(200, "Cart fetched successfully", cart);
        }

        return new ApiResponse<>(403, "Access denied", null);
    }

    // ================= CANCEL CART =================
    public ApiResponse<Cart> cancelCart(Long cartId, UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Cart cart = cartRepo.findById(cartId).orElse(null);

        if (cart == null || cart.isCancelled()) {
            return new ApiResponse<>(404, "Cart not found", null);
        }

        String role = user.getRole();

        if (role.equalsIgnoreCase("admin") ||
                role.equalsIgnoreCase("super_admin")) {
            // allowed
        } else if (role.equalsIgnoreCase("customer")) {
            if (!cart.getUserId().equals(user.getId())) {
                return new ApiResponse<>(403, "You can cancel only your cart", null);
            }
        } else {
            return new ApiResponse<>(403, "You are not allowed to cancel cart", null);
        }

        cart.setCancelled(true);
        cart.setCancelledAt(LocalDateTime.now());

        cartRepo.save(cart);

        return new ApiResponse<>(200, "Cart cancelled successfully", cart);
    }
}
