package com.github.mutsum1n.ecommerce.controller;

import com.github.mutsum1n.ecommerce.entity.Order;
import com.github.mutsum1n.ecommerce.service.OrderService;
import com.github.mutsum1n.ecommerce.service.CartService;
import com.github.mutsum1n.ecommerce.service.EmailService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/buyer")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final EmailService emailService;

    public OrderController(OrderService orderService, CartService cartService,
                           EmailService emailService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.emailService = emailService;
    }

    @GetMapping
    public String myOrders(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Order> orders = orderService.getUserOrders(username);
            model.addAttribute("orders", orders);
            model.addAttribute("username", username);
            return "buyer/orders";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "获取订单失败: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/create")
    public String createOrder(@RequestParam String shippingAddress,
                              @RequestParam String paymentMethod,
                              Authentication authentication) {
        try {
            String username = authentication.getName();
            var cartItems = cartService.getCartItems(username);
            if (cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            Order order = orderService.createOrderAndSendEmail(username, cartItems,
                    shippingAddress, paymentMethod);
            cartService.clearCart(username);
            return "redirect:/buyer?created=true";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cart?error=create_failed";
        }
    }
}