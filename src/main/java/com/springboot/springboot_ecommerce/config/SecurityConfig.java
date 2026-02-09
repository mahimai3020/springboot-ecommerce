package com.springboot.springboot_ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()

                // .requestMatchers(HttpMethod.POST, "/api/users/create").permitAll()
                // .requestMatchers(HttpMethod.PUT, "/api/users/update").permitAll()
                // .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
                // .requestMatchers("/api/users/**").authenticated()

                // .requestMatchers("/").permitAll() // allow public access
                // .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());
        // .formLogin(form -> form.permitAll().defaultSuccessUrl("/dashboard"));

        return http.build();
    }

    // @Bean
    // public UserDetailsService userDetailsService(PasswordEncoder encoder) {

    // // UserDetails user = User
    // // .withUsername("imbino")
    // // .password(encoder.encode("imbino@123"))
    // // .roles("guest")
    // // .build();
    // // UserDetails admin = User
    // // .withUsername("imadmin")
    // // .password(encoder.encode("imadmin@123"))
    // // .roles("admin")
    // // .build();

    // // return new InMemoryUserDetailsManager(user, admin);

    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
