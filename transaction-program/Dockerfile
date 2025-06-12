# 多阶段构建 - 构建阶段
FROM maven:3.8-openjdk-11-slim AS builder

WORKDIR /app

# 复制pom.xml和maven wrapper
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# 复制源码并构建，使用settings.xml配置国内镜像源
COPY src ./src
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true -s .mvn/settings.xml

# 运行阶段 - 使用更轻量级的镜像
FROM openjdk:11-jdk-slim

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 创建非root用户
RUN groupadd -r mall && useradd -r -g mall mall

# 复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads && chown -R mall:mall /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 切换到非root用户
USER mall

EXPOSE 8080

# JVM调优参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 