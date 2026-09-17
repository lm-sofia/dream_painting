# =====================================================================
# backend-dotnet Dockerfile（多阶段构建 + 非 root 运行）
# 构建：.NET SDK 8   运行：ASP.NET Core 8 Runtime（最小化）
# =====================================================================

# ---------- 阶段 1：发布 ----------
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# 先复制 csproj 并 restore（利用层缓存）
COPY AiProject.Api/AiProject.Api.csproj ./AiProject.Api/
RUN dotnet restore ./AiProject.Api/AiProject.Api.csproj

# 复制源码并发布
COPY AiProject.Api/ ./AiProject.Api/
RUN dotnet publish ./AiProject.Api/AiProject.Api.csproj -c Release -o /app/publish

# ---------- 阶段 2：运行 ----------
FROM mcr.microsoft.com/dotnet/aspnet:8.0
LABEL maintainer="ai-project" \
      description="AI-Project .NET Backend"

WORKDIR /app
COPY --from=build /app/publish .

# aspnet 镜像内置非 root 用户 app（UID 1654），安全基线
USER app
ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080
ENTRYPOINT ["dotnet", "AiProject.Api.dll"]
