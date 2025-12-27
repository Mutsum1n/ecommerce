package com.github.mutsum1n.ecommerce.controller;

import com.github.mutsum1n.ecommerce.entity.Order;
import com.github.mutsum1n.ecommerce.service.CartService;
import com.github.mutsum1n.ecommerce.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String paymentPage(@RequestParam("totalAmount") BigDecimal totalAmount,
                              Model model,
                              Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("username", username);
        return "payment";
    }

    @PostMapping("/complete")
    public String completePayment(@RequestParam BigDecimal totalAmount,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        try {
            // 获取当前用户的购物车商品
            var cartItems = cartService.getCartItems(username);

            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "购物车为空，无法完成支付");
                return "redirect:/cart";
            }

            // 创建订单并发送确认邮件
            Order order = orderService.createOrderAndSendEmail(
                    username,
                    cartItems,
                    "默认收货地址",  // 实际项目中应从表单获取
                    "在线支付"      // 实际项目中应根据支付方式确定
            );

            // 清空购物车
            cartService.clearCart(username);

            // 重定向到订单页面，显示成功消息
            redirectAttributes.addFlashAttribute("success",
                    String.format("支付成功！订单号：%s，我们已发送确认邮件到您的邮箱。", order.getOrderNumber()));
            return "redirect:/buyer";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "支付失败: " + e.getMessage());
            return "redirect:/payment?totalAmount=" + totalAmount;
        }
    }
}