package com.springboot.springboot_ecommerce.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET_STRING = "my-super-secret-key-for-jwt-authentication-2026";

    private final SecretKey SECRET = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // ===============================
    // GENERATE TOKEN (NO EXPIRY)
    // ===============================
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .signWith(SECRET)
                .compact();
    }

    // ===============================
    // EXTRACT EMAIL
    // ===============================
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(SECRET)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
