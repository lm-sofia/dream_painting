# AI-Project 全栈脚手架

> Java + .NET 双后端 · React 前端 · PostgreSQL + Redis · Docker · CI/CD

## 架构总览

```
┌─────────────────────────────────────────────────┐
│                   frontend/                      │
│            React 18 + Vite + TypeScript          │
│            Ant Design 5 + Zustand                │
└──────────────────────┬──────────────────────────┘
                       │ REST API（统一接口契约）
        ┌──────────────┴──────────────┐
        ▼                             ▼
┌─────────────────┐          ┌─────────────────┐
│  backend-java   │          │  backend-dotnet │
│ Spring Boot 3   │          │ ASP.NET Core 8  │
│ + JPA + Maven   │          │ + EF Core       │
└────────┬────────┘          └────────┬────────┘
         │                            │
         └────────────┬───────────────┘
                      ▼
        ┌─────────────────────────────┐
        │  PostgreSQL 16 + Redis 7    │
        │  （同一份数据库 Schema）      │
        └─────────────────────────────┘
```

## 目录结构

| 目录 | 说明 |
|---|---|
| `frontend/` | React 18 前端（Vite + TS + Ant Design） |
| `backend-java/` | Spring Boot 3 后端（Maven + JPA） |
| `backend-dotnet/` | ASP.NET Core 8 后端（EF Core） |
| `database/` | PostgreSQL 初始化脚本与 Schema 定义 |
| `tests/` | Playwright E2E 与接口契约测试 |
| `docker/` | Dockerfile 与 docker-compose 编排 |
| `.github/workflows/` | CI/CD 流水线 |

## 快速开始（待 Docker 安装后）

```bash
docker compose -f docker/docker-compose.yml up -d
```

## 技术栈

- **前端**：React 18 · Vite · TypeScript · Ant Design 5 · Zustand · React Router v6 · Axios
- **后端 A**：Java 21 · Spring Boot 3 · Spring Data JPA · Maven
- **后端 B**：.NET 8 · ASP.NET Core · EF Core
- **数据库**：PostgreSQL 16 · Redis 7
- **测试**：JUnit 5 · xUnit · Playwright · 统一契约测试
- **部署**：Docker · docker-compose · GitHub Actions

## CI/CD 流水线

| 流水线 | 触发 | 内容 |
|---|---|---|
| `ci.yml` | push / PR | Java 测试 · .NET 测试 · 前端 ESLint+Prettier+构建 |
| `contract-test.yml` | push / PR | 真实 PostgreSQL+Redis 下启动双后端，跑统一契约测试 |
| `docker-build.yml` | main / tag v* | 构建 3 个镜像并推送 GHCR |

将仓库推送到 GitHub（或 Gitee 等支持 Actions 的平台）后自动生效。

## 开发环境

- JDK 21、Maven 3.9+、.NET SDK 8、Node.js 20+（均安装于 `D:\devtools`）
- IDE：Trae-CN
