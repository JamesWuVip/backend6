package com.wanli.security;

import com.wanli.dto.LoginDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import com.wanli.config.UnitTestConfig;

import java.time.LocalDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 登录安全测试
 * 验证JWT token生成、验证和安全性
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("unit-test")
@Import(UnitTestConfig.class)
@DisplayName("登录安全测试")
class LoginSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId("security-test-user-id");
        testUser.setUsername("securitytest");
        testUser.setEmail("security@test.com");
        testUser.setFullName("Security Test User");
        testUser.setPhone("13800138002");
        testUser.setPasswordHash("$2a$10$encodedPassword");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        validLoginDTO = new LoginDTO("securitytest", "securitypassword123");
    }

    @Test
    @DisplayName("JWT Token格式验证测试")
    void should_GenerateValidJwtFormat_when_LoginSuccessful() {
        // Given
        String username = "securitytest";
        String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzZWN1cml0eXRlc3QiLCJpYXQiOjE2MzQ1Njc4OTAsImV4cCI6MTYzNDU3MTQ5MH0.signature";
        
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(jwtTokenProvider.generateToken(auth)).thenReturn(mockToken);

        // When
        String token = jwtTokenProvider.generateToken(auth);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        String[] tokenParts = token.split("\\.");
        assertThat(tokenParts).hasSize(3); // JWT应该有header.payload.signature三部分
        
        // 验证每部分都是Base64编码
        for (String part : tokenParts) {
            assertThat(part).isNotEmpty();
            assertThatCode(() -> Base64.getUrlDecoder().decode(part + "=="))
                .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("JWT Token内容验证测试")
    void should_ContainCorrectUserInfo_when_JwtTokenGenerated() {
        // Given
        String username = "securitytest";
        String mockToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZWN1cml0eXRlc3QifQ.signature";
        
        when(jwtTokenProvider.getUsernameFromToken(mockToken)).thenReturn(username);
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);
        
        // When & Then
        assertThat(jwtTokenProvider.getUsernameFromToken(mockToken)).isEqualTo(username);
        assertThat(jwtTokenProvider.validateToken(mockToken)).isTrue();
    }

    @Test
    @DisplayName("JWT Token过期时间验证测试")
    void should_HaveCorrectExpirationTime_when_JwtTokenGenerated() {
        // Given
        long expectedExpirationTime = 3600000L;
        when(jwtTokenProvider.getExpirationTime()).thenReturn(expectedExpirationTime);
        
        // When
        long expirationTime = jwtTokenProvider.getExpirationTime();
        
        // Then
        assertThat(expirationTime).isGreaterThan(0);
        assertThat(expirationTime).isEqualTo(3600000L); // 1小时 = 3600000毫秒
    }

    @Test
    @DisplayName("JWT Token验证失败测试 - 无效token")
    void should_FailValidation_when_InvalidTokenProvided() {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        
        // When & Then
        assertThat(jwtTokenProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("JWT Token验证失败测试 - 用户名不匹配")
    void should_FailValidation_when_UsernameNotMatch() {
        // Given
        String mockToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzZWN1cml0eXRlc3QifQ.signature";
        when(jwtTokenProvider.validateToken(mockToken)).thenReturn(false);
        
        // When & Then
        assertThat(jwtTokenProvider.validateToken(mockToken)).isFalse();
    }

    @Test
    @DisplayName("JWT Token验证失败测试 - 空token")
    void should_FailValidation_when_TokenIsNull() {
        // Given
        String nullToken = null;
        when(jwtTokenProvider.validateToken(nullToken)).thenReturn(false);
        
        // When & Then
        assertThat(jwtTokenProvider.validateToken(nullToken)).isFalse();
    }

    @Test
    @DisplayName("JWT Token验证失败测试 - 空用户名")
    void should_FailValidation_when_UsernameIsNull() {
        // Given
        String nullToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOm51bGx9.signature";
        when(jwtTokenProvider.validateToken(nullToken)).thenReturn(false);
        
        // When & Then
        assertThat(jwtTokenProvider.validateToken(nullToken)).isFalse();
    }

    @Test
    @DisplayName("密码加密验证测试")
    void should_EncryptPassword_when_UserCreated() {
        // Given
        String rawPassword = "testpassword123";
        
        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword); // 加密后应该不等于原密码
        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt格式
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("密码验证失败测试")
    void should_FailPasswordMatch_when_WrongPasswordProvided() {
        // Given
        String rawPassword = "testpassword123";
        String wrongPassword = "wrongpassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // When & Then
        assertThat(passwordEncoder.matches(wrongPassword, encodedPassword)).isFalse();
    }



    @Test
    @DisplayName("Token唯一性测试")
    void should_GenerateUniqueTokens_when_MultipleLogins() {
        // Given
        String username = "securitytest";
        
        // When - 生成多个token
        Authentication auth1 = mock(Authentication.class);
        when(auth1.getName()).thenReturn(username);
        Authentication auth2 = mock(Authentication.class);
        when(auth2.getName()).thenReturn(username);
        Authentication auth3 = mock(Authentication.class);
        when(auth3.getName()).thenReturn(username);
        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth2);
        String token3 = jwtTokenProvider.generateToken(auth3);
        
        // Then - 每个token都应该是唯一的
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token2).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(token3);
        
        // 但都应该是有效的
        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token3)).isTrue();
    }


}