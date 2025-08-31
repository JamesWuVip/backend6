package com.wanli.controller;

import com.wanli.entity.User;
import com.wanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用户控制器
 * 提供用户相关的REST API接口
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        try {
            User createdUser = userService.createUser(user);
            // 不返回密码哈希
            createdUser.setPasswordHash(null);
            
            response.put("success", true);
            response.put("message", "用户创建成功");
            response.put("data", createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userService.findById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 不返回密码哈希
            user.setPasswordHash(null);
            
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 不返回密码哈希
            user.setPasswordHash(null);
            
            response.put("success", true);
            response.put("data", user);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 分页查询用户列表
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向（asc/desc）
     * @param status 用户状态过滤
     * @return 用户分页列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) User.UserStatus status) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<User> userPage;
            if (status != null) {
                userPage = userService.findByStatus(status, pageable);
            } else {
                userPage = userService.findAll(pageable);
            }
            
            // 清除密码哈希
            userPage.getContent().forEach(user -> user.setPasswordHash(null));
            
            Map<String, Object> pageInfo = new HashMap<>();
            pageInfo.put("content", userPage.getContent());
            pageInfo.put("totalElements", userPage.getTotalElements());
            pageInfo.put("totalPages", userPage.getTotalPages());
            pageInfo.put("currentPage", userPage.getNumber());
            pageInfo.put("size", userPage.getSize());
            pageInfo.put("hasNext", userPage.hasNext());
            pageInfo.put("hasPrevious", userPage.hasPrevious());
            
            response.put("success", true);
            response.put("data", pageInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param user 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable String id, 
            @Valid @RequestBody User user) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = userService.updateUser(id, user);
            // 不返回密码哈希
            updatedUser.setPasswordHash(null);
            
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 更新用户密码
     * 
     * @param id 用户ID
     * @param passwordRequest 密码更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @PathVariable String id,
            @RequestBody Map<String, String> passwordRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            response.put("success", false);
            response.put("message", "旧密码和新密码不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            boolean success = userService.updatePassword(id, oldPassword, newPassword);
            if (success) {
                response.put("success", true);
                response.put("message", "密码更新成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "旧密码验证失败");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param statusRequest 状态更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        String statusStr = statusRequest.get("status");
        if (statusStr == null) {
            response.put("success", false);
            response.put("message", "状态不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            User.UserStatus status = User.UserStatus.valueOf(statusStr.toUpperCase());
            User updatedUser = userService.updateUserStatus(id, status);
            // 不返回密码哈希
            updatedUser.setPasswordHash(null);
            
            response.put("success", true);
            response.put("message", "用户状态更新成功");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "无效的用户状态");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 验证邮箱
     * 
     * @param id 用户ID
     * @return 验证结果
     */
    @PutMapping("/{id}/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = userService.verifyEmail(id);
            // 不返回密码哈希
            updatedUser.setPasswordHash(null);
            
            response.put("success", true);
            response.put("message", "邮箱验证成功");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 验证手机号
     * 
     * @param id 用户ID
     * @return 验证结果
     */
    @PutMapping("/{id}/verify-phone")
    public ResponseEntity<Map<String, Object>> verifyPhone(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User updatedUser = userService.verifyPhone(id);
            // 不返回密码哈希
            updatedUser.setPasswordHash(null);
            
            response.put("success", true);
            response.put("message", "手机号验证成功");
            response.put("data", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 删除用户（软删除）
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(id);
            response.put("success", true);
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @RequestParam @NotBlank String username) {
        
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.existsByUsername(username);
        
        response.put("success", true);
        response.put("exists", exists);
        response.put("message", exists ? "用户名已存在" : "用户名可用");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestParam @NotBlank String email) {
        
        Map<String, Object> response = new HashMap<>();
        boolean exists = userService.existsByEmail(email);
        
        response.put("success", true);
        response.put("exists", exists);
        response.put("message", exists ? "邮箱已存在" : "邮箱可用");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            statistics.put("totalUsers", userService.countUsers());
            statistics.put("activeUsers", userService.countByStatus(User.UserStatus.ACTIVE));
            statistics.put("inactiveUsers", userService.countByStatus(User.UserStatus.INACTIVE));
            statistics.put("deletedUsers", userService.countByStatus(User.UserStatus.DELETED));
            
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}