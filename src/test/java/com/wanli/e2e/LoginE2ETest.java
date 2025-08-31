package com.wanli.e2e;

import com.wanli.dto.LoginDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * 登录功能端到端测试
 * 使用 TestRestTemplate 进行真实HTTP请求测试
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("登录功能端到端测试")
class LoginE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private User testUser;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        
        // 清理数据库
        userRepository.deleteAll();

        // 创建测试用户
        testUser = new User();
        testUser.setId("e2e-test-user-id");
        testUser.setUsername("e2etest");
        testUser.setEmail("e2e@test.com");
        testUser.setFullName("E2E Test User");
        testUser.setPhone("13800138001");
        testUser.setPasswordHash(passwordEncoder.encode("e2epassword123"));
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        testUser = userRepository.save(testUser);

        validLoginDTO = new LoginDTO("e2etest", "e2epassword123");
    }

    @Test
    @DisplayName("端到端登录测试 - 成功登录")
    void should_LoginSuccessfully_when_ValidCredentialsProvided() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);
        assertThat(responseBody.get("data")).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        assertThat(data.get("token")).isNotNull();
        assertThat(data.get("token").toString()).isNotEmpty();
        assertThat(data.get("expiresIn")).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) data.get("user");
        assertThat(user.get("id")).isEqualTo(testUser.getId());
        assertThat(user.get("username")).isEqualTo("e2etest");
        assertThat(user.get("email")).isEqualTo("e2e@test.com");
        assertThat(user.get("fullName")).isEqualTo("E2E Test User");
        assertThat(user.get("phone")).isEqualTo("13800138001");
        assertThat(user.get("status")).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("端到端登录测试 - 用户名不存在")
    void should_FailLogin_when_UsernameNotExists() {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO("nonexistentuser", "e2epassword123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(invalidLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("code")).isEqualTo(2001);
        assertThat(responseBody.get("message")).isEqualTo("用户名或密码错误");
    }

    @Test
    @DisplayName("端到端登录测试 - 密码错误")
    void should_FailLogin_when_PasswordIncorrect() {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO("e2etest", "wrongpassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(invalidLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("code")).isEqualTo(2001);
        assertThat(responseBody.get("message")).isEqualTo("用户名或密码错误");
    }

    @Test
    @DisplayName("端到端登录测试 - 用户状态非活跃")
    void should_FailLogin_when_UserStatusInactive() {
        // Given - 更新用户状态为非活跃
        testUser.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(testUser);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("code")).isEqualTo(2002);
        assertThat(responseBody.get("message")).isEqualTo("用户账户已被禁用");
    }

    @Test
    @DisplayName("端到端参数验证测试 - 用户名为空")
    void should_FailValidation_when_UsernameIsEmpty() {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO("", "e2epassword123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(invalidLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("code")).isEqualTo(4000);
    }

    @Test
    @DisplayName("端到端参数验证测试 - 密码为空")
    void should_FailValidation_when_PasswordIsEmpty() {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO("e2etest", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(invalidLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("code")).isEqualTo(4000);
    }

    @Test
    @DisplayName("端到端验证用户凭据测试 - 成功验证")
    void should_ValidateCredentials_when_ValidCredentialsProvided() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/validate", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);
        assertThat(responseBody.get("data")).isEqualTo(true);
    }

    @Test
    @DisplayName("端到端验证用户凭据测试 - 验证失败")
    void should_FailValidateCredentials_when_InvalidCredentialsProvided() {
        // Given
        LoginDTO invalidLoginDTO = new LoginDTO("e2etest", "wrongpassword");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(invalidLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/validate", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);
        assertThat(responseBody.get("data")).isEqualTo(false);
    }

    @Test
    @DisplayName("端到端获取登录状态测试")
    void should_ReturnLoginStatus_when_GetStatusEndpoint() {
        // When
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/status", Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(true);
        assertThat(responseBody.get("data")).isEqualTo("登录API服务正常运行");
    }

    @Test
    @DisplayName("端到端HTTP头部测试 - Content-Type验证")
    void should_ReturnCorrectContentType_when_LoginRequest() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString())
            .contains(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    @DisplayName("端到端错误处理测试 - 无效JSON格式")
    void should_HandleInvalidJson_when_MalformedRequest() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String invalidJson = "{\"username\": \"test\", \"password\":}";
        HttpEntity<String> request = new HttpEntity<>(invalidJson, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("端到端性能测试 - 登录响应时间")
    void should_RespondWithinReasonableTime_when_LoginRequest() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseTime).isLessThan(5000); // 响应时间应小于5秒
    }
}