package com.springboot.springboot_ecommerce.cart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.springboot.springboot_ecommerce.cart.dto.CartAddRequest;
import com.springboot.springboot_ecommerce.cart.entity.Cart;
import com.springboot.springboot_ecommerce.cart.entity.CartItem;
import com.springboot.springboot_ecommerce.cart.service.CartService;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.order.entity.Order;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService service;

    // ================= ADD TO CART =================
    @PostMapping("/add")
    public ApiResponse<CartItem> addToCart(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody CartAddRequest request) {

        return service.addToCart(user, request);
    }

    // ================= GET CART BY ID =================
    @GetMapping("/{cartId}")
    public ApiResponse<Cart> getCart(
            @PathVariable Long cartId,
            @AuthenticationPrincipal UserEntity user) {

        return service.getCartById(cartId, user);
    }

    // ================= CANCEL CART =================
    @DeleteMapping("/cancel/{cartId}")
    public ApiResponse<Cart> cancel(
            @PathVariable Long cartId,
            @AuthenticationPrincipal UserEntity user) {

        return service.cancelCart(cartId, user);
    }

    // ================= SEARCH ORDERS (ADMIN) =================
    @GetMapping("/orders/search")
    public ApiResponse<List<Order>> searchOrders(
            @RequestParam(required = false) Long cartId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String productName,
            @AuthenticationPrincipal UserEntity user) {

        return service.searchOrders(cartId, productId, sellerId, productName, user);
    }
}
