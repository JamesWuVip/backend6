# 部署指南

## Railway 部署配置

### 1. 环境变量配置

#### 开发环境 (dev)
```bash
# 数据库配置
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=mysql://localhost:3306/wanli_dev
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# Redis配置
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=

# JWT配置
JWT_SECRET=your_jwt_secret_key_for_dev
JWT_EXPIRATION=86400000

# 应用配置
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
APP_FILE_UPLOAD_PATH=/tmp/uploads
APP_FILE_MAX_SIZE=10MB

# 邮件配置
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

#### 测试环境 (staging)
```bash
# 数据库配置
SPRING_PROFILES_ACTIVE=staging
SPRING_DATASOURCE_URL=${DATABASE_URL}
SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Redis配置
SPRING_REDIS_HOST=${REDIS_HOST}
SPRING_REDIS_PORT=${REDIS_PORT}
SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}

# JWT配置
JWT_SECRET=${JWT_SECRET_STAGING}
JWT_EXPIRATION=86400000

# 应用配置
APP_CORS_ALLOWED_ORIGINS=${CORS_ORIGINS_STAGING}
APP_FILE_UPLOAD_PATH=/app/uploads
APP_FILE_MAX_SIZE=10MB

# 邮件配置
SPRING_MAIL_HOST=${MAIL_HOST}
SPRING_MAIL_PORT=${MAIL_PORT}
SPRING_MAIL_USERNAME=${MAIL_USERNAME}
SPRING_MAIL_PASSWORD=${MAIL_PASSWORD}

# 监控配置
SENTRY_DSN=${SENTRY_DSN_STAGING}
```

#### 生产环境 (prod)
```bash
# 数据库配置
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=${DATABASE_URL}
SPRING_DATASOURCE_USERNAME=${DB_USERNAME}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Redis配置
SPRING_REDIS_HOST=${REDIS_HOST}
SPRING_REDIS_PORT=${REDIS_PORT}
SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}

# JWT配置
JWT_SECRET=${JWT_SECRET_PROD}
JWT_EXPIRATION=86400000

# 应用配置
APP_CORS_ALLOWED_ORIGINS=${CORS_ORIGINS_PROD}
APP_FILE_UPLOAD_PATH=/app/uploads
APP_FILE_MAX_SIZE=10MB

# 邮件配置
SPRING_MAIL_HOST=${MAIL_HOST}
SPRING_MAIL_PORT=${MAIL_PORT}
SPRING_MAIL_USERNAME=${MAIL_USERNAME}
SPRING_MAIL_PASSWORD=${MAIL_PASSWORD}

# 监控配置
SENTRY_DSN=${SENTRY_DSN_PROD}
```

### 2. Railway 项目设置步骤

#### 2.1 创建 Railway 项目
1. 登录 [Railway](https://railway.app)
2. 点击 "New Project"
3. 选择 "Deploy from GitHub repo"
4. 选择 `JamesWuVip/backend6` 仓库

#### 2.2 配置数据库服务
1. 在项目中添加 MySQL 数据库服务
2. 记录数据库连接信息：
   - `DATABASE_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`

#### 2.3 配置 Redis 服务
1. 在项目中添加 Redis 服务
2. 记录 Redis 连接信息：
   - `REDIS_HOST`
   - `REDIS_PORT`
   - `REDIS_PASSWORD`

#### 2.4 设置环境变量
在 Railway 项目设置中添加上述环境变量

### 3. GitHub Secrets 配置

在 GitHub 仓库的 Settings > Secrets and variables > Actions 中添加以下 secrets：

```bash
# Railway 部署
RAILWAY_TOKEN=your_railway_token

# 代码质量
CODECOV_TOKEN=your_codecov_token
SNYK_TOKEN=your_snyk_token

# 数据库配置 (开发环境)
DB_PASSWORD_DEV=your_dev_db_password
REDIS_PASSWORD_DEV=your_dev_redis_password

# 数据库配置 (测试环境)
DB_PASSWORD_STAGING=your_staging_db_password
REDIS_PASSWORD_STAGING=your_staging_redis_password

# 数据库配置 (生产环境)
DB_PASSWORD_PROD=your_prod_db_password
REDIS_PASSWORD_PROD=your_prod_redis_password

# JWT 密钥
JWT_SECRET_DEV=your_jwt_secret_for_dev
JWT_SECRET_STAGING=your_jwt_secret_for_staging
JWT_SECRET_PROD=your_jwt_secret_for_prod

# 邮件配置
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# 监控服务
SENTRY_DSN_STAGING=your_sentry_dsn_for_staging
SENTRY_DSN_PROD=your_sentry_dsn_for_prod
```

### 4. 部署流程

#### 4.1 开发环境部署
- 推送到 `dev` 分支自动触发部署
- 部署到 Railway 开发环境
- 运行测试和安全扫描

#### 4.2 测试环境部署
- 推送到 `staging` 分支自动触发部署
- 部署到 Railway 测试环境
- 运行完整测试套件
- 执行端到端测试

#### 4.3 生产环境部署
- 推送到 `main` 分支自动触发部署
- 部署到 Railway 生产环境
- 执行健康检查
- 发送部署通知

### 5. 监控和日志

#### 5.1 应用监控
- Railway 内置监控面板
- 自定义健康检查端点：`/actuator/health`
- Prometheus 指标：`/actuator/prometheus`

#### 5.2 错误追踪
- Sentry 集成用于错误监控
- 自动错误报告和告警

#### 5.3 性能监控
- UptimeRobot 用于可用性监控
- 响应时间和正常运行时间统计

### 6. 故障排除

#### 6.1 常见问题
1. **数据库连接失败**
   - 检查 `DATABASE_URL` 格式
   - 验证数据库服务状态

2. **Redis 连接失败**
   - 检查 Redis 服务配置
   - 验证网络连接

3. **部署失败**
   - 检查 Dockerfile 配置
   - 验证环境变量设置

#### 6.2 日志查看
```bash
# Railway CLI 查看日志
railway logs

# 查看特定服务日志
railway logs --service your-service-name
```

### 7. 安全注意事项

1. **环境变量安全**
   - 不要在代码中硬编码敏感信息
   - 使用 Railway 环境变量管理
   - 定期轮换密钥和令牌

2. **数据库安全**
   - 使用强密码
   - 启用 SSL 连接
   - 定期备份数据

3. **网络安全**
   - 配置适当的 CORS 策略
   - 使用 HTTPS
   - 实施速率限制

---

**注意：请根据实际环境调整配置参数，确保所有敏感信息都通过环境变量管理。**