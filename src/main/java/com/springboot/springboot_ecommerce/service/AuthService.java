package com.springboot.springboot_ecommerce.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springboot.springboot_ecommerce.entity.UserEntity;
import com.springboot.springboot_ecommerce.repository.UserRepository;
import com.springboot.springboot_ecommerce.security.JwtUtil;
import com.springboot.springboot_ecommerce.exception.ApiException;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ===============================
    // LOGIN
    // ===============================
    public UserEntity login(String usernameOrEmail, String password) {

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Optional<UserEntity> userOpt = repo.findByEmail(usernameOrEmail);

        if (userOpt.isEmpty()) {
            userOpt = repo.findByUserName(usernameOrEmail);
        }

        if (userOpt.isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UserEntity user = userOpt.get();

        if (!encoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // reuse token
        if (user.getToken() == null) {
            String newToken = jwtUtil.generateToken(user.getEmail());
            user.setToken(newToken);
            repo.save(user);
        }

        return user;
    }

    // ===============================
    // LOGOUT
    // ===============================
    public void logout(Long id, String token) {

        UserEntity user = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getToken() == null || !user.getToken().equals(token)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        user.setToken(null);
        repo.save(user);
    }

    // reset password

    public void resetPassword(String email, String password, String confirmPassword) {

        // email validation
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        // password validation
        if (password == null || password.isEmpty()
                || confirmPassword == null || confirmPassword.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        // match check

        UserEntity user = repo.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!password.equals(confirmPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        // encode & save
        user.setPassword(encoder.encode(password));

        // optional → force logout after reset
        user.setToken(null);

        repo.save(user);
    }
}
