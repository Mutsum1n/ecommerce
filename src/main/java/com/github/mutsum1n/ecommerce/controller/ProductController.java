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
            String username = "游客";
            User user = null;

            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
                user = userService.findByUsername(username);
            }

            Product product = productService.getProductById(id);
            if (product == null) {
                return "redirect:/products";
            }

            if (user != null) {
                activityLogService.logActivity(
                        user,
                        "VIEW_PRODUCT",
                        "浏览商品：" + product.getName()
                );
            }

            model.addAttribute("product", product);
            model.addAttribute("username", username);

            return "product-detail";
        } catch (Exception e) {
            return "redirect:/products";
        }
    }
}