package com.springboot.springboot_ecommerce.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.springboot_ecommerce.entity.UserEntity;
import com.springboot.springboot_ecommerce.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserEntity user) {

        Map<String, Object> response = new HashMap<>();

        // ---------- ID VALIDATION ----------
        
        if (user.getId() != null) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "ID should not be provided");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ---------- NAME VALIDATION ----------

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Name is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getName().length() < 3 || user.getName().length() > 20) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Name must be between 3 and 20 characters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ---------- EMAIL VALIDATION ----------

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Email is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String emailRegex = "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$";

        if (!user.getEmail().matches(emailRegex)) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Invalid email format (example@gmail.com)");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ---------- PASSWORD VALIDATION ----------

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Password is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (user.getPassword().length() < 7 || user.getPassword().length() > 15) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Password must be between 7 and 15 characters");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ---------- ROLE VALIDATION ----------

        List<String> allowedRoles = List.of(
                "super_admin", "admin", "customer", "seller",
                "delivery", "support", "manager", "finance", "guest");

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Role is required");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!allowedRoles.contains(user.getRole())) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Invalid role");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // ---------- STATUS VALIDATION ----------

        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Status can only be ACTIVE");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // If status is null → @PrePersist sets INACTIVE

        // ---------- UNIQUE NAME CHECK ----------

        if (userRepository.existsByName(user.getName())) {
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("message", "User name already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        // ---------- UNIQUE EMAIL CHECK ----------

        if (userRepository.existsByEmail(user.getEmail())) {
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("message", "User email already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        // ---------- SAVE USER ----------

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity savedUser = userRepository.save(user);

        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "User created successfully");
        response.put("data", savedUser);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Users fetched successfully");
        response.put("data", users);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.OK.value());
                    response.put("message", "User found");
                    response.put("data", user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.NOT_FOUND.value());
                    response.put("message", "User not found");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    // UPDATE USER

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity userDetails) {

        return userRepository.findById(id)
                .map(user -> {

                    boolean isUpdated = false;

                    // Update name only if provided AND changed

                    if (userDetails.getName() != null &&
                            !userDetails.getName().equals(user.getName())) {
                        user.setName(userDetails.getName());
                        isUpdated = true;
                    }

                    // Update email only if provided AND changed

                    if (userDetails.getEmail() != null &&
                            !userDetails.getEmail().equals(user.getEmail())) {
                        user.setEmail(userDetails.getEmail());
                        isUpdated = true;
                    }

                    Map<String, Object> response = new HashMap<>();

                    // Nothing changed

                    if (!isUpdated) {
                        response.put("status", HttpStatus.OK.value());
                        response.put("message", "Nothing is updated");
                        return ResponseEntity.ok(response);
                    }

                    // Save only if something changed

                    UserEntity updatedUser = userRepository.save(user);

                    response.put("status", HttpStatus.OK.value());
                    response.put("message", "User updated successfully");
                    response.put("data", updatedUser);

                    return ResponseEntity.ok(response);

                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", HttpStatus.NOT_FOUND.value());
                    response.put("message", "User not found");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    // DELETE USER

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {

        if (!userRepository.existsById(id)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("message", "User not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        userRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "User deleted successfully");

        return ResponseEntity.ok(response);

    }
}
