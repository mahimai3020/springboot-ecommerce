package com.springboot.springboot_ecommerce.user.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody

public class HomeController {

    @GetMapping
    public String getHomepage() {
        return "Welcome to Homepage";
    }

    @GetMapping("/dashboard")
    public String getdashboardpage() {
        return "Login Sucessfully";
    }

}
