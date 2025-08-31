package com.wanli.config;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.spring.boot.SentryProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * Sentry错误追踪配置类
 * 用于配置错误监控和性能追踪
 * 
 * @author wanli-backend
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "sentry.dsn")
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String environment;

    @Value("${sentry.traces-sample-rate:0.1}")
    private Double tracesSampleRate;

    @Value("${sentry.profiles-sample-rate:0.1}")
    private Double profilesSampleRate;

    @Value("${sentry.debug:false}")
    private Boolean debug;

    private final Environment env;

    public SentryConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        if (sentryDsn != null && !sentryDsn.isEmpty()) {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setTracesSampleRate(tracesSampleRate);
                options.setProfilesSampleRate(profilesSampleRate);
                options.setDebug(debug);
                
                // 设置发布版本
                String version = getClass().getPackage().getImplementationVersion();
                if (version != null) {
                    options.setRelease(version);
                }
                
                // 配置标签
                options.setTag("application", "wanli-backend");
                options.setTag("profile", String.join(",", env.getActiveProfiles()));
                
                // 配置上下文
                options.setServerName(getServerName());
                
                // 配置采样
                configureSampling(options);
                
                // 配置过滤器
                configureFilters(options);
            });
        }
    }

    /**
     * 配置采样策略
     */
    private void configureSampling(SentryOptions options) {
        // 根据环境调整采样率
        if ("production".equals(environment)) {
            options.setTracesSampleRate(0.05); // 生产环境降低采样率
            options.setProfilesSampleRate(0.05);
        } else if ("staging".equals(environment)) {
            options.setTracesSampleRate(0.1);
            options.setProfilesSampleRate(0.1);
        } else {
            options.setTracesSampleRate(1.0); // 开发环境全量采样
            options.setProfilesSampleRate(1.0);
        }
    }

    /**
     * 配置过滤器
     */
    private void configureFilters(SentryOptions options) {
        // 过滤健康检查请求
        options.setBeforeSend((event, hint) -> {
            if (event.getRequest() != null && event.getRequest().getUrl() != null) {
                String url = event.getRequest().getUrl();
                if (url.contains("/actuator/health") || 
                    url.contains("/health") ||
                    url.contains("/metrics")) {
                    return null; // 不发送健康检查相关的事件
                }
            }
            return event;
        });
        
        // 过滤敏感信息
        options.setBeforeSendTransaction((transaction, hint) -> {
            // 可以在这里过滤敏感的事务信息
            return transaction;
        });
    }

    /**
     * 获取服务器名称
     */
    private String getServerName() {
        String serverName = System.getenv("HOSTNAME");
        if (serverName == null || serverName.isEmpty()) {
            serverName = System.getenv("COMPUTERNAME");
        }
        if (serverName == null || serverName.isEmpty()) {
            serverName = "unknown";
        }
        return serverName;
    }

    /**
     * 自定义Sentry属性配置
     */
    @Bean
    @ConditionalOnProperty(name = "sentry.dsn")
    public SentryProperties sentryProperties() {
        SentryProperties properties = new SentryProperties();
        properties.setDsn(sentryDsn);
        properties.setEnvironment(environment);
        properties.setTracesSampleRate(tracesSampleRate);
        properties.setDebug(debug);
        return properties;
    }
}