-- 万里后端项目数据库初始化脚本
-- 用于Docker Compose环境的MySQL数据库初始化

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS wanli_backend 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE wanli_backend;

-- 创建用户（如果不存在）
CREATE USER IF NOT EXISTS 'wanli'@'%' IDENTIFIED BY 'wanli123';

-- 授权
GRANT ALL PRIVILEGES ON wanli_backend.* TO 'wanli'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER ON wanli_backend.* TO 'wanli'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 创建基础表结构（Flyway会处理具体的迁移）
-- 这里只创建一些基础配置

-- 设置时区
SET time_zone = '+00:00';

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id VARCHAR(36) PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    INDEX idx_system_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入初始系统配置
INSERT INTO system_config (id, config_key, config_value, description, created_by) VALUES 
(UUID(), 'system.version', '1.0.0', '系统版本号', 'system'),
(UUID(), 'system.name', '万里后端系统', '系统名称', 'system'),
(UUID(), 'system.environment', 'development', '系统环境', 'system'),
(UUID(), 'jwt.expiration', '86400', 'JWT过期时间（秒）', 'system'),
(UUID(), 'password.min.length', '8', '密码最小长度', 'system'),
(UUID(), 'upload.max.size', '10485760', '文件上传最大大小（字节）', 'system')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- 创建数据库版本信息表（用于跟踪Flyway迁移）
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success BOOLEAN NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 输出初始化完成信息
SELECT 'Database initialization completed successfully!' as message;
SELECT COUNT(*) as config_count FROM system_config;
