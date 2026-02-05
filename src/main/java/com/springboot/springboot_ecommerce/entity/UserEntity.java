package com.springboot.springboot_ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // 🔑 Token (NULL on create)
    @Column(length = 500)
    private String token;

    @Column(nullable = false)
    private String role;

    // ✅ Default status
    @Column(nullable = false)
    private String status;

    // ===============================
    // JPA LIFECYCLE CALLBACK
    // ===============================
    @PrePersist
    public void prePersist() {
        // token should be NULL when user is created
        this.token = null;

        // default status
        if (this.status == null) {
            this.status = "INACTIVE";
        }
    }

    // ===== Constructors =====
    public UserEntity() {
    }

    public UserEntity(Long id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ⚠️ password must be ENCODED before save
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // token handled internally (login)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    // default role can be set from service/controller
    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    // allow manual status change (ADMIN use-case)
    public void setStatus(String status) {
        this.status = status;
    }
}
