# AI-Project 自动化测试指南

三层测试体系：**单元测试 → 接口契约测试 → E2E 端到端测试**。

## 1. 后端单元 / 集成测试

| 后端 | 命令 | 说明 |
|---|---|---|
| Java (backend-java) | `cd backend-java && mvn test` | JUnit 5 + H2 内存库，覆盖 JWT 与认证全链路 |
| .NET (backend-dotnet) | `cd backend-dotnet && dotnet test` | xUnit + EF Core InMemory，覆盖同样链路 |

## 2. 双后端接口契约测试（核心）

同一套 23 项断言对 Java 与 .NET 分别执行，确保行为与 `database/API_CONTRACT.md` 一致。

**前置**：启动两个后端（无 PostgreSQL 也可，走内存库模式）：

```bash
# 终端 1：Java（H2 内存库，端口 8080）
cd backend-java
mvn spring-boot:run "-Dspring-boot.run.profiles=test"

# 终端 2：.NET（InMemory，端口 5080）
cd backend-dotnet/AiProject.Api
$env:ASPNETCORE_ENVIRONMENT = "Test"
dotnet run --no-launch-profile --urls http://localhost:5080
```

**运行**：

```bash
python tests/contract_test.py --all        # 对双后端各跑一遍
python tests/contract_test.py --base-url http://localhost:8080/api/v1   # 单后端
```

覆盖：健康检查、注册/重复注册/参数校验、登录/错误密码、me 鉴权、文章 CRUD、越权 403、删除后 404。

## 3. 前端 E2E（Playwright）

**前置**：前端 dev server + 任一后端（同上）：

```bash
# 终端 3：前端（默认代理到 Java 8080）
cd frontend
npm run dev
```

**运行**：

```bash
cd frontend
npx playwright install chromium   # 首次需下载浏览器
npm run test:e2e
```

覆盖：注册→登录→创建文章→列表→详情→删除 完整用户旅程 + 未登录守卫。

## 4. 代码规范检查（前端）

```bash
cd frontend
npm run lint          # ESLint
npm run format        # Prettier 格式化
npm run build         # 类型检查 + 生产构建
```

## 测试结果基线（2026-09-17）

| 层级 | 结果 |
|---|---|
| Java 单测/集成 | 4/4 通过 |
| .NET 单测/集成 | 3/3 通过 |
| 契约测试 Java | 23/23 通过 |
| 契约测试 .NET | 23/23 通过 |
| Playwright E2E | 2/2 通过 |
