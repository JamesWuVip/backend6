# 万里后端项目

## 项目概述

万里后端项目是一个基于Spring Boot的现代化Web应用程序，提供完整的用户管理和课程管理功能。

## 技术栈

- **后端框架**: Spring Boot 3.x
- **编程语言**: Java 17
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **构建工具**: Maven
- **部署平台**: Railway
- **CI/CD**: GitHub Actions

## 分支管理

- `main`: 生产环境分支
- `staging`: 测试环境分支  
- `dev`: 开发环境分支

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 本地开发

```bash
# 克隆项目
git clone https://github.com/JamesWuVip/backend6.git
cd backend6

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

## 部署

详细部署说明请参考 [DEPLOYMENT_GUIDE.md](./doc/DEPLOYMENT_GUIDE.md)

## 贡献

请遵循 GitFlow 工作流程：
1. 从 `dev` 分支创建功能分支
2. 完成开发后提交 Pull Request 到 `dev`
3. 测试通过后合并到 `staging` 进行集成测试
4. 最终合并到 `main` 进行生产部署

## 许可证

MIT License