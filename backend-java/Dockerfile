# =====================================================================
# backend-java Dockerfile（多阶段构建 + 非 root 运行）
# 构建：maven:3.9 + temurin 21   运行：temurin 21 JRE（最小化）
# =====================================================================

# ---------- 阶段 1：构建 ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 使用阿里云 Maven 镜像加速依赖下载（国内构建）
COPY docker-settings.xml /root/.m2/settings.xml

# 先复制 pom 并预下载依赖（利用 Docker 层缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -B

# ---------- 阶段 2：运行 ----------
FROM eclipse-temurin:21-jre
LABEL maintainer="ai-project" \
      description="AI-Project Java Backend"

# 非 root 运行（安全基线）
RUN useradd --system --uid 1001 --create-home appuser
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER 1001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
