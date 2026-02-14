package com.springboot.springboot_ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SpringbootEcommerceApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMalformed().ignoreIfMissing().load();
        if (dotenv.get("DB_URL") != null) {
            System.setProperty("DB_URL", dotenv.get("DB_URL"));
        }
        if (dotenv.get("DB_USER") != null) {
            System.setProperty("DB_USER", dotenv.get("DB_USER"));
        }
        if (dotenv.get("DB_PASS") != null) {
            System.setProperty("DB_PASS", dotenv.get("DB_PASS"));
        }

        SpringApplication.run(SpringbootEcommerceApplication.class, args);
    }
}
