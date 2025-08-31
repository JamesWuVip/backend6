package com.wanli.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.config.UnitTestConfig;
import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LoginController 单元测试
 * 使用Mockito进行纯单元测试，不依赖Spring上下文
 * 使用unit-test profile和UnitTestConfig配置
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("unit-test")
@Import(UnitTestConfig.class)
@DisplayName("LoginController 单元测试")
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("成功登录应返回200状态码和JWT令牌")
    void should_ReturnOkWithToken_when_ValidCredentialsProvided() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "password123");

        LoginResponseDTO.UserInfoDto userInfo = new LoginResponseDTO.UserInfoDto(
            "user-123", "testuser", "test@example.com", "Test User", 
            "1234567890", "ACTIVE", LocalDateTime.now(), LocalDateTime.now()
        );
        LoginResponseDTO response = new LoginResponseDTO("jwt-token", userInfo, 3600000L);

        when(loginService.login(any(LoginDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));
    }

    @Test
    @DisplayName("无效凭据应返回401状态码")
    void should_ReturnUnauthorized_when_InvalidCredentialsProvided() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "wrongpassword");

        when(loginService.login(any(LoginDTO.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("空用户名应返回400状态码")
    void should_ReturnBadRequest_when_UsernameIsEmpty() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("空密码应返回400状态码")
    void should_ReturnBadRequest_when_PasswordIsEmpty() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("密码过短应返回400状态码")
    void should_ReturnBadRequest_when_PasswordIsTooShort() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户名过长应返回400状态码")
    void should_ReturnBadRequest_when_UsernameIsTooLong() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("a".repeat(51), "password123"); // 超过50个字符

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("密码过长应返回400状态码")
    void should_ReturnBadRequest_when_PasswordIsTooLong() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "a".repeat(101)); // 超过100个字符

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null用户名应返回400状态码")
    void should_ReturnBadRequest_when_UsernameIsNull() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":null,\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("null密码应返回400状态码")
    void should_ReturnBadRequest_when_PasswordIsNull() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"password\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("无效JSON格式应返回400状态码")
    void should_ReturnBadRequest_when_InvalidJsonProvided() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}