package com.springboot.springboot_ecommerce.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // display name
    @Column(nullable = false)
    private String name;

    // login username
    @Column(name = "user_name", unique = true, nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    // 🔑 Token
    @Column(length = 500)
    private String token;

    @Column(nullable = false)
    private String role;

    // ACTIVE / INACTIVE
    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // ===============================
    // SINGLE CALLBACK
    // ===============================
    @PrePersist
    public void beforeSave() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        // token default
        this.token = null;

        if (this.status == null) {
            this.status = "INACTIVE";
        }
    }

    // ===== Constructors =====
    public UserEntity() {
    }

    public UserEntity(Long id, String name, String userName, String email,
            String password, String role, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    // encode in service
    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
