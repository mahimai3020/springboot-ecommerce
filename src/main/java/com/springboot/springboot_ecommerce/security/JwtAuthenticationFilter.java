package com.springboot.springboot_ecommerce.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springboot.springboot_ecommerce.user.entity.UserEntity;
import com.springboot.springboot_ecommerce.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository repo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                String email = jwtUtil.extractEmail(token);

                UserEntity user = repo.findByEmail(email).orElse(null);

                //  MUST CHECK

                if (user != null && token.equals(user.getToken())) {

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, new java.util.ArrayList<>());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // token invalid → ignore
            }
        }

        filterChain.doFilter(request, response);
    }
}
