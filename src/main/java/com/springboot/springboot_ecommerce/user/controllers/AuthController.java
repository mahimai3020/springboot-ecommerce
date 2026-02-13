package com.springboot.springboot_ecommerce.user.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.springboot_ecommerce.user.dto.*;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        @Autowired
        private AuthService service;
        private final AuthService authService;

        public AuthController(AuthService authService) {
                this.authService = authService;
        }

        @PostMapping("/login")
        public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {

                String username = request.get("username");
                String password = request.get("password");

                UserEntity user = authService.login(username, password);

                Map<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("name", user.getName());
                data.put("token", user.getToken());

                Map<String, Object> response = new HashMap<>();
                response.put("status", 200);
                response.put("message", "Login successful");
                response.put("data", data);

                return ResponseEntity.ok(response);
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse> logout(
                        @RequestParam Long id,
                        @RequestParam String token) {

                service.logout(id, token);

                return ResponseEntity.ok(
                                new ApiResponse(200, "Logout successful", null));
        }

        @PostMapping("/reset_password")
        public ResponseEntity<ApiResponse> resetPassword(@RequestBody Map<String, String> request) {

                String email = request.get("email");
                String password = request.get("password");
                String confirmPassword = request.get("confirmPassword");

                service.resetPassword(email, password, confirmPassword);

                return ResponseEntity.ok(
                                new ApiResponse(200, "Password reset successful", null));
        }

}
