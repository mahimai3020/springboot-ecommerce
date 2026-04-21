package com.springboot.springboot_ecommerce.cart.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.springboot.springboot_ecommerce.cart.dto.CartAddRequest;
import com.springboot.springboot_ecommerce.cart.entity.Cart;
import com.springboot.springboot_ecommerce.cart.entity.CartItem;
import com.springboot.springboot_ecommerce.cart.service.CartService;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    //  ADD TO CART 
    @PostMapping("/add")
    public ApiResponse<CartItem> addToCart(
            @AuthenticationPrincipal UserEntity user,
            @Valid @RequestBody CartAddRequest request) {

        return service.addToCart(user, request);
    }

    //  GET CART 
    @GetMapping("/{cartId}")
    public ApiResponse<Cart> getCart(
            @PathVariable Long cartId,
            @AuthenticationPrincipal UserEntity user) {

        return service.getCartById(cartId, user);
    }

    //  CANCEL CART 

    @DeleteMapping("/cancel/{cartId}")
    public ApiResponse<Cart> cancel(
            @PathVariable Long cartId,
            @AuthenticationPrincipal UserEntity user) {

        return service.cancelCart(cartId, user);
    }
}
