package com.wanli.dto;

import java.time.LocalDateTime;

public class LoginResponseDTO {

    private String token;
    private UserInfoDto user;
    private long expiresIn;
    private LocalDateTime expirationTime;

    // Constructors
    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, UserInfoDto user, long expiresIn) {
        this.token = token;
        this.user = user;
        this.expiresIn = expiresIn;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfoDto getUser() {
        return user;
    }

    public void setUser(UserInfoDto user) {
        this.user = user;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    // Inner class for user info
    public static class UserInfoDto {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Constructors
        public UserInfoDto() {}

        public UserInfoDto(String id, String username, String email, String fullName, String phone, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.fullName = fullName;
            this.phone = phone;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}