package com.wanli.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanli.dto.LoginDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录功能集成测试
 * 使用@SpringBootTest启动完整的Spring上下文
 * 测试各个组件之间的集成
 * 使用本地MySQL数据库和Redis进行集成测试
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUpTestData() {
        // 清理数据库
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhone("1234567890");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("成功登录 - 完整流程测试")
    void should_LoginSuccessfully_when_ValidCredentialsProvided() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.user.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.expirationTime").exists());
    }

    @Test
    @DisplayName("登录失败 - 用户名不存在")
    void should_ReturnUnauthorized_when_UserNotExists() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("nonexistentuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void should_ReturnUnauthorized_when_PasswordIncorrect() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("登录失败 - 用户状态非活跃")
    void should_ReturnUnauthorized_when_UserInactive() throws Exception {
        // Given
        testUser.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(testUser);
        
        LoginDTO request = new LoginDTO("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    @DisplayName("参数验证 - 用户名为空")
    void should_ReturnBadRequest_when_UsernameEmpty() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("参数验证失败"));
    }

    @Test
    @DisplayName("参数验证 - 密码为空")
    void should_ReturnBadRequest_when_PasswordEmpty() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("参数验证失败"));
    }

    @Test
    @DisplayName("参数验证 - 密码过短")
    void should_ReturnBadRequest_when_PasswordTooShort() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("参数验证失败"));
    }

    @Test
    @DisplayName("参数验证 - 用户名过长")
    void should_ReturnBadRequest_when_UsernameTooLong() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("a".repeat(51), "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("参数验证失败"));
    }

    @Test
    @DisplayName("JSON格式验证 - 无效JSON")
    void should_ReturnBadRequest_when_InvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Content-Type验证 - 缺少Content-Type")
    void should_ReturnUnsupportedMediaType_when_MissingContentType() throws Exception {
        // Given
        LoginDTO request = new LoginDTO("testuser", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }
}