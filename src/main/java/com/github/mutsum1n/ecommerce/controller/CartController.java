package com.github.mutsum1n.ecommerce.controller;

import com.github.mutsum1n.ecommerce.service.CartService;
import com.github.mutsum1n.ecommerce.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.github.mutsum1n.ecommerce.entity.CartItem;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping({"", "/"})
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<CartItem> cartItems = cartService.getCartItems(username);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("username", username);

        BigDecimal total = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalAmount", total);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            Authentication authentication) {

        String username = authentication.getName();
        cartService.addToCart(username, productId, quantity);
        return "redirect:/products";
    }

    @PostMapping("/update/{itemId}")
    public String updateCartItem(@PathVariable Long itemId,
                                 @RequestParam Integer quantity,
                                 Authentication authentication) {
        String username = authentication.getName();
        cartService.updateCartItemQuantity(username, itemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeCartItem(@PathVariable Long itemId,
                                 Authentication authentication) {
        String username = authentication.getName();
        cartService.removeFromCart(username, itemId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication) {
        String username = authentication.getName();
        cartService.clearCart(username);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Authentication authentication) {
        String username = authentication.getName();
        List<CartItem> cartItems = cartService.getCartItems(username);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return "redirect:/payment?totalAmount=" + totalAmount;
    }
}