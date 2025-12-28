package com.github.mutsum1n.ecommerce.service;

import com.github.mutsum1n.ecommerce.entity.*;
import com.github.mutsum1n.ecommerce.repository.OrderRepository;
import com.github.mutsum1n.ecommerce.repository.OrderItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final ProductService productService;
    private final UserActivityLogService userActivityLogService;
    private final EmailService emailService;  // 邮件服务

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        UserService userService,
                        ProductService productService,
                        UserActivityLogService userActivityLogService,
                        EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.productService = productService;
        this.userActivityLogService = userActivityLogService;
        this.emailService = emailService;  // 注入邮件服务
    }

    @Transactional
    public Order createOrderFromCart(String username, List<CartItem> cartItems,
                                     String shippingAddress, String paymentMethod) {
        User user = userService.findByUsername(username);

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String orderNumber = generateOrderNumber();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus("已支付");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(savedOrder);
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(cartItem.getProduct().getPrice());
                    orderItem.setSubtotal(cartItem.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())));

                    // 强制设置卖家ID
                    if (cartItem.getProduct().getSeller() != null) {
                        orderItem.setSeller(cartItem.getProduct().getSeller());
                    } else {
                        // 如果商品没有卖家，设为当前用户（通常是买家）
                        // 但这不应该发生，商品必须有卖家
                        orderItem.setSeller(cartItem.getProduct().getSeller());
                    }

                    Product product = cartItem.getProduct();
                    if (product.getStock() < cartItem.getQuantity()) {
                        throw new RuntimeException("商品库存不足: " + product.getName());
                    }
                    product.setStock(product.getStock() - cartItem.getQuantity());
                    productService.saveProduct(product);

                    return orderItem;
                })
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        userActivityLogService.logActivity(user, "PURCHASE",
                "购买了订单：" + savedOrder.getOrderNumber());

        return savedOrder;
    }
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    @Transactional
    public Order createOrderAndSendEmail(String username, List<CartItem> cartItems,
                                         String shippingAddress, String paymentMethod) {
        Order order = createOrderFromCart(username, cartItems, shippingAddress, paymentMethod);

        User user = order.getUser();

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.warn("用户 {} 没有邮箱，跳过邮件发送", user.getUsername());
            return order;
        }

        try {
            String customerName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            String totalAmount = "¥" + order.getTotalAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
            String orderDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            log.info("为订单 {} 发送确认邮件给用户 {} ({})",
                    order.getOrderNumber(), user.getUsername(), user.getEmail());

            emailService.sendOrderConfirmation(
                    user.getEmail(),
                    order.getOrderNumber(),
                    customerName,
                    totalAmount,
                    orderDate
            );

            log.info("订单 {} 的邮件已成功发送", order.getOrderNumber());

        } catch (Exception e) {
            log.error("发送订单 {} 的邮件失败，但不影响订单创建: {}",
                    order.getOrderNumber(), e.getMessage(), e);
        }

        return order;
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().toString().replace("-", "");
        String randomPart = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD" + datePart + randomPart;
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public List<Order> getUserOrders(String username) {
        User user = userService.findByUsername(username);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void updateOrderStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        if (status == OrderStatus.CANCELLED || status == OrderStatus.REFUNDED) {
            restoreStock(order);
        }

        orderRepository.save(order);
    }

    private void restoreStock(Order order) {
        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productService.saveProduct(product);
        }
    }

    public Map<String, Long> getOrderStatistics() {
        return Map.of(
                "totalOrders", orderRepository.count(),
                "paidOrders", orderRepository.countByStatus(OrderStatus.PAID),
                "shippedOrders", orderRepository.countByStatus(OrderStatus.SHIPPED),
                "deliveredOrders", orderRepository.countByStatus(OrderStatus.DELIVERED),
                "cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED),
                "refundedOrders", orderRepository.countByStatus(OrderStatus.REFUNDED)
        );
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在：" + id));
    }
}