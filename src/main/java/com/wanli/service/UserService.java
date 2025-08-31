package com.wanli.service;

import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户服务类
 * 提供用户相关的业务逻辑处理
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建新用户
     * 
     * @param user 用户信息
     * @return 创建的用户
     */
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 设置默认值
        user.setId(UUID.randomUUID().toString());
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 分页查询用户列表
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 根据状态分页查询用户
     * 
     * @param status 用户状态
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Transactional(readOnly = true)
    public Page<User> findByStatus(User.UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param user 更新的用户信息
     * @return 更新后的用户信息
     */
    public User updateUser(String id, User user) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User existingUser = existingUserOpt.get();
        
        // 更新允许修改的字段
        if (user.getFullName() != null) {
            existingUser.setFullName(user.getFullName());
        }
        if (user.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }

    /**
     * 更新用户密码
     * 
     * @param id 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    public boolean updatePassword(String id, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return false;
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        return true;
    }

    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 新状态
     * @return 更新后的用户信息
     */
    public User updateUserStatus(String id, User.UserStatus status) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * 验证邮箱
     * 
     * @param id 用户ID
     * @return 更新后的用户信息
     */
    public User verifyEmail(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * 验证手机号
     * 
     * @param id 用户ID
     * @return 更新后的用户信息
     */
    public User verifyPhone(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setPhoneVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * 更新最后登录时间
     * 
     * @param id 用户ID
     */
    public void updateLastLoginTime(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    /**
     * 软删除用户
     * 
     * @param id 用户ID
     */
    public void deleteUser(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(User.UserStatus.DELETED);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * 根据状态统计用户数量
     * 
     * @param status 用户状态
     * @return 用户数量
     */
    @Transactional(readOnly = true)
    public long countByStatus(User.UserStatus status) {
        return userRepository.countByStatus(status);
    }
}