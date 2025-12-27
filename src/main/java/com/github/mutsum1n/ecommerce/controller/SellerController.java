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
import java.util.List;
import java.util.Map;

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
    public String manageOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
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
    public String salesReport(Model model) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDateTime.now();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Map<String, Long> orderStats = orderService.getOrderStatistics();
        BigDecimal todayRevenue = orderService.getSalesRevenue(todayStart, todayEnd);
        BigDecimal weekRevenue = orderService.getSalesRevenue(weekStart, todayEnd);
        BigDecimal monthRevenue = orderService.getSalesRevenue(monthStart, todayEnd);
        Long activeUsersToday = activityLogService.getTodayActiveUsers();

        model.addAttribute("orderStats", orderStats);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("weekRevenue", weekRevenue);
        model.addAttribute("monthRevenue", monthRevenue);
        model.addAttribute("activeUsersToday", activeUsersToday);

        return "seller/reports";
    }

    @GetMapping("/customers")
    public String manageCustomers(Model model) {
        List<User> customers = userService.getAllBuyers();
        model.addAttribute("customers", customers);
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