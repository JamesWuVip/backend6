# 环境变量配置清单

本文档列出了万里后端项目在不同环境下所需的所有环境变量配置。

## 1. 数据库配置

### MySQL 数据库
```bash
# 数据库连接配置
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/wanli_backend?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=wanli
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver

# 连接池配置
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=600000
SPRING_DATASOURCE_HIKARI_MAX_LIFETIME=1800000
```

### JPA 配置
```bash
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
```

## 2. Redis 配置

```bash
# Redis 连接配置
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=your_redis_password
SPRING_REDIS_DATABASE=0

# Redis 连接池配置
SPRING_REDIS_JEDIS_POOL_MAX_ACTIVE=20
SPRING_REDIS_JEDIS_POOL_MAX_IDLE=10
SPRING_REDIS_JEDIS_POOL_MIN_IDLE=5
SPRING_REDIS_JEDIS_POOL_MAX_WAIT=-1ms

# Redis 超时配置
SPRING_REDIS_TIMEOUT=2000ms
SPRING_REDIS_CONNECT_TIMEOUT=2000ms
```

## 3. JWT 配置

```bash
# JWT 密钥和过期时间
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
JWT_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800
```

## 4. 邮件服务配置

```bash
# SMTP 配置
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

## 5. 应用配置

```bash
# 服务器配置
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api

# 应用配置
SPRING_APPLICATION_NAME=wanli-backend
SPRING_PROFILES_ACTIVE=dev

# 文件上传配置
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB

# CORS 配置
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
APP_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
APP_CORS_ALLOWED_HEADERS=*
APP_CORS_ALLOW_CREDENTIALS=true
```

## 6. 监控和日志配置

### Actuator 配置
```bash
# 管理端点配置
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when_authorized
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

### Sentry 配置
```bash
# Sentry 错误追踪
SENTRY_DSN=your_sentry_dsn_url
SENTRY_ENVIRONMENT=development
SENTRY_TRACES_SAMPLE_RATE=1.0
SENTRY_PROFILES_SAMPLE_RATE=1.0
SENTRY_DEBUG=false
```

### 日志配置
```bash
# 日志级别
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_WANLI=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG

# 日志文件
LOGGING_FILE_NAME=logs/wanli-backend.log
LOGGING_PATTERN_FILE=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## 7. 安全配置

```bash
# 密码加密
APP_PASSWORD_ENCODER_STRENGTH=12

# 会话配置
SPRING_SESSION_STORE_TYPE=redis
SPRING_SESSION_REDIS_NAMESPACE=wanli:session

# CSRF 配置
SPRING_SECURITY_CSRF_ENABLED=false
```

## 8. 缓存配置

```bash
# 缓存配置
SPRING_CACHE_TYPE=redis
SPRING_CACHE_REDIS_TIME_TO_LIVE=3600000
SPRING_CACHE_REDIS_KEY_PREFIX=wanli:cache:
```

## 9. 环境特定配置

### 开发环境 (dev)
```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_JPA_SHOW_SQL=true
LOGGING_LEVEL_ROOT=DEBUG
SENTRY_TRACES_SAMPLE_RATE=1.0
SENTRY_DEBUG=true
```

### 测试环境 (staging)
```bash
SPRING_PROFILES_ACTIVE=staging
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_ROOT=INFO
SENTRY_TRACES_SAMPLE_RATE=0.1
SENTRY_DEBUG=false
```

### 生产环境 (prod)
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_ROOT=WARN
SENTRY_TRACES_SAMPLE_RATE=0.05
SENTRY_DEBUG=false
```

## 10. Railway 部署配置

### Railway 环境变量
```bash
# Railway 特定配置
PORT=8080
RAILWAY_ENVIRONMENT=production

# 数据库 URL (Railway 自动提供)
DATABASE_URL=mysql://user:password@host:port/database

# Redis URL (Railway 自动提供)
REDIS_URL=redis://user:password@host:port
```

## 11. Docker 环境配置

### Docker Compose 环境变量
```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=wanli_backend
MYSQL_USER=wanli
MYSQL_PASSWORD=wanli123

# Redis 配置
REDIS_PASSWORD=redis123

# 应用配置
SPRING_PROFILES_ACTIVE=dev
```

## 12. 配置验证清单

### 必需配置检查
- [ ] 数据库连接配置
- [ ] Redis 连接配置
- [ ] JWT 密钥配置
- [ ] 邮件服务配置
- [ ] Sentry DSN 配置
- [ ] CORS 配置
- [ ] 文件上传配置

### 安全配置检查
- [ ] JWT 密钥长度至少 256 位
- [ ] 数据库密码强度
- [ ] Redis 密码配置
- [ ] 邮件服务认证
- [ ] HTTPS 配置（生产环境）

### 性能配置检查
- [ ] 数据库连接池配置
- [ ] Redis 连接池配置
- [ ] JVM 内存配置
- [ ] 缓存配置
- [ ] 日志级别配置

## 13. 配置文件模板

### .env 模板文件
```bash
# 复制此模板并重命名为 .env
# 填入实际的配置值

# 数据库配置
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/wanli_backend
SPRING_DATASOURCE_USERNAME=wanli
SPRING_DATASOURCE_PASSWORD=change_me

# Redis 配置
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=change_me

# JWT 配置
JWT_SECRET=change_me_to_a_secure_secret_key
JWT_EXPIRATION=86400

# 邮件配置
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password

# Sentry 配置
SENTRY_DSN=your_sentry_dsn_url
SENTRY_ENVIRONMENT=development
```

## 14. 故障排除

### 常见配置问题
1. **数据库连接失败**
   - 检查数据库 URL、用户名、密码
   - 确认数据库服务正在运行
   - 检查防火墙设置

2. **Redis 连接失败**
   - 检查 Redis 主机和端口
   - 确认 Redis 密码配置
   - 检查 Redis 服务状态

3. **JWT 认证失败**
   - 检查 JWT 密钥配置
   - 确认密钥长度足够
   - 检查过期时间设置

4. **邮件发送失败**
   - 检查 SMTP 配置
   - 确认邮箱密码或应用密码
   - 检查邮箱安全设置

5. **Sentry 错误追踪不工作**
   - 检查 Sentry DSN 配置
   - 确认环境配置正确
   - 检查采样率设置

---

**注意：**
- 生产环境中绝不要在代码中硬编码敏感信息
- 使用环境变量或安全的配置管理工具
- 定期更新密码和密钥
- 监控配置变更和访问日志