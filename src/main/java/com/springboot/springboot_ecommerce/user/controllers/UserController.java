package com.springboot.springboot_ecommerce.user.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.springboot.springboot_ecommerce.user.dto.UserRequestDTO;
import com.springboot.springboot_ecommerce.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequestDTO user) {
        Map<String, Object> response = userService.createUser(user);
        return ResponseEntity.status((int) response.get("status")).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        Map<String, Object> response = userService.getUserById(id);
        return ResponseEntity.status((int) response.get("status")).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = userService.deleteUser(id);
        return ResponseEntity.status((int) response.get("status")).body(response);
    }
}