package com.wanli.service;

import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 登录服务类
 * 处理用户登录相关的业务逻辑
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@Service
@Transactional
public class LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求数据
     * @return 登录响应数据
     * @throws BadCredentialsException 认证失败时抛出
     */
    public LoginResponseDTO login(LoginDTO loginDTO) {
        try {
            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );

            // 查找用户
            Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());
            if (userOpt.isEmpty()) {
                throw new BadCredentialsException("用户不存在");
            }

            User user = userOpt.get();
            
            // 检查用户状态
            if (!"ACTIVE".equals(user.getStatus().toString())) {
                throw new BadCredentialsException("用户账户已被禁用");
            }

            // 生成JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            long expirationTime = jwtTokenProvider.getExpirationTime();
            LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationTime / 1000);

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            // 构建用户信息DTO
            LoginResponseDTO.UserInfoDto userInfo = new LoginResponseDTO.UserInfoDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getStatus().toString(),
                user.getCreatedAt(),
                user.getUpdatedAt()
            );

            // 返回登录响应
            LoginResponseDTO response = new LoginResponseDTO(token, userInfo, expirationTime);
            response.setExpirationTime(expirationDateTime);
            return response;

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 验证用户凭据
     * 
     * @param username 用户名
     * @param password 密码
     * @return 验证结果
     */
    public boolean validateCredentials(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    /**
     * 检查用户是否存在且状态为活跃
     * 
     * @param username 用户名
     * @return 检查结果
     */
    public boolean isUserActiveByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return "ACTIVE".equals(user.getStatus().toString());
    }
}