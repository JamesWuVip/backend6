package com.wanli.controller;

import com.wanli.dto.ApiResponse;
import com.wanli.dto.LoginDTO;
import com.wanli.dto.LoginResponseDTO;
import com.wanli.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 登录控制器
 * 提供用户登录相关的REST API接口
 * 
 * @author JamesWu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 用户登录
     * 
     * @param loginDTO 登录请求数据
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResponseDTO responseDTO = loginService.login(loginDTO);
            ApiResponse<LoginResponseDTO> response = ApiResponse.success(responseDTO);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误", "AUTH_001"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("登录服务异常", "SYS_001"));
        }
    }

    /**
     * 获取登录状态
     * 
     * @return 登录状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getLoginStatus() {
        ApiResponse<String> response = ApiResponse.success("登录API服务正常运行");
        return ResponseEntity.ok(response);
    }

    /**
     * 验证用户凭据
     * 
     * @param loginDTO 登录凭据
     * @return 验证结果
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCredentials(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            boolean isValid = loginService.validateCredentials(loginDTO.getUsername(), loginDTO.getPassword());
            ApiResponse<Boolean> response = ApiResponse.success(isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("验证服务异常", "SYS_002"));
        }
    }
}