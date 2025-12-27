package com.github.mutsum1n.ecommerce.service;

import com.github.mutsum1n.ecommerce.entity.User;
import com.github.mutsum1n.ecommerce.entity.UserActivityLog;
import com.github.mutsum1n.ecommerce.repository.UserActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserActivityLogService {
    private final UserActivityLogRepository activityLogRepository;
    private final UserService userService;

    public UserActivityLogService(UserActivityLogRepository activityLogRepository,
                                  UserService userService) {
        this.activityLogRepository = activityLogRepository;
        this.userService = userService;
    }

    @Transactional
    public void logActivity(User user, String activityType, String details) {
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setActivityType(activityType);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    @Transactional
    public void logProductView(User user, Long productId, String productName) {
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setActivityType("VIEW_PRODUCT");
        log.setProductId(productId);
        log.setDetails("浏览商品：" + productName);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    public List<UserActivityLog> getUserActivities(String username) {
        User user = userService.findByUsername(username);
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Long getTodayActiveUsers() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        return activityLogRepository.countDistinctUsersByActivityDate(startOfDay, endOfDay);
    }
}