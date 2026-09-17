# AI-Project Docker 部署说明
# ===========================

## 前置要求

安装 **Docker Desktop**（Windows）并启用 WSL2 后，在本目录执行：

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

## 访问入口

| 服务 | 地址 | 说明 |
|---|---|---|
| 前端 | http://localhost:8081 | Nginx 托管 React 构建产物，/api 代理到 Java 后端 |
| Java 后端 | http://localhost:8080/api/v1 | Swagger: http://localhost:8080/swagger-ui/index.html（开发模式） |
| .NET 后端 | http://localhost:5080/api/v1 | Swagger: http://localhost:5080/swagger |
| PostgreSQL | localhost:5432 | 库 ai_project / 用户 app_user（可用 .env 覆盖） |
| Redis | localhost:6379 | 持久化开启（appendonly） |

## 演示账号（种子数据）

- 管理员：`admin / admin123`
- 普通用户：`alice / user123`

## 切换到 .NET 后端

编辑 `frontend/nginx.conf`，将 `/api` 的 `proxy_pass` 从
`http://backend-java:8080` 改为 `http://backend-dotnet:8080`，然后：

```bash
docker compose -f docker/docker-compose.yml up -d --build frontend
```

## 生产安全加固清单

1. **JWT 密钥**：必须用强随机值覆盖默认值
   ```bash
   # PowerShell 生成 32 字节随机密钥
   $secret = -join ((1..64) | ForEach-Object { '{0:x}' -f (Get-Random -Max 16) })
   ```
   然后设置环境变量 `JWT_SECRET=$secret` 再执行 compose。
2. **数据库口令**：设置 `POSTGRES_PASSWORD` 为强口令，勿用默认值。
3. 镜像已默认**非 root 用户运行**（Java: uid 1001 / .NET: app / Nginx: nginx）。
4. Nginx 已启用安全响应头（X-Frame-Options / nosniff / Referrer-Policy）。
5. 数据库与 Redis 端口仅在本地开发暴露；生产环境建议移除 `ports` 映射，
   只保留前端入口，后端与数据库走内部网络（`networks: app-net`）。

## 常用运维命令

```bash
# 查看状态
docker compose -f docker/docker-compose.yml ps

# 查看日志
docker compose -f docker/docker-compose.yml logs -f backend-java

# 停止
docker compose -f docker/docker-compose.yml down

# 停止并删除数据卷（慎用，会清空数据库）
docker compose -f docker/docker-compose.yml down -v
```

## 国内镜像加速建议

若镜像拉取慢，在 Docker Desktop Settings → Docker Engine 中配置 registry-mirrors：
`https://docker.m.daocloud.io` 等镜像加速器。
