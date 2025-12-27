package com.github.mutsum1n.ecommerce.repository;

import com.github.mutsum1n.ecommerce.entity.Order;
import com.github.mutsum1n.ecommerce.entity.OrderStatus;
import com.github.mutsum1n.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByOrderNumber(String orderNumber);
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status IN ('PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED') AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE p.seller.id = :sellerId")
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);
}