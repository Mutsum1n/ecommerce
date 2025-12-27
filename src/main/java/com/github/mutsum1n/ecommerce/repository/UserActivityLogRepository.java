package com.github.mutsum1n.ecommerce.repository;

import com.github.mutsum1n.ecommerce.entity.User;
import com.github.mutsum1n.ecommerce.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COUNT(DISTINCT ual.user) FROM UserActivityLog ual WHERE ual.createdAt BETWEEN :startDate AND :endDate")
    Long countDistinctUsersByActivityDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(ual) FROM UserActivityLog ual WHERE ual.createdAt BETWEEN :startDate AND :endDate")
    Long countActivitiesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}