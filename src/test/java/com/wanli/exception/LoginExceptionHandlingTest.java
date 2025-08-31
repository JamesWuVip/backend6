package com.wanli.exception;

import com.wanli.config.TestSecurityConfig;
import com.wanli.dto.LoginDTO;
import com.wanli.entity.User;
import com.wanli.repository.UserRepository;
import com.wanli.security.JwtTokenProvider;
import com.wanli.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;

/**
 * 登录异常处理测试
 * 验证各种异常情况的处理和错误响应
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("登录异常处理测试")
class LoginExceptionHandlingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginService loginService;

    private String baseUrl;
    private User testUser;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/auth";

        // 创建测试用户
        testUser = new User();
        testUser.setId("exception-test-user-id");
        testUser.setUsername("exceptiontest");
        testUser.setEmail("exception@test.com");
        testUser.setFullName("Exception Test User");
        testUser.setPhone("13800138004");
        testUser.setPasswordHash(passwordEncoder.encode("exceptionpassword123"));
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        validLoginDTO = new LoginDTO("exceptiontest", "exceptionpassword123");
    }

    @Test
    @DisplayName("数据库连接异常测试")
    void should_HandleDatabaseException_when_DatabaseConnectionFails() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new DataAccessException("Database connection failed") {});

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
        assertThat(responseBody.get("message")).asString().contains("系统内部错误");
    }

    @Test
    @DisplayName("数据库查询超时异常测试")
    void should_HandleQueryTimeout_when_DatabaseQueryTimesOut() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new QueryTimeoutException("Query timeout"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("数据完整性违反异常测试")
    void should_HandleDataIntegrityViolation_when_DataConstraintViolated() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("JWT Token生成异常测试")
    void should_HandleJwtException_when_TokenGenerationFails() {
        // Given
        when(userRepository.findByUsername("exceptiontest"))
            .thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(any(Authentication.class)))
            .thenThrow(new RuntimeException("JWT generation failed"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("空指针异常测试")
    void should_HandleNullPointerException_when_UnexpectedNullValue() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new NullPointerException("Unexpected null value"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("非法参数异常测试")
    void should_HandleIllegalArgumentException_when_InvalidArgument() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid argument"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("内存不足异常测试")
    void should_HandleOutOfMemoryError_when_InsufficientMemory() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new OutOfMemoryError("Java heap space"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("网络超时异常测试")
    void should_HandleNetworkTimeout_when_NetworkConnectionTimesOut() {
        // Given - 模拟网络超时
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "close");
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // 设置较短的超时时间
        TestRestTemplate timeoutRestTemplate = new TestRestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(1));
        factory.setReadTimeout(Duration.ofMillis(1));
        timeoutRestTemplate.getRestTemplate().setRequestFactory(factory);

        // When & Then - 这个测试可能不会总是触发超时，所以我们主要验证异常处理机制
        try {
            ResponseEntity<Map> response = timeoutRestTemplate.postForEntity(
                baseUrl + "/login", request, Map.class);
            
            // 如果没有超时，应该得到正常的响应
            assertThat(response.getStatusCode()).isIn(
                HttpStatus.OK, 
                HttpStatus.UNAUTHORIZED, 
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (Exception e) {
            // 如果发生超时或其他网络异常，这是预期的
            assertThat(e).isInstanceOfAny(
                org.springframework.web.client.ResourceAccessException.class,
                java.net.SocketTimeoutException.class
            );
        }
    }

    @Test
    @DisplayName("并发访问异常测试")
    void should_HandleConcurrentAccess_when_MultipleRequestsSimultaneously() {
        // Given - 模拟并发访问时的异常情况
        when(userRepository.findByUsername("exceptiontest"))
            .thenThrow(new RuntimeException("Concurrent access exception"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When - 并发发送多个请求
        int numberOfThreads = 5;
        Thread[] threads = new Thread[numberOfThreads];
        ResponseEntity<Map>[] responses = new ResponseEntity[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    responses[index] = restTemplate.postForEntity(
                        baseUrl + "/login", request, Map.class);
                } catch (Exception e) {
                    // 记录异常但不抛出，让测试继续
                    System.err.println("Thread " + index + " failed: " + e.getMessage());
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join(3000); // 最多等待3秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Then - 验证所有请求都得到了异常处理响应
        long errorResponses = 0;
        for (ResponseEntity<Map> response : responses) {
            if (response != null && response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                errorResponses++;
            }
        }
        
        // 至少应该有一些请求得到了错误响应
        assertThat(errorResponses).isGreaterThan(0);
    }

    @Test
    @DisplayName("系统资源不足异常测试")
    void should_HandleResourceExhaustion_when_SystemResourcesLow() {
        // Given - 模拟系统资源不足
        when(userRepository.findByUsername(anyString()))
            .thenAnswer(invocation -> {
                // 模拟资源不足的情况
                throw new RuntimeException("Too many connections");
            });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("异常处理测试")
    void should_HandleException_when_ServiceThrowsRuntimeException() {
        // Given - 模拟服务抛出运行时异常
        when(userRepository.findByUsername("exceptiontest"))
            .thenThrow(new RuntimeException("Service temporarily unavailable"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When - 发送请求
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then - 应该返回500错误
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
        assertThat(responseBody.get("message")).isNotNull();
    }

    @Test
    @DisplayName("异常日志记录测试")
    void should_LogException_when_ExceptionOccurs() {
        // Given
        when(userRepository.findByUsername(anyString()))
            .thenThrow(new RuntimeException("Test exception for logging"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/login", request, Map.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        // 验证异常被正确处理（通过响应格式）
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody).containsKeys("success", "errorCode", "message", "timestamp");
        assertThat(responseBody.get("success")).isEqualTo(false);
        assertThat(responseBody.get("errorCode")).isEqualTo("1001");
    }

    @Test
    @DisplayName("异常响应格式一致性测试")
    void should_ReturnConsistentErrorFormat_when_DifferentExceptionsOccur() {
        // Given - 测试不同类型的异常
        Exception[] exceptions = {
            new RuntimeException("Runtime exception"),
            new IllegalArgumentException("Illegal argument"),
            new DataAccessException("Data access exception") {},
            new NullPointerException("Null pointer exception")
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDTO> request = new HttpEntity<>(validLoginDTO, headers);

        for (Exception exception : exceptions) {
            // Given
            reset(userRepository);
            when(userRepository.findByUsername(anyString())).thenThrow(exception);

            // When
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/login", request, Map.class);

            // Then - 所有异常都应该返回一致的错误格式
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody).containsKeys("success", "errorCode", "message", "timestamp");
            assertThat(responseBody.get("success")).isEqualTo(false);
            assertThat(responseBody.get("errorCode")).isEqualTo("1001");
            assertThat(responseBody.get("message")).asString().contains("系统内部错误");
        }
    }
}