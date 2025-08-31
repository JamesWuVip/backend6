package com.wanli.validation;

import com.wanli.dto.LoginDTO;
import com.wanli.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * 登录参数验证测试
 * 验证@Valid注解和Bean Validation功能
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@DisplayName("登录参数验证测试")
class LoginValidationTest {

    private Validator validator;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化Validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // 创建测试用户
        testUser = new User();
        testUser.setId("validation-test-user-id");
        testUser.setUsername("validationtest");
        testUser.setEmail("validation@test.com");
        testUser.setFullName("Validation Test User");
        testUser.setPhone("13800138003");
        testUser.setPasswordHash("$2a$10$encodedPassword");
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("用户名为空验证测试")
    void should_FailValidation_when_UsernameIsNull() {
        // Given
        LoginDTO loginDTO = new LoginDTO(null, "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        
        ConstraintViolation<LoginDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("username");
        assertThat(violation.getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("用户名为空字符串验证测试")
    void should_FailValidation_when_UsernameIsEmpty() {
        // Given
        LoginDTO loginDTO = new LoginDTO("", "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        
        boolean hasBlankViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") && 
                          v.getMessage().contains("不能为空"));
        assertThat(hasBlankViolation).isTrue();
    }

    @Test
    @DisplayName("用户名只包含空格验证测试")
    void should_FailValidation_when_UsernameIsBlank() {
        // Given
        LoginDTO loginDTO = new LoginDTO("   ", "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        
        boolean hasBlankViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") && 
                          v.getMessage().contains("不能为空"));
        assertThat(hasBlankViolation).isTrue();
    }

    @Test
    @DisplayName("密码为空验证测试")
    void should_FailValidation_when_PasswordIsNull() {
        // Given
        LoginDTO loginDTO = new LoginDTO("validationtest", null);
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(1);
        
        ConstraintViolation<LoginDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("password");
        assertThat(violation.getMessage()).contains("不能为空");
    }

    @Test
    @DisplayName("密码为空字符串验证测试")
    void should_FailValidation_when_PasswordIsEmpty() {
        // Given
        LoginDTO loginDTO = new LoginDTO("validationtest", "");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        
        boolean hasBlankViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                          v.getMessage().contains("不能为空"));
        assertThat(hasBlankViolation).isTrue();
    }

    @Test
    @DisplayName("密码只包含空格验证测试")
    void should_FailValidation_when_PasswordIsBlank() {
        // Given
        LoginDTO loginDTO = new LoginDTO("validationtest", "   ");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isNotEmpty();
        
        boolean hasBlankViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                          v.getMessage().contains("不能为空"));
        assertThat(hasBlankViolation).isTrue();
    }

    @Test
    @DisplayName("用户名和密码都为空验证测试")
    void should_FailValidation_when_BothUsernameAndPasswordAreNull() {
        // Given
        LoginDTO loginDTO = new LoginDTO(null, null);
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).hasSize(2);
        
        boolean hasUsernameViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        boolean hasPasswordViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        
        assertThat(hasUsernameViolation).isTrue();
        assertThat(hasPasswordViolation).isTrue();
    }

    @Test
    @DisplayName("有效参数验证测试")
    void should_PassValidation_when_ValidParametersProvided() {
        // Given
        LoginDTO loginDTO = new LoginDTO("validationtest", "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then
        assertThat(violations).isEmpty();
    }



    @Test
    @DisplayName("字符编码验证测试 - 中文用户名")
    void should_HandleChineseCharacters_when_ChineseUsernameProvided() {
        // Given
        LoginDTO loginDTO = new LoginDTO("中文用户名", "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then - 验证通过
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("特殊字符验证测试")
    void should_HandleSpecialCharacters_when_SpecialCharsInInput() {
        // Given
        LoginDTO loginDTO = new LoginDTO("user@domain.com", "pass!@#$%^&*()");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then - 验证应该通过
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("长度边界验证测试 - 极长用户名")
    void should_FailValidation_when_VeryLongUsernameProvided() {
        // Given - 创建一个很长的用户名（超过50个字符）
        String longUsername = "a".repeat(60);
        LoginDTO loginDTO = new LoginDTO(longUsername, "validpassword123");
        
        // When
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);
        
        // Then - 应该验证失败，因为超过了50字符的限制
        assertThat(violations).isNotEmpty();
        
        boolean hasSizeViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("username") && 
                          v.getMessage().contains("长度必须在3-50个字符之间"));
        assertThat(hasSizeViolation).isTrue();
    }
}