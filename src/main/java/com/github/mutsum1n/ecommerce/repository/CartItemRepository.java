package com.github.mutsum1n.ecommerce.repository;

import com.github.mutsum1n.ecommerce.entity.CartItem;
import com.github.mutsum1n.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProduct(User user, com.github.mutsum1n.ecommerce.entity.Product product);
}