package com.springboot.springboot_ecommerce.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.order.dto.AssignDeliveryRequest;
import com.springboot.springboot_ecommerce.order.dto.DeliveryStatusRequest;
import com.springboot.springboot_ecommerce.order.dto.PaymentRequest;
import com.springboot.springboot_ecommerce.order.entity.Order;
import com.springboot.springboot_ecommerce.order.service.OrderService;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    @PostMapping("/checkout")
    public ApiResponse<Order> checkout(@AuthenticationPrincipal UserEntity user) {
        return service.checkout(user);
    }

    @PutMapping("/{orderId}/assign_delivery")
    public ApiResponse<Order> assignDelivery(
            @PathVariable Long orderId,
            @RequestBody AssignDeliveryRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.assignDelivery(orderId, request, user);
    }

    @GetMapping("/search")
    public List<Order> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String status) {

        return service.search(userId, sellerId, productId, status);
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Order> pay(@PathVariable Long orderId,
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserEntity user) {
        return service.pay(orderId, request, user);
    }

    @PostMapping("/{orderId}/retry-payment")
    public ApiResponse<Order> retry(@PathVariable Long orderId,
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserEntity user) {
        return service.retryPayment(orderId, request, user);
    }

    @PutMapping("/{orderId}/delivery_status")
    public ApiResponse<Order> updateStatus(
            @PathVariable Long orderId,
            @RequestBody DeliveryStatusRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.updateDeliveryStatus(orderId, request, user);
    }

}
