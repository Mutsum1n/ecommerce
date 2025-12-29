package com.github.mutsum1n.ecommerce.controller;

import com.github.mutsum1n.ecommerce.entity.*;
import com.github.mutsum1n.ecommerce.service.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final UserActivityLogService activityLogService;

    public SellerController(ProductService productService, OrderService orderService,
                            UserService userService, UserActivityLogService activityLogService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/products")
    public String manageProducts(Model model, Authentication authentication) {
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        List<Product> products = productService.getProductsBySeller(seller);
        model.addAttribute("products", products);
        model.addAttribute("username", username);
        return "seller/products";
    }

    @GetMapping("/products/add")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "seller/product-form";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = productService.getProductById(id);
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        if (!product.getSeller().getId().equals(seller.getId())) {
            return "redirect:/seller/products";
        }
        model.addAttribute("product", product);
        return "seller/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, Authentication authentication) {
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        if (product.getId() == null) {
            productService.saveProductForSeller(product, seller);
        } else {
            productService.updateProduct(product.getId(), product);
        }
        return "redirect:/seller/products";
    }

    @PostMapping("/products/delete")
    public String deleteProduct(@RequestParam("id") Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            String username = authentication.getName();
            User seller = userService.findByUsername(username);

            if (product.getSeller().getId().equals(seller.getId())) {
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("success", "商品删除成功！");
            } else {
                redirectAttributes.addFlashAttribute("error", "您没有权限删除此商品！");
            }
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "删除失败：该商品已被订单引用，无法删除。<br>" +
                            "请先确认该商品没有被任何订单使用，或联系管理员处理。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败：" + e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @GetMapping("/orders")
    public String manageOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        List<Order> orders = orderService.getOrdersBySeller(seller.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("username", username);
        return "seller/orders";
    }

    @GetMapping("")
    public String sellerHome(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "seller/index";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                redirectAttributes.addFlashAttribute("error", "订单不存在");
                return "redirect:/seller/orders";
            }
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            orderService.updateOrderStatus(order, orderStatus);
            redirectAttributes.addFlashAttribute("success", "订单状态更新成功");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "无效的状态值: " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失败: " + e.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @GetMapping("/reports")
    public String salesReport(Model model, Authentication authentication) {
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        List<Order> sellerOrders = orderService.getOrdersBySeller(seller.getId());

        long totalOrders = sellerOrders.size();
        long paidOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long shippedOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count();
        long deliveredOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long completedOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelledOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long refundedOrders = sellerOrders.stream().filter(o -> o.getStatus() == OrderStatus.REFUNDED).count();

        Map<String, Long> orderStats = new HashMap<>();
        orderStats.put("totalOrders", totalOrders);
        orderStats.put("paidOrders", paidOrders);
        orderStats.put("shippedOrders", shippedOrders);
        orderStats.put("deliveredOrders", deliveredOrders);
        orderStats.put("completedOrders", completedOrders);
        orderStats.put("cancelledOrders", cancelledOrders);
        orderStats.put("refundedOrders", refundedOrders);

        BigDecimal todayRevenue = BigDecimal.ZERO;
        BigDecimal weekRevenue = BigDecimal.ZERO;
        BigDecimal monthRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        for (Order order : sellerOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
                continue;
            }
            List<OrderItem> sellerItems = order.getOrderItems().stream()
                    .filter(item -> item.getSeller() != null &&
                            item.getSeller().getId().equals(seller.getId()))
                    .collect(Collectors.toList());

            if (sellerItems.isEmpty()) {
                continue;
            }

            BigDecimal sellerAmountInOrder = sellerItems.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDateTime orderCreatedAt = order.getCreatedAt();

            totalRevenue = totalRevenue.add(sellerAmountInOrder);

            if (orderCreatedAt.isAfter(todayStart) && orderCreatedAt.isBefore(todayEnd)) {
                todayRevenue = todayRevenue.add(sellerAmountInOrder);
            }
            if (orderCreatedAt.isAfter(weekStart) && orderCreatedAt.isBefore(todayEnd)) {
                weekRevenue = weekRevenue.add(sellerAmountInOrder);
            }
            if (orderCreatedAt.isAfter(monthStart) && orderCreatedAt.isBefore(todayEnd)) {
                monthRevenue = monthRevenue.add(sellerAmountInOrder);
            }
        }
        model.addAttribute("orderStats", orderStats);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("weekRevenue", weekRevenue);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("username", username);
        return "seller/reports";
    }

    @GetMapping("/customers")
    public String manageCustomers(Model model, Authentication authentication) {
        String username = authentication.getName();
        User seller = userService.findByUsername(username);
        List<Order> sellerOrders = orderService.getOrdersBySeller(seller.getId());
        Set<User> customers = new LinkedHashSet<>();
        for (Order order : sellerOrders) {
            customers.add(order.getUser());
        }
        model.addAttribute("customers", new ArrayList<>(customers));
        model.addAttribute("username", username);
        return "seller/customers";
    }

    @GetMapping("/customers/{id}")
    public String customerDetail(@PathVariable Long id, Model model) {
        User customer = userService.getUserById(id);
        List<Order> customerOrders = orderService.getUserOrders(customer.getUsername());
        List<UserActivityLog> customerActivities = activityLogService.getUserActivities(customer.getUsername());
        model.addAttribute("customer", customer);
        model.addAttribute("orders", customerOrders);
        model.addAttribute("activities", customerActivities);

        return "seller/customer-detail";
    }

}