package com.springboot.springboot_ecommerce.order.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.order.dto.AssignDeliveryRequest;
import com.springboot.springboot_ecommerce.order.dto.DeliveryStatusRequest;
import com.springboot.springboot_ecommerce.order.dto.PaymentRequest;
import com.springboot.springboot_ecommerce.order.entity.Order;
import com.springboot.springboot_ecommerce.order.service.OrderService;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // ================= CHECKOUT =================
    @PostMapping("/checkout")
    public ApiResponse<Order> checkout(
            @AuthenticationPrincipal UserEntity user) {
        return service.checkout(user);
    }

    // ================= ASSIGN DELIVERY =================
    @PutMapping("/{orderId}/assign-delivery")
    public ApiResponse<Order> assignDelivery(
            @PathVariable Long orderId,
            @Valid @RequestBody AssignDeliveryRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.assignDelivery(orderId, request, user);
    }

    // ================= SEARCH =================
    @GetMapping("/search")
    public ApiResponse<List<Order>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserEntity user) {

        return service.searchOrders(userId, status, user);
    }

    // ================= PAY =================
    @PostMapping("/{orderId}/pay")
    public ApiResponse<Order> pay(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.pay(orderId, request, user);
    }

    // ================= RETRY PAYMENT =================
    @PostMapping("/{orderId}/retry-payment")
    public ApiResponse<Order> retry(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.retryPayment(orderId, request, user);
    }

    // ================= DELIVERY STATUS =================
    @PutMapping("/{orderId}/delivery-status")
    public ApiResponse<Order> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryStatusRequest request,
            @AuthenticationPrincipal UserEntity user) {

        return service.updateDeliveryStatus(orderId, request, user);
    }
}
