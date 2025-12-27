package com.github.mutsum1n.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // 将根路径重定向到商品页面
        return "redirect:/products";
    }
}