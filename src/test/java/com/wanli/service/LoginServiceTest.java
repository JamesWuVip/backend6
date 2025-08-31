package com.wanli.service;

import com.wanli.config.UnitTestConfig;
import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginService 单元测试
 * 使用 @ExtendWith(MockitoExtension.class) 进行Mock测试
 * 使用unit-test profile和UnitTestConfig配置
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("unit-test")
@Import(UnitTestConfig.class)
@DisplayName("LoginService 单元测试")
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginService loginService;

    private User mockUser;
    private LoginDTO validLoginDTO;
    private LoginDTO invalidLoginDTO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setFullName("Test User");
        mockUser.setPhone("13800138000");
        mockUser.setPasswordHash("$2a$10$encodedPassword");
        mockUser.setStatus(User.UserStatus.ACTIVE);
        mockUser.setCreatedAt(LocalDateTime.now().minusDays(30));
        mockUser.setUpdatedAt(LocalDateTime.now());
        mockUser.setLastLoginAt(LocalDateTime.now().minusDays(1));

        validLoginDTO = new LoginDTO("testuser", "password123");
        invalidLoginDTO = new LoginDTO("wronguser", "wrongpassword");
    }

    @Test
    @DisplayName("成功登录 - 返回JWT token和用户信息")
    void should_ReturnLoginResponse_when_ValidCredentialsProvided() {
        // Given
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        doNothing().when(userService).updateLastLoginTime("user-123");

        // When
        LoginResponseDTO result = loginService.login(validLoginDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        assertThat(result.getExpiresIn()).isEqualTo(3600000L);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo("user-123");
        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUser().getFullName()).isEqualTo("Test User");
        assertThat(result.getUser().getPhone()).isEqualTo("13800138000");
        assertThat(result.getUser().getStatus()).isEqualTo("ACTIVE");

        // 验证方法调用
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, times(1)).generateToken(mockAuth);
        verify(jwtTokenProvider, times(1)).getExpirationTime();
        verify(userService, times(1)).updateLastLoginTime("user-123");
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void should_ThrowBadCredentialsException_when_UserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("认证失败"));

        // When & Then
        assertThatThrownBy(() -> loginService.login(invalidLoginDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("用户名或密码错误");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void should_ThrowBadCredentialsException_when_PasswordIncorrect() {
        // Given
        LoginDTO wrongPasswordDTO = new LoginDTO("testuser", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("认证失败"));

        // When & Then
        assertThatThrownBy(() -> loginService.login(wrongPasswordDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("用户名或密码错误");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    @DisplayName("登录失败 - 用户状态非活跃")
    void should_ThrowBadCredentialsException_when_UserStatusInactive() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setId("user123");
        inactiveUser.setUsername("testuser");
        inactiveUser.setPasswordHash("$2a$10$encodedPassword");
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThatThrownBy(() -> loginService.login(validLoginDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("用户名或密码错误");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    @DisplayName("验证用户凭据 - 成功")
    void should_ReturnTrue_when_ValidateValidCredentials() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        // When
        boolean result = loginService.validateCredentials("testuser", "password123");

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("验证用户凭据 - 用户不存在返回false")
    void should_ReturnFalse_when_ValidateNonExistentUser() {
        // Given
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // When
        boolean result = loginService.validateCredentials("wronguser", "password123");

        // Then
        assertThat(result).isFalse();

        verify(userRepository, times(1)).findByUsername("wronguser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("验证用户凭据 - 失败")
    void should_ReturnFalse_when_ValidateInvalidCredentials() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        // When
        boolean result = loginService.validateCredentials("testuser", "wrongpassword");

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("验证用户凭据 - 用户状态非活跃仍返回true（只验证密码）")
    void should_ReturnTrue_when_ValidateInactiveUserCredentials() {
        // Given
        mockUser.setStatus(User.UserStatus.INACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        // When
        boolean result = loginService.validateCredentials("testuser", "password123");

        // Then
        assertThat(result).isTrue(); // validateCredentials只验证密码，不检查用户状态

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$encodedPassword");
    }

    @Test
    @DisplayName("数据库异常处理 - 抛出运行时异常")
    void should_ThrowRuntimeException_when_DatabaseError() {
        // Given
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername("testuser"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        assertThatThrownBy(() -> loginService.login(validLoginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("数据库连接失败");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    @DisplayName("检查用户状态 - 活跃用户返回true")
    void should_ReturnTrue_when_ValidateActiveUser() {
        // Given
        mockUser.setStatus(User.UserStatus.ACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        boolean result = loginService.isUserActiveByUsername("testuser");

        // Then
        assertThat(result).isTrue();
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("检查用户状态 - 非活跃用户返回false")
    void should_ReturnFalse_when_ValidateInactiveUser() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setUsername("testuser");
        inactiveUser.setStatus(User.UserStatus.INACTIVE);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When
        boolean result = loginService.isUserActiveByUsername("testuser");

        // Then
        assertThat(result).isFalse();
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("JWT生成异常处理 - 抛出运行时异常")
    void should_ThrowRuntimeException_when_JwtGenerationError() {
        // Given
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtTokenProvider.generateToken(mockAuth))
                .thenThrow(new RuntimeException("JWT生成失败"));

        // When & Then
        assertThatThrownBy(() -> loginService.login(validLoginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("JWT生成失败");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, times(1)).generateToken(mockAuth);
        verify(userService, never()).updateLastLoginTime(anyString());
    }
}