package com.springboot.springboot_ecommerce.order.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.springboot_ecommerce.cart.entity.Cart;
import com.springboot.springboot_ecommerce.cart.repository.CartRepository;
import com.springboot.springboot_ecommerce.common.ApiResponse;
import com.springboot.springboot_ecommerce.order.dto.AssignDeliveryRequest;
import com.springboot.springboot_ecommerce.order.dto.DeliveryStatusRequest;
import com.springboot.springboot_ecommerce.order.dto.PaymentRequest;
import com.springboot.springboot_ecommerce.order.entity.Order;
import com.springboot.springboot_ecommerce.order.repository.OrderRepository;
import com.springboot.springboot_ecommerce.product.repository.ProductRepository;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.repository.UserRepository;

@Service
public class OrderService {

    private final UserRepository userRepo;
    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderService(UserRepository userRepo,
            CartRepository cartRepo,
            OrderRepository orderRepo,
            ProductRepository productRepo) {
        this.userRepo = userRepo;
        this.cartRepo = cartRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // SEARCH (ADMIN) 
    public ApiResponse<List<Order>> searchOrders(
            Long userId,
            String status,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"admin".equalsIgnoreCase(user.getRole()) &&
                !"super_admin".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only admin can search orders", null);
        }

        List<Order> orders = orderRepo.search(userId, status);

        return new ApiResponse<>(200, "Orders fetched successfully", orders);
    }

    //  CHECKOUT
    @Transactional
    public ApiResponse<Order> checkout(UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"customer".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only customers can place orders", null);
        }

        Optional<Cart> cartOpt = cartRepo.findByUserIdAndCancelledFalse(user.getId());

        if (cartOpt.isEmpty()) {
            return new ApiResponse<>(404, "Cart not found", null);
        }

        Cart cart = cartOpt.get();

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return new ApiResponse<>(400, "Cart is empty", null);
        }

        double total = cart.getItems()
                .stream()
                .mapToDouble(i -> i.getQuantity()) // later multiply by price
                .sum();

        Order order = new Order();
        order.setUserId(user.getId());
        order.setCartId(cart.getId());
        order.setTotalAmount(total);

        order.setStatus("CREATED");
        order.setPaymentStatus("PENDING");
        order.setDeliveryStatus("PENDING");

        Order saved = orderRepo.save(order);

        cart.setCancelled(true);
        cartRepo.save(cart);

        return new ApiResponse<>(201, "Order placed successfully", saved);
    }

    //ASSIGN DELIVERY 
    @Transactional
    public ApiResponse<Order> assignDelivery(Long orderId,
            AssignDeliveryRequest request,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"admin".equalsIgnoreCase(user.getRole()) &&
                !"super_admin".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only admin can assign delivery", null);
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return new ApiResponse<>(404, "Order not found", null);
        }

        if (!"SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
            return new ApiResponse<>(400, "Payment not completed", null);
        }

        UserEntity deliveryBoy = userRepo.findById(request.getDeliveryManId())
                .orElse(null);

        if (deliveryBoy == null) {
            return new ApiResponse<>(404, "Delivery user not found", null);
        }

        if (!"delivery".equalsIgnoreCase(deliveryBoy.getRole())) {
            return new ApiResponse<>(400, "User is not delivery person", null);
        }

        order.setDeliveryManId(deliveryBoy.getId());
        order.setDeliveryManName(deliveryBoy.getName());
        order.setDeliveryManContact(deliveryBoy.getPhoneNumber());
        order.setDeliveryStatus("ASSIGNED");

        orderRepo.save(order);

        return new ApiResponse<>(200, "Delivery assigned successfully", order);
    }

    //  PAYMENT 

    @Transactional
    public ApiResponse<Order> pay(Long orderId,
            PaymentRequest request,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return new ApiResponse<>(404, "Order not found", null);
        }

        if (!order.getUserId().equals(user.getId())) {
            return new ApiResponse<>(403, "You cannot pay this order", null);
        }

        if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
            return new ApiResponse<>(400, "Payment already completed", null);
        }

        order.setPaymentMethod(request.getPaymentMethod());

        if (request.isSuccess()) {
            order.setPaymentStatus("SUCCESS");
            order.setStatus("CONFIRMED");
            order.setTransactionId("TXN" + System.currentTimeMillis());
            order.setPaidAt(LocalDateTime.now());
        } else {
            order.setPaymentStatus("FAILED");
        }

        orderRepo.save(order);

        return new ApiResponse<>(200, "Payment processed", order);
    }

    //  RETRY 
    public ApiResponse<Order> retryPayment(Long orderId,
            PaymentRequest request,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return new ApiResponse<>(404, "Order not found", null);
        }

        if (!order.getUserId().equals(user.getId())) {
            return new ApiResponse<>(403, "You cannot retry this order", null);
        }

        if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
            return new ApiResponse<>(400, "Payment already successful", null);
        }

        return pay(orderId, request, user);
    }

    //  DELIVERY STATUS 
    @Transactional
    public ApiResponse<Order> updateDeliveryStatus(Long orderId,
            DeliveryStatusRequest request,
            UserEntity user) {

        if (user == null) {
            return new ApiResponse<>(401, "Invalid or missing token", null);
        }

        if (!"delivery".equalsIgnoreCase(user.getRole())) {
            return new ApiResponse<>(403, "Only delivery person can update", null);
        }

        Order order = orderRepo.findById(orderId).orElse(null);

        if (order == null) {
            return new ApiResponse<>(404, "Order not found", null);
        }

        if (!user.getId().equals(order.getDeliveryManId())) {
            return new ApiResponse<>(403, "This order is not assigned to you", null);
        }

        String newStatus = request.getStatus().toUpperCase();

        switch (newStatus) {
            case "PICKED":
            case "OUT_FOR_DELIVERY":
            case "DELIVERED":
            case "CANCELLED":
                order.setDeliveryStatus(newStatus);
                break;

            default:
                return new ApiResponse<>(400, "Invalid status", null);
        }

        orderRepo.save(order);

        return new ApiResponse<>(200, "Delivery status updated", order);
    }
}
