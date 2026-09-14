# AI-Project REST API 统一契约 v1.0

> 本文档是 **backend-java（Spring Boot 3）** 与 **backend-dotnet（ASP.NET Core 8）** 双后端的唯一接口契约。
> 两个后端必须实现完全一致的路径、方法、请求/响应结构与错误码，前端按本文档对接。

## 1. 基础约定

| 项 | 约定 |
|---|---|
| Base URL | `http://localhost:8080`（Java）/ `http://localhost:5080`（.NET） |
| 前缀 | 所有接口统一 `/api/v1` |
| 内容类型 | `application/json; charset=utf-8` |
| 认证方式 | JWT Bearer Token（`Authorization: Bearer <token>`） |
| 密码算法 | BCrypt（成本因子 10） |
| JWT 有效期 | 24 小时 |

## 2. 统一响应格式

所有接口返回统一信封（HTTP 状态码与业务 code 并存）：

```json
{
  "code": 0,
  "message": "ok",
  "data": { }
}
```

### 错误码表

| code | HTTP | 含义 |
|---|---|---|
| 0 | 200 | 成功 |
| 40001 | 400 | 参数校验失败 |
| 40101 | 401 | 未认证 / Token 无效或过期 |
| 40301 | 403 | 无权限（非资源所有者） |
| 40401 | 404 | 资源不存在 |
| 40901 | 409 | 资源冲突（用户名/邮箱已存在） |
| 50000 | 500 | 服务器内部错误 |

## 3. 接口清单

### 3.1 认证

#### POST `/api/v1/auth/register` — 注册
**请求**
```json
{ "username": "alice", "email": "alice@example.com", "password": "user123" }
```
**响应** `code=0, data: { "id": 2, "username": "alice", "email": "alice@example.com", "role": "USER" }`
**错误**：40901（用户名/邮箱已存在）、40001（字段不合法：username 3-50 位、email 合法格式、password ≥ 8 位）

#### POST `/api/v1/auth/login` — 登录
**请求**
```json
{ "username": "alice", "password": "user123" }
```
**响应** `data: { "token": "<jwt>", "tokenType": "Bearer", "expiresIn": 86400, "user": { "id": 2, "username": "alice", "email": "alice@example.com", "role": "USER" } }`
**错误**：40101（用户名或密码错误）

#### GET `/api/v1/auth/me` — 当前用户信息（需认证）
**响应** `data: { "id": 2, "username": "alice", "email": "alice@example.com", "role": "USER", "createdAt": "2026-09-14T10:00:00Z" }`

### 3.2 文章

#### GET `/api/v1/articles?page=1&pageSize=10` — 文章列表（分页，含未发布需认证）
**响应**
```json
data: {
  "items": [ { "id": 1, "title": "标题", "author": "alice", "published": true, "createdAt": "..." } ],
  "total": 1, "page": 1, "pageSize": 10
}
```
（列表不返回 content 全文，仅摘要字段；未登录仅见 published=true）

#### GET `/api/v1/articles/{id}` — 文章详情
**响应** `data: { "id": 1, "title": "...", "content": "...", "author": "alice", "published": true, "createdAt": "...", "updatedAt": "..." }`
**错误**：40401（不存在）；未发布文章需作者或管理员可见，否则 40401

#### POST `/api/v1/articles` — 创建文章（需认证）
**请求** `{ "title": "标题", "content": "正文", "published": false }`
**响应** `data: { "id": 1, ... }`；**错误**：40001（title 1-200 位）、40101

#### PUT `/api/v1/articles/{id}` — 更新文章（需认证，仅作者/管理员）
**请求** `{ "title": "新标题", "content": "新正文", "published": true }`
**响应** `data: { "id": 1, ... }`；**错误**：40301（非作者）、40401

#### DELETE `/api/v1/articles/{id}` — 删除文章（需认证，仅作者/管理员）
**响应** `data: null`；**错误**：40301、40401

### 3.3 健康检查

#### GET `/api/v1/health` — 服务与依赖状态
**响应** `data: { "status": "UP", "service": "backend-java|backend-dotnet", "db": "UP", "redis": "UP", "timestamp": "..." }`

## 4. 安全要求

1. 密码必须 BCrypt 哈希存储，禁止明文/可逆加密
2. JWT 密钥通过环境变量注入（`JWT_SECRET`），禁止硬编码于源码
3. 所有写接口必须认证；越权访问返回 40301
4. 登录成功/失败、创建/删除文章等关键操作写入 `audit_logs`
5. 统一异常处理，禁止向客户端泄露堆栈信息
