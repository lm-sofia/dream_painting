# =====================================================================
# frontend Dockerfile（多阶段构建：Node 构建 + Nginx 托管）
# 注意：Vite 默认 base 为 /，SPA 路由由 nginx try_files 处理
# =====================================================================

# ---------- 阶段 1：构建 ----------
FROM node:20-alpine AS build
WORKDIR /app

# 先装依赖（利用层缓存）
COPY package.json package-lock.json ./
RUN npm ci

# 复制源码并构建
COPY . .
RUN npm run build

# ---------- 阶段 2：Nginx 托管 ----------
FROM nginx:1.27-alpine
LABEL maintainer="ai-project" \
      description="AI-Project Frontend (Nginx)"

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 非 root 运行（安全基线）：nginx alpine 的 worker 默认以 nginx 用户运行
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
