package com.wanli.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/actuator")
public class HealthController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        boolean allHealthy = true;
        
        // 检查数据库连接
        Map<String, Object> dbHealth = checkDatabase();
        components.put("database", dbHealth);
        if (!"UP".equals(dbHealth.get("status"))) {
            allHealthy = false;
        }
        
        // 检查Redis连接
        Map<String, Object> redisHealth = checkRedis();
        components.put("redis", redisHealth);
        if (!"UP".equals(redisHealth.get("status"))) {
            allHealthy = false;
        }
        
        // 检查磁盘空间
        Map<String, Object> diskHealth = checkDiskSpace();
        components.put("diskSpace", diskHealth);
        if (!"UP".equals(diskHealth.get("status"))) {
            allHealthy = false;
        }
        
        response.put("status", allHealthy ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now());
        response.put("components", components);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 详细健康检查
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("maxMemory", Runtime.getRuntime().maxMemory());
        system.put("totalMemory", Runtime.getRuntime().totalMemory());
        system.put("freeMemory", Runtime.getRuntime().freeMemory());
        
        response.put("system", system);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            Connection connection = dataSource.getConnection();
            boolean isValid = connection.isValid(5); // 5秒超时
            connection.close();
            
            result.put("status", isValid ? "UP" : "DOWN");
            result.put("database", "MySQL");
            if (isValid) {
                result.put("details", "Database connection is healthy");
            } else {
                result.put("details", "Database connection validation failed");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to connect to database");
        }
        return result;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            connection.close();
            
            boolean isHealthy = "PONG".equals(pong);
            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("redis", "Redis");
            if (isHealthy) {
                result.put("details", "Redis connection is healthy");
            } else {
                result.put("details", "Redis ping failed");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to connect to Redis");
        }
        return result;
    }

    /**
     * 检查磁盘空间
     */
    private Map<String, Object> checkDiskSpace() {
        Map<String, Object> result = new HashMap<>();
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercentage = (double) usedSpace / totalSpace * 100;
            
            boolean isHealthy = usagePercentage < 90; // 磁盘使用率小于90%认为健康
            
            result.put("status", isHealthy ? "UP" : "DOWN");
            result.put("total", totalSpace);
            result.put("free", freeSpace);
            result.put("used", usedSpace);
            result.put("usagePercentage", Math.round(usagePercentage * 100.0) / 100.0);
            
            if (isHealthy) {
                result.put("details", "Disk space is sufficient");
            } else {
                result.put("details", "Disk space is running low");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            result.put("details", "Failed to check disk space");
        }
        return result;
    }

    @Override
    public Health health() {
        try {
            // 检查数据库
            Connection connection = dataSource.getConnection();
            boolean dbHealthy = connection.isValid(5);
            connection.close();
            
            // 检查Redis
            RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
            boolean redisHealthy = "PONG".equals(redisConnection.ping());
            redisConnection.close();
            
            if (dbHealthy && redisHealthy) {
                return Health.up()
                        .withDetail("database", "UP")
                        .withDetail("redis", "UP")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", dbHealthy ? "UP" : "DOWN")
                        .withDetail("redis", redisHealthy ? "UP" : "DOWN")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}