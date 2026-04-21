package com.springboot.springboot_ecommerce.user.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springboot.springboot_ecommerce.user.dto.UserRequestDTO;
import com.springboot.springboot_ecommerce.user.dto.UserResponseDTO;
import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> createUser(UserRequestDTO user) {

        Map<String, Object> response = new HashMap<>();

        UserEntity entity = new UserEntity();
        // entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setUserName(user.getUserName());
        entity.setEmail(user.getEmail());
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setPassword(user.getPassword());
        entity.setRole(user.getRole());
        entity.setStatus(user.getStatus());

        if (entity.getId() != null) {
            return error(response, 400, "ID should not be provided");
        }

        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            return error(response, 400, "Name is required");
        }

        if (entity.getName().length() < 3 || entity.getName().length() > 20) {
            return error(response, 400, "Name must be between 3 and 20 characters");
        }

        if (entity.getUserName() == null || entity.getUserName().trim().isEmpty()) {
            return error(response, 400, "Username is required");
        }

        if (userRepository.existsByUserName(entity.getUserName())) {
            return error(response, 409, "Username already exists");
        }

        if (entity.getEmail() == null || entity.getEmail().trim().isEmpty()) {
            return error(response, 400, "Email is required");
        }

        String emailRegex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

        if (!entity.getEmail().matches(emailRegex)) {
            return error(response, 400, "Invalid email format");
        }

        if (userRepository.existsByEmail(entity.getEmail())) {
            return error(response, 409, "Email already exists");
        }

        if (entity.getPhoneNumber() == null || entity.getPhoneNumber().trim().isEmpty()) {
            return error(response, 400, "Phone number is required");
        }

        if (!entity.getPhoneNumber().matches("^[0-9]{10}$")) {
            return error(response, 400, "Phone number must be exactly 10 digits");
        }

        if (userRepository.existsByPhoneNumber(entity.getPhoneNumber())) {
            return error(response, 409, "Phone number already exists");
        }

        if (entity.getPassword() == null || entity.getPassword().isEmpty()) {
            return error(response, 400, "Password is required");
        }

        if (entity.getPassword().length() < 7 || entity.getPassword().length() > 15) {
            return error(response, 400, "Password must be between 7 and 15 characters");
        }

        List<String> roles = List.of("super_admin", "admin", "customer", "seller", "delivery", "support", "manager",
                "finance", "guest");

        if (entity.getRole() == null || !roles.contains(entity.getRole())) {
            return error(response, 400, "Invalid role");
        }

        if (entity.getStatus() != null && !"ACTIVE".equalsIgnoreCase(entity.getStatus())) {
            return error(response, 400, "Status can only be ACTIVE");
        }

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        UserEntity saved = userRepository.save(entity);

        response.put("status", 201);
        response.put("message", "User created successfully");
        response.put("data", mapToResponse(saved));

        return response;
    }

    public Map<String, Object> getAllUsers() {
        List<UserResponseDTO> users = userRepository.findAll()
                .stream().map(this::mapToResponse).toList();

        return Map.of(
                "status", 200,
                "message", "Users fetched successfully",
                "data", users);
    }

    public Map<String, Object> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> Map.of(
                        "status", 200,
                        "message", "User found",
                        "data", mapToResponse(user)))
                .orElse(Map.of(
                        "status", 404,
                        "message", "User not found"));
    }

    public Map<String, Object> deleteUser(Long id) {

        Map<String, Object> response = new HashMap<>();

        UserEntity user = userRepository.findById(id).orElse(null);

        if (user == null)
            return error(response, 404, "User not found");

        if (user.getDeletedAt() != null)
            return error(response, 400, "User already deleted");

        user.setDeletedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        response.put("status", 200);
        response.put("message", "User deleted successfully");

        return response;
    }

    private Map<String, Object> error(Map<String, Object> res, int status, String msg) {
        res.put("status", status);
        res.put("message", msg);
        return res;
    }

    private UserResponseDTO mapToResponse(UserEntity user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}