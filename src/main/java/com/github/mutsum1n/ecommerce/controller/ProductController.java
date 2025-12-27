package com.github.mutsum1n.ecommerce.controller;

import com.github.mutsum1n.ecommerce.entity.Product;
import com.github.mutsum1n.ecommerce.entity.User;
import com.github.mutsum1n.ecommerce.service.ProductService;
import com.github.mutsum1n.ecommerce.service.UserActivityLogService;
import com.github.mutsum1n.ecommerce.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UserService userService;
    private final UserActivityLogService activityLogService;

    public ProductController(ProductService productService,
                             UserService userService,
                             UserActivityLogService activityLogService) {
        this.productService = productService;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/products")
    public String productList(
            Model model,
            Authentication authentication,
            @RequestParam(value = "keyword", required = false) String keyword) {

        // 处理用户未登录的情况
        String username = "游客";
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
        }

        model.addAttribute("products", productService.searchProducts(keyword));
        model.addAttribute("username", username);
        model.addAttribute("keyword", keyword);
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(
            @PathVariable Long id,
            Model model,
            Authentication authentication) {
        try {
            // 处理用户信息
            String username = "游客";
            User user = null;

            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
                user = userService.findByUsername(username);
            }

            // 获取商品信息
            Product product = productService.getProductById(id);
            if (product == null) {
                return "redirect:/products";
            }

            // 如果用户已登录，记录活动日志
            if (user != null) {
                activityLogService.logActivity(
                        user,
                        "VIEW_PRODUCT",
                        "浏览商品：" + product.getName()
                );
            }

            // 添加商品信息和用户信息到模型
            model.addAttribute("product", product);
            model.addAttribute("username", username);

            return "product-detail";
        } catch (Exception e) {
            // 如果出错，重定向到商品列表页
            return "redirect:/products";
        }
    }
}