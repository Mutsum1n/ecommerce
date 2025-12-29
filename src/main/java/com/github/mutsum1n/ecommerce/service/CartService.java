package com.github.mutsum1n.ecommerce.service;

import com.github.mutsum1n.ecommerce.entity.CartItem;
import com.github.mutsum1n.ecommerce.entity.Product;
import com.github.mutsum1n.ecommerce.entity.User;
import com.github.mutsum1n.ecommerce.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, UserService userService, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productService = productService;
    }

    @Transactional
    public void addToCart(String username, Long productId, Integer quantity) {
        User user = userService.findByUsername(username);
        Product product = productService.getProductById(productId);

        CartItem existingItem = cartItemRepository.findByUserAndProduct(user, product);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public List<CartItem> getCartItems(String username) {
        User user = userService.findByUsername(username);
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public void updateCartItemQuantity(String username, Long itemId, Integer quantity) {
        User user = userService.findByUsername(username);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此购物车项");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public void removeFromCart(String username, Long itemId) {
        User user = userService.findByUsername(username);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此购物车项");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String username) {
        User user = userService.findByUsername(username);
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        cartItemRepository.deleteAll(cartItems);
    }
}