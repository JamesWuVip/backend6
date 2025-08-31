# 监控配置指南

## UptimeRobot 监控设置

### 1. 创建 UptimeRobot 账户
1. 访问 [UptimeRobot](https://uptimerobot.com/)
2. 注册免费账户（支持最多50个监控器）
3. 验证邮箱并登录

### 2. 配置监控器

#### 2.1 开发环境监控
- **监控类型**: HTTP(s)
- **友好名称**: Wanli Backend Dev
- **URL**: `https://your-dev-app.railway.app/api/actuator/health`
- **监控间隔**: 5分钟
- **超时**: 30秒
- **HTTP方法**: GET
- **期望状态码**: 200
- **关键字监控**: `"status":"UP"`

#### 2.2 测试环境监控
- **监控类型**: HTTP(s)
- **友好名称**: Wanli Backend Staging
- **URL**: `https://your-staging-app.railway.app/api/actuator/health`
- **监控间隔**: 5分钟
- **超时**: 30秒
- **HTTP方法**: GET
- **期望状态码**: 200
- **关键字监控**: `"status":"UP"`

#### 2.3 生产环境监控
- **监控类型**: HTTP(s)
- **友好名称**: Wanli Backend Production
- **URL**: `https://your-prod-app.railway.app/api/actuator/health`
- **监控间隔**: 1分钟
- **超时**: 30秒
- **HTTP方法**: GET
- **期望状态码**: 200
- **关键字监控**: `"status":"UP"`

### 3. 告警配置

#### 3.1 邮件告警
- 添加接收告警的邮箱地址
- 配置告警触发条件：
  - 服务下线时立即发送
  - 服务恢复时发送通知

#### 3.2 Webhook 告警（可选）
如果需要集成到 Slack 或其他系统：
```json
{
  "webhook_url": "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK",
  "payload": {
    "text": "*monitorFriendlyName* is *alertType* (*alertDetails*)",
    "username": "UptimeRobot",
    "icon_emoji": ":exclamation:"
  }
}
```

### 4. 状态页面设置

#### 4.1 创建公共状态页面
1. 在 UptimeRobot 控制台选择 "Status Pages"
2. 创建新的状态页面
3. 添加监控器到状态页面
4. 自定义页面外观和域名

#### 4.2 状态页面配置
- **页面标题**: "万里后端系统状态"
- **描述**: "实时监控万里后端系统的可用性和性能"
- **自定义域名**: `status.wanli.com`（可选）
- **显示监控器**: 选择生产环境监控器

## Sentry 错误追踪设置

### 1. 创建 Sentry 项目
1. 访问 [Sentry](https://sentry.io/)
2. 创建新项目，选择 "Java" 平台
3. 获取 DSN 配置

### 2. 项目配置

#### 2.1 添加 Sentry 依赖
在 `pom.xml` 中已包含：
```xml
<dependency>
    <groupId>io.sentry</groupId>
    <artifactId>sentry-spring-boot-starter</artifactId>
    <version>6.28.0</version>
</dependency>
```

#### 2.2 环境配置
在各环境配置文件中设置：
```yaml
sentry:
  dsn: ${SENTRY_DSN}
  environment: ${ENVIRONMENT_NAME}
  traces-sample-rate: 0.1  # 测试环境
  # traces-sample-rate: 0.01  # 生产环境
```

### 3. 错误追踪配置

#### 3.1 自动错误捕获
Sentry 会自动捕获：
- 未处理的异常
- HTTP 错误响应
- 数据库连接错误
- 性能问题

#### 3.2 自定义错误追踪
```java
import io.sentry.Sentry;

// 手动报告错误
try {
    // 业务逻辑
} catch (Exception e) {
    Sentry.captureException(e);
    throw e;
}

// 添加上下文信息
Sentry.setUser(new User().setId(userId).setEmail(userEmail));
Sentry.setTag("feature", "user-management");
Sentry.addBreadcrumb("User attempted login");
```

### 4. 告警规则配置

#### 4.1 错误率告警
- **条件**: 错误率超过 5% 在 5 分钟内
- **通知**: 邮件 + Slack
- **环境**: 生产环境

#### 4.2 性能告警
- **条件**: 响应时间 P95 超过 2 秒
- **通知**: 邮件
- **环境**: 生产环境

#### 4.3 新错误告警
- **条件**: 出现新的错误类型
- **通知**: 立即邮件通知
- **环境**: 所有环境

## 应用性能监控 (APM)

### 1. Spring Boot Actuator 端点

已配置的监控端点：
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 应用指标
- `/actuator/prometheus` - Prometheus 格式指标

### 2. 自定义健康检查

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // 检查数据库连接
            return Health.up()
                .withDetail("database", "Available")
                .withDetail("connections", getActiveConnections())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
    }
}
```

### 3. 业务指标监控

```java
@Service
public class UserService {
    
    private final MeterRegistry meterRegistry;
    private final Counter userRegistrationCounter;
    private final Timer userLoginTimer;
    
    public UserService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userRegistrationCounter = Counter.builder("user.registrations")
            .description("Number of user registrations")
            .register(meterRegistry);
        this.userLoginTimer = Timer.builder("user.login.duration")
            .description("User login duration")
            .register(meterRegistry);
    }
    
    public void registerUser(User user) {
        // 业务逻辑
        userRegistrationCounter.increment();
    }
    
    public void loginUser(String username, String password) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 登录逻辑
        } finally {
            sample.stop(userLoginTimer);
        }
    }
}
```

## 日志监控

### 1. 结构化日志

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
  level:
    com.wanli: INFO
    org.springframework.security: WARN
```

### 2. 日志聚合（可选）

如果需要集中式日志管理，可以考虑：
- **ELK Stack** (Elasticsearch, Logstash, Kibana)
- **Grafana Loki**
- **Fluentd**

## 监控仪表板

### 1. Grafana 仪表板（可选）

如果使用 Prometheus + Grafana：

```yaml
# docker-compose.yml
version: '3.8'
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
  
  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

### 2. 关键指标监控

- **系统指标**:
  - CPU 使用率
  - 内存使用率
  - 磁盘使用率
  - 网络 I/O

- **应用指标**:
  - 请求响应时间
  - 请求成功率
  - 活跃用户数
  - 数据库连接池状态

- **业务指标**:
  - 用户注册数
  - 登录成功率
  - API 调用频率
  - 错误率趋势

## 告警策略

### 1. 告警级别

- **P0 (紧急)**: 生产环境完全不可用
- **P1 (高)**: 生产环境部分功能不可用
- **P2 (中)**: 性能下降或测试环境问题
- **P3 (低)**: 非关键功能问题或警告

### 2. 告警通知渠道

- **P0/P1**: 电话 + 短信 + 邮件 + Slack
- **P2**: 邮件 + Slack
- **P3**: 邮件

### 3. 告警抑制

配置告警抑制规则，避免告警风暴：
- 同一问题 5 分钟内只发送一次告警
- 维护窗口期间暂停告警
- 级联故障时只发送根因告警

---

**注意：请根据实际需求调整监控配置，确保监控覆盖关键业务指标和系统健康状况。**