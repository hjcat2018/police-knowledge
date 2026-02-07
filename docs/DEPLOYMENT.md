# 公安专网知识库系统部署指南

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **技术栈版本**: Spring AI 1.1.2 | Spring AI Alibaba 1.1.2.0 | Spring Boot 3.5.7  
> **应用场景**: 公安专网 | 面向基层民警 | 内部知识管理

本文档描述了公安专网知识库系统的部署方案，包括环境准备、SeekDB安装、应用部署、配置管理和常见问题处理。

### 1.1 部署模式

系统支持以下部署模式：

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| 开发模式 | 单机部署，快速启动 | 开发测试 |
| 测试模式 | 完整服务，本地运行 | 集成测试 |
| 生产模式 | 高可用部署，分布式架构 | 生产环境（公安专网） |

### 1.2 公安专网部署架构

```
┌─────────────────────────────────────────────────────────────────┐
│                     公安专网部署架构                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   公安专网边界                           │   │
│  │  ┌───────────────┐  ┌───────────────┐                   │   │
│  │  │   防火墙      │  │   安全网关    │                   │   │
│  │  └───────────────┘  └───────────────┘                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   负载均衡层                            │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │  硬件负载均衡器 (F5/长城)                        │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   应用服务层                            │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │         知识库应用集群 (3-5节点)                 │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                   │
│  ┌─────────────────────────┴───────────────────────────────┐   │
│  │                      数据层                              │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │            SeekDB 向量数据库                    │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  │  ┌─────────────────────────────────────────────────┐   │   │
│  │  │              Redis 缓存集群                      │   │   │
│  │  └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ──────────────────────────────────────────────────────────    │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   安全管理中心                          │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────┐  │   │
│  │  │  统一身份认证  │  │   审计日志    │  │  安全监控  │  │   │
│  │  │  (专网SSO)   │  │   存储        │  │           │  │   │
│  │  └───────────────┘  └───────────────┘  └───────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 等保合规要求

| 等保要求 | 适配措施 |
|----------|----------|
| 身份鉴别 | 专网SSO集成、强密码策略、会话超时 |
| 访问控制 | RBAC权限模型、最小权限原则 |
| 安全审计 | 完整操作日志、日志不可篡改 |
| 通信安全 | 全站HTTPS、VPN隧道 |
| 数据安全 | 敏感数据加密、数据库加密 |

---

## 二、环境准备

### 2.1 硬件要求

#### 开发环境

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核心 | 8核心 |
| 内存 | 8GB | 16GB |
| 磁盘 | 50GB | 100GB SSD |
| 网络 | 100Mbps | 1Gbps |

#### 生产环境

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 8核心 | 16核心 |
| 内存 | 16GB | 32GB |
| 磁盘 | 200GB SSD | 500GB SSD |
| 网络 | 1Gbps | 10Gbps |
| 节点数 | 3 | 5+ |

### 2.2 软件要求

#### 必需软件

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 21+ | Java运行环境 |
| Maven | 3.8+ | 项目构建 |
| SeekDB | 1.0+ | 向量数据库 |
| Docker | 20.10+ | 容器化（可选） |
| Kubernetes | 1.20+ | 容器编排（可选） |

#### 可选软件

| 软件 | 版本 | 说明 |
|------|------|------|
| Redis | 6.0+ | 缓存和会话存储 |
| Elasticsearch | 7.0+ | 日志存储（可选） |
| Prometheus | 2.30+ | 监控（可选） |
| Grafana | 8.0+ | 监控面板（可选） |

### 2.3 网络要求

| 端口 | 服务 | 说明 |
|------|------|------|
| 3307 | SeekDB | 数据库服务端口 |
| 8080 | 应用服务 | HTTP服务端口 |
| 443 | HTTPS | 安全访问（生产环境必须） |
| 8443 | HTTPS | 监控管理端口 |

### 2.4 公安专网网络配置

#### 2.4.1 网络隔离要求

```yaml
# 公安专网网络配置
network:
  type: "公安专网"
  isolation: "物理隔离"
  zones:
    - name: "DMZ区"
      description: "对外服务区域"
      access: "仅限专网"
    - name: "业务区"
      description: "应用服务器区域"
      access: "仅限DMZ访问"
    - name: "数据区"
      description: "数据库区域"
      access: "仅限业务区访问"
```

#### 2.4.2 防火墙规则配置

```bash
# 公安专网防火墙规则示例
# 允许负载均衡到应用服务器
iptables -A INPUT -p tcp -s 10.0.0.0/16 --dport 8080 -j ACCEPT

# 允许应用服务器到SeekDB
iptables -A INPUT -p tcp -s 10.1.0.0/16 --dport 3307 -j ACCEPT

# 允许应用服务器到Redis
iptables -A INPUT -p tcp -s 10.1.0.0/16 --dport 6379 -j ACCEPT

# 拒绝其他所有入站请求
iptables -A INPUT -j DROP
```

#### 2.4.3 VPN配置（可选）

```yaml
# VPN配置
vpn:
  type: "IPSec VPN"
  endpoint: "vpn-gateway.moj.gov.cn"
  authentication: "双因素认证"
  encryption: "AES-256"
```

---

## 三、SeekDB安装

### 3.1 Docker部署（推荐）

#### 3.1.1 拉取镜像

```bash
# 拉取SeekDB镜像
docker pull oceanbase/seekdb:latest

# 查看镜像
docker images | grep seekdb
```

#### 3.1.2 创建数据目录

```bash
# 创建数据目录
mkdir -p /data/seekdb

# 设置权限
chmod 777 /data/seekdb
```

#### 3.1.3 启动容器

```bash
# 创建并启动SeekDB容器
docker run -d \
    --name seekdb \
    -p 3307:3307 \
    -v /data/seekdb:/data \
    -e SEEDB_DATA_DIR=/data \
    -e SEEDB_HTTP_PORT=8081 \
    oceanbase/seekdb:latest

# 验证容器状态
docker ps | grep seekdb

# 查看容器日志
docker logs -f seekdb
```

#### 3.1.4 Docker Compose部署

创建 `seekdb-compose.yml` 文件：

```yaml
version: '3.8'

services:
  seekdb:
    image: oceanbase/seekdb:latest
    container_name: seekdb
    ports:
      - "3307:3307"
      - "8081:8081"
    volumes:
      - seekdb_data:/data
    environment:
      - SEEDB_DATA_DIR=/data
      - SEEDB_CLUSTER_NAME=kb-cluster
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-ppassword"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  seekdb_data:
    driver: local
```

启动命令：

```bash
docker-compose -f seekdb-compose.yml up -d
```

### 3.2 本地安装

#### 3.2.1 使用pip安装（开发环境）

```bash
# 安装SeekDB Python客户端
pip install pyseekdb

# 安装命令行工具
pip install seekdb-cli
```

#### 3.2.2 初始化SeekDB

```bash
# 初始化数据目录
seekdb init --data-dir /data/seekdb

# 启动SeekDB服务
seekdb start --port 3307 --http-port 8081

# 验证服务状态
seekdb status
```

### 3.3 SeekDB配置

#### 3.3.1 配置文件

创建 `/data/seekdb/config.yaml`：

```yaml
# SeekDB配置
seekdb:
  data_dir: /data/seekdb
  http_port: 8081
  mysql_port: 3307
  
  # 向量配置
  vector:
    dimension: 1024
    index_type: HNSW
    metric: COSINE
    
  # 存储配置
  storage:
    max_memory: 8GB
    buffer_pool_size: 2GB
    
  # 日志配置
  logging:
    level: INFO
    path: /data/seekdb/logs
```

#### 3.3.2 初始化数据库

```bash
# 连接到SeekDB
mysql -h localhost -P 3307 -u root -p

# 创建知识库数据库
CREATE DATABASE IF NOT EXISTS knowledge_base CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建初始表结构
USE knowledge_base;

-- 知识库主表
CREATE TABLE IF NOT EXISTS kb_knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    owner_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'CREATING',
    settings JSON,
    document_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_owner (owner_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文档表
CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    knowledge_base_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT,
    mime_type VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING',
    chunk_count INT DEFAULT 0,
    metadata JSON,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    INDEX idx_kb_id (knowledge_base_id),
    INDEX idx_status (status),
    FOREIGN KEY (knowledge_base_id) REFERENCES kb_knowledge_base(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文档块向量表
CREATE TABLE IF NOT EXISTS kb_doc_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    knowledge_base_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    content_vector VECTOR(1024),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_doc_id (document_id),
    INDEX idx_kb_id (knowledge_base_id),
    FULLTEXT INDEX idx_content (content),
    FOREIGN KEY (document_id) REFERENCES kb_document(id),
    FOREIGN KEY (knowledge_base_id) REFERENCES kb_knowledge_base(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3.4 SeekDB验证

```bash
# 测试连接
mysql -h localhost -P 3307 -u root -p -e "SELECT VERSION();"

# 查看数据库列表
mysql -h localhost -P 3307 -u root -p -e "SHOW DATABASES;"

# 查看表结构
mysql -h localhost -P 3307 -u root -p knowledge_base -e "SHOW TABLES;"
```

---

## 四、应用部署

### 4.1 Maven构建

#### 4.1.1 打包命令

```bash
# 清理并打包
mvn clean package -DskipTests

# 指定配置文件打包
mvn clean package -DskipTests -Pprod

# 构建Docker镜像
mvn spring-boot:build-image -DskipTests
```

#### 4.1.2 构建产物

```
target/
├── knowledge-base-system-2.0.0.jar
├── knowledge-base-system-2.0.0.jar.original
└── knowledge-base-system-2.0.0-docker.tar.gz
```

### 4.2 Docker部署

#### 4.2.1 Dockerfile

创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM maven:3.8.6-openjdk-21-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制jar包
COPY --from=builder /app/target/knowledge-base-system-*.jar app.jar

# 创建必要的目录
RUN mkdir -p /app/logs /app/uploads /app/config

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 4.2.2 Docker Compose完整部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  # SeekDB数据库
  seekdb:
    image: oceanbase/seekdb:latest
    container_name: seekdb
    ports:
      - "3307:3307"
      - "8081:8081"
    volumes:
      - seekdb_data:/data
      - seekdb_logs:/data/logs
    environment:
      - SEEDB_DATA_DIR=/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-ppassword"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis缓存
  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # 知识库应用
  knowledge-base:
    build: .
    image: knowledge-base-system:latest
    container_name: knowledge-base
    ports:
      - "8080:8080"
    depends_on:
      seekdb:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SEEKDB_HOST=seekdb
      - SEEKDB_PORT=3307
      - SEEKDB_DATABASE=knowledge_base
      - SEEKDB_USERNAME=root
      - SEEKDB_PASSWORD=${SEEKDB_PASSWORD:-password}
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - SERVER_PORT=8080
    volumes:
      - uploads_data:/app/uploads
      - logs_data:/app/logs
      - config_data:/app/config:ro
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  seekdb_data:
    driver: local
  seekdb_logs:
    driver: local
  redis_data:
    driver: local
  uploads_data:
    driver: local
  logs_data:
    driver: local
  config_data:
    driver: local

networks:
  default:
    name: knowledge-base-network
```

#### 4.2.3 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f knowledge-base

# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

### 4.3 Kubernetes部署

#### 4.3.1 ConfigMap

创建 `k8s/configmap.yaml`：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kb-config
  namespace: knowledge-base
data:
  application.yml: |
    spring:
      application:
        name: knowledge-base-system
      profiles:
        active: prod
      datasource:
        url: jdbc:mysql://seekdb-service:3307/knowledge_base
        username: root
        password: ${DB_PASSWORD}
      redis:
        host: redis-service
        port: 6379
    seekdb:
      host: seekdb-service
      port: 3307
      database: knowledge_base
      table-prefix: kb_
    spring:
      ai:
        dashscope:
          api-key: ${AI_API_KEY}
    server:
      port: 8080
```

#### 4.3.2 Secret

创建 `k8s/secret.yaml`：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kb-secrets
  namespace: knowledge-base
type: Opaque
stringData:
  db-password: your-secure-password
  ai-api-key: your-dashscope-api-key
```

#### 4.3.3 Deployment

创建 `k8s/deployment.yaml`：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: knowledge-base-system
  namespace: knowledge-base
  labels:
    app: knowledge-base-system
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: knowledge-base-system
  template:
    metadata:
      labels:
        app: knowledge-base-system
    spec:
      serviceAccountName: knowledge-base-sa
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
      containers:
      - name: knowledge-base
        image: knowledge-base-system:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: kb-secrets
              key: db-password
        - name: AI_API_KEY
          valueFrom:
            secretKeyRef:
              name: kb-secrets
              key: ai-api-key
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
          readOnly: true
        - name: logs-volume
          mountPath: /app/logs
        - name: uploads-volume
          mountPath: /app/uploads
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
      volumes:
      - name: config-volume
        configMap:
          name: kb-config
      - name: logs-volume
        persistentVolumeClaim:
          claimName: kb-logs-pvc
      - name: uploads-volume
        persistentVolumeClaim:
          claimName: kb-uploads-pvc
```

#### 4.3.4 Service

创建 `k8s/service.yaml`：

```yaml
apiVersion: v1
kind: Service
metadata:
  name: knowledge-base-service
  namespace: knowledge-base
spec:
  selector:
    app: knowledge-base-system
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: seekdb-service
  namespace: knowledge-base
spec:
  selector:
    app: seekdb
  ports:
  - port: 3307
    targetPort: 3307
    protocol: TCP
    name: mysql
  - port: 8081
    targetPort: 8081
    protocol: TCP
    name: http
  type: ClusterIP

---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
  namespace: knowledge-base
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
    protocol: TCP
    name: redis
  type: ClusterIP
```

#### 4.3.5 Ingress

创建 `k8s/ingress.yaml`：

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: knowledge-base-ingress
  namespace: knowledge-base
  annotations:
    kubernetes.io/ingress.class: nginx
    nginx.ingress.kubernetes.io/proxy-body-size: "100m"
    nginx.ingress.kubernetes.io/proxy-connect-timeout: "30"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "60"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "60"
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - kb.example.com
    secretName: kb-tls
  rules:
  - host: kb.example.com
    http:
      paths:
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: knowledge-base-service
            port:
              number: 80
```

#### 4.3.6 HPA

创建 `k8s/hpa.yaml`：

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: knowledge-base-hpa
  namespace: knowledge-base
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: knowledge-base-system
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

#### 4.3.7 PVC

创建 `k8s/pvc.yaml`：

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: kb-uploads-pvc
  namespace: knowledge-base
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: kb-logs-pvc
  namespace: knowledge-base
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

#### 4.3.8 部署命令

```bash
# 创建命名空间
kubectl create namespace knowledge-base

# 创建密钥
kubectl apply -f k8s/secret.yaml

# 创建配置映射
kubectl apply -f k8s/configmap.yaml

# 创建存储卷
kubectl apply -f k8s/pvc.yaml

# 部署应用
kubectl apply -f k8s/deployment.yaml

# 部署服务
kubectl apply -f k8s/service.yaml

# 部署Ingress
kubectl apply -f k8s/ingress.yaml

# 部署HPA
kubectl apply -f k8s/hpa.yaml

# 查看部署状态
kubectl get pods -n knowledge-base -w

# 查看服务状态
kubectl get svc -n knowledge-base

# 查看日志
kubectl logs -f deployment/knowledge-base-system -n knowledge-base
```

### 4.4 传统部署

#### 4.4.1 上传并解压

```bash
# 上传jar包到服务器
scp target/knowledge-base-system-2.0.0.jar user@server:/app/

# 解压
mkdir -p /app/knowledge-base
cd /app/knowledge-base
jar -xf /app/knowledge-base-system-2.0.0.jar
```

#### 4.4.2 创建启动脚本

创建 `/app/knowledge-base/start.sh`：

```bash
#!/bin/bash

# 配置环境变量
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 设置内存参数
export JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC"

# 启动应用
java $JAVA_OPTS \
    -jar /app/knowledge-base-system-2.0.0.jar \
    --spring.config.location=/app/knowledge-base/config/application.yml \
    --server.port=8080 \
    >> /app/knowledge-base/logs/app.log 2>&1
```

创建 `/app/knowledge-base/stop.sh`：

```bash
#!/bin/bash

# 停止应用
PID=$(ps -ef | grep 'knowledge-base-system' | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    kill -TERM $PID
    echo "Waiting for process to stop..."
    while kill -0 $PID 2>/dev/null; do
        sleep 1
    done
    echo "Process stopped"
fi
```

#### 4.4.3 创建Systemd服务

创建 `/etc/systemd/system/knowledge-base.service`：

```ini
[Unit]
Description=Knowledge Base System
After=network.target mysql.service

[Service]
Type=simple
User=kbuser
Group=kbuser
WorkingDirectory=/app/knowledge-base
ExecStart=/bin/bash /app/knowledge-base/start.sh
ExecStop=/bin/bash /app/knowledge-base/stop.sh
Restart=always
RestartSec=10
StandardOutput=append:/app/knowledge-base/logs/system.log
StandardError=append:/app/knowledge-base/logs/error.log

[Install]
WantedBy=multi-user.target
```

#### 4.4.4 启动服务

```bash
# 重新加载systemd
sudo systemctl daemon-reload

# 启用服务
sudo systemctl enable knowledge-base

# 启动服务
sudo systemctl start knowledge-base

# 查看状态
sudo systemctl status knowledge-base

# 查看日志
sudo journalctl -u knowledge-base -f
```

---

## 五、配置管理

### 5.1 配置文件结构

```
config/
├── application.yml           # 主配置文件
├── application-dev.yml       # 开发环境配置
├── application-test.yml      # 测试环境配置
├── application-prod.yml      # 生产环境配置
├── logback-spring.xml        # 日志配置
└── prometheus/               # 监控配置
    └── metrics.yml
```

### 5.2 主配置文件

创建 `config/application.yml`：

```yaml
spring:
  application:
    name: knowledge-base-system
  config:
    import: optional:file:./config/
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # 数据源配置
  datasource:
    url: jdbc:mysql://${SEEKDB_HOST:localhost}:${SEEKDB_PORT:3307}/${SEEKDB_DATABASE:knowledge_base}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${SEEKDB_USERNAME:root}
    password: ${SEEKDB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 600000
      connection-timeout: 30000

  # Redis配置
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REedis_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
    timeout: 10000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5

  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB
      max-request-size: 100MB

# SeekDB配置
seekdb:
  host: ${SEEKDB_HOST:localhost}
  port: ${SEEKDB_PORT:3307}
  database: ${SEEKDB_DATABASE:knowledge_base}
  username: ${SEEKDB_USERNAME:root}
  password: ${SEEKDB_PASSWORD:password}
  table-prefix: kb_
  vector:
    dimension: 1024
    index-type: HNSW
    metric: COSINE

# Spring AI配置
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-max
      embedding:
        options:
          model: text-embedding-v3

# MCP配置
spring:
  ai:
    mcp:
      server:
        name: knowledge-base-server
        version: 2.0.0

# Observability配置
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  metrics:
    export:
      prometheus:
        enabled: true

# 服务器配置
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /

# 日志配置
logging:
  level:
    root: INFO
    com.knowledgebase: DEBUG
    org.springframework.ai: INFO
  file:
    name: logs/knowledge-base.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 5.3 生产环境配置

创建 `config/application-prod.yml`：

```yaml
spring:
  profiles: prod
  
  datasource:
    url: jdbc:mysql://seekdb-service:3307/knowledge_base?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useSSL=false
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
      idle-timeout: 600000
      max-lifetime: 1800000

  redis:
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10

# SeekDB向量配置优化
seekdb:
  vector:
    index-type: HNSW
    ef-construction: 200
    m: 16

# 服务器配置
server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

# 日志配置
logging:
  level:
    root: INFO
    com.knowledgebase: INFO
  file:
    name: /app/logs/knowledge-base.log
    max-size: 100MB
    max-history: 30

# Actuator安全配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
```

---

## 六、监控与运维

### 6.1 健康检查

```bash
# 检查应用健康状态
curl http://localhost:8080/actuator/health

# 检查详细信息
curl http://localhost:8080/actuator/health/details

# 检查各组件状态
curl http://localhost:8080/actuator/health
```

### 6.2 监控指标

#### 6.2.1 Prometheus指标

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'knowledge-base'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

#### 6.2.2 关键指标

| 指标名称 | 说明 | 阈值 |
|----------|------|------|
| kb_document_processing_duration | 文档处理耗时 | < 30s |
| kb_vector_search_duration | 向量检索耗时 | < 5s |
| kb_rag_query_duration | RAG查询耗时 | < 10s |
| kb_cache_hit_ratio | 缓存命中率 | > 0.8 |
| kb_vector_index_size | 向量索引大小 | < 10GB |
| kb_active_users | 活跃用户数 | - |

### 6.3 日志管理

#### 6.3.1 日志级别

```yaml
logging:
  level:
    root: INFO
    com.knowledgebase: DEBUG
    org.springframework.ai: INFO
    org.springframework.data: INFO
```

#### 6.3.2 日志轮转

使用Logback配置日志轮转：

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/knowledge-base.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/knowledge-base.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>100MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        <maxHistory>30</maxHistory>
        <totalSizeCap>10GB</totalSizeCap>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

### 6.4 备份与恢复

#### 6.4.1 数据库备份

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR=/backup/knowledge-base
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -h localhost -P 3307 -u root -p${DB_PASSWORD} \
    knowledge_base > $BACKUP_DIR/knowledge_base_$DATE.sql

# 压缩备份
gzip $BACKUP_DIR/knowledge_base_$DATE.sql

# 删除30天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: knowledge_base_$DATE.sql.gz"
```

#### 6.4.2 恢复数据库

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: restore.sh <backup_file>"
    exit 1
fi

# 解压备份
gunzip -k $BACKUP_FILE

# 恢复数据库
mysql -h localhost -P 3307 -u root -p${DB_PASSWORD} \
    knowledge_base < ${BACKUP_FILE%.gz}

echo "Database restored from: $BACKUP_FILE"
```

### 6.5 性能调优

#### 6.5.1 JVM参数调优

```bash
# 生产环境JVM参数
JAVA_OPTS="-Xms4g -Xmx8g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:+UnlockExperimentalVMOptions \
    -XX:G1NewSizePercent=20 \
    -XX:G1MaxNewSizePercent=50 \
    -XX:InitiatingHeapOccupancyPercent=45 \
    -XX:G1HeapRegionSize=16m"
```

#### 6.5.2 数据库调优

```sql
-- SeekDB参数优化
SET GLOBAL max_connections = 500;
SET GLOBAL thread_cache_size = 64;
SET GLOBAL query_cache_type = 0;
SET GLOBAL innodb_buffer_pool_size = 4G;
SET GLOBAL innodb_log_file_size = 1G;
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
```

---

## 七、常见问题

### 7.1 部署问题

#### 问题1: SeekDB连接失败

**症状**: `Connection refused` 错误

**解决方案**:
```bash
# 检查SeekDB状态
docker ps | grep seekdb

# 检查端口是否监听
netstat -tlnp | grep 3307

# 检查防火墙
sudo ufw status

# 开放端口（如果需要）
sudo ufw allow 3307/tcp
```

#### 问题2: 应用启动失败

**症状**: `OutOfMemoryError` 或其他启动错误

**解决方案**:
```bash
# 查看详细日志
tail -f /app/knowledge-base/logs/app.log

# 检查内存使用
free -m

# 调整JVM内存参数
export JAVA_OPTS="-Xms2g -Xmx4g"
```

#### 问题3: 文件上传失败

**症状**: `413 Request Entity Too Large`

**解决方案**:
```yaml
# 在application.yml中增加限制
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 100MB
      max-request-size: 100MB
```

### 7.2 性能问题

#### 问题4: 向量检索慢

**解决方案**:
1. 增加SeekDB缓存
2. 优化HNSW索引参数
3. 使用更快的向量模型

```yaml
seekdb:
  vector:
    index-type: HNSW
    ef-construction: 100
    m: 8
```

#### 问题5: 文档处理超时

**解决方案**:
1. 增加处理超时时间
2. 优化分块策略
3. 使用异步处理

### 7.3 监控问题

#### 问题6: Prometheus无法抓取指标

**解决方案**:
```bash
# 检查endpoint是否暴露
curl http://localhost:8080/actuator/prometheus

# 检查防火墙
sudo ufw allow 9090/tcp

# 检查Prometheus配置
cat prometheus.yml | grep knowledge-base
```

---

## 八、升级指南

### 8.1 版本升级步骤

```bash
# 1. 备份数据库
./backup.sh

# 2. 停止服务
kubectl delete deployment knowledge-base-system -n knowledge-base

# 3. 构建新镜像
docker build -t knowledge-base-system:2.0.0 .

# 4. 推送镜像
docker push knowledge-base-system:2.0.0

# 5. 更新部署
kubectl set image deployment/knowledge-base-system \
    knowledge-base=knowledge-base-system:2.0.0 \
    -n knowledge-base

# 6. 验证升级
kubectl rollout status deployment/knowledge-base-system -n knowledge-base

# 7. 健康检查
curl http://kb.example.com/actuator/health
```

### 8.2 回滚步骤

```bash
# 回滚到上一版本
kubectl rollout undo deployment/knowledge-base-system -n knowledge-base

# 回滚到指定版本
kubectl rollout undo deployment/knowledge-base-system \
    --to-revision=2 -n knowledge-base

# 查看回滚状态
kubectl rollout status deployment/knowledge-base-system -n knowledge-base
```

---

## 九、公安专网特殊配置

### 9.1 安全配置

#### 9.1.1 等保三级适配配置

```yaml
# 等保三级安全配置
security:
  # 密码策略
  password:
    min-length: 8
    max-length: 20
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special: true
    history-count: 5  # 不可使用最近5次密码
  
  # 会话管理
  session:
    timeout: 1800  # 30分钟超时
    max-sessions: 1  # 同一账号只允许一个会话
    concurrent-control: true
  
  # 访问控制
  access-control:
    enabled: true
    policy: "RBAC"
    mfa-required: false  # 可根据需要启用双因素认证

# 审计日志配置
audit:
  enabled: true
  storage:
    type: "DB"  # 数据库存储
    retention: 180  # 保留180天
  events:
    - LOGIN
    - LOGOUT
    - DOCUMENT_CREATE
    - DOCUMENT_DELETE
    - DOCUMENT_PUBLISH
    - QUERY
    - CONFIG_CHANGE
```

#### 9.1.2 HTTPS强制配置

```java
@Configuration
public class HttpsConfig {
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    private Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(443);
        return connector;
    }
}
```

### 9.2 统一身份认证对接

```yaml
# 专网SSO对接配置
sso:
  enabled: true
  type: "CAS"  # 或 OIDC, SAML
  server:
    url: "https://sso.moj.gov.cn"
    login-url: "https://sso.moj.gov.cn/login"
    validate-url: "https://sso.moj.gov.cn/serviceValidate"
  attributes:
    username: "username"
    police-no: "policeNo"  # 警号
    department: "department"  # 部门
    role: "role"  # 角色
```

### 9.3 公安专网监控配置

```yaml
# 公安专网监控配置
monitoring:
  # 同步状态监控
  sync:
    enabled: true
    check-interval: 60000  # 1分钟检查一次
    alert-threshold:
      failed-ratio: 0.05  # 失败率超过5%告警
      no-sync-time: 3600000  # 1小时无同步告警
  
  # 查询热度统计
  query-heat:
    enabled: true
    aggregation-interval: 3600000  # 1小时聚合一次
    retention: 30  # 保留30天
  
  # 用户行为分析
  user-behavior:
    enabled: true
    track-login: true
    track-query: true
    track-document-access: true

# 告警配置
alerting:
  enabled: true
  channels:
    - type: "sms"
      receivers: ["13800138000"]
    - type: "dingtalk"
      receivers: ["security-team"]
    - type: "email"
      receivers: ["admin@moj.gov.cn"]
```

### 9.4 治安平台对接配置

```yaml
# 治安平台数据同步配置
security-platform:
  enabled: true
  sync:
    # 定时同步配置
    cron: "0 0 2 * * ?"  # 每天凌晨2点
    # 手动同步接口
    manual-trigger: true
    # 同步参数
    batch-size: 100  # 每批处理数量
    retry:
      max-attempts: 3
      backoff-multiplier: 2
      initial-delay: 1000
  # 文档格式支持
  supported-formats:
    - "doc"
    - "docx"
    - "pdf"
    - "txt"
    - "xls"
    - "xlsx"
  # 保留原文链接
  preserve-original-url: true
```

---

## 十、运维手册

### 10.1 日常运维检查清单

```bash
#!/bin/bash
# daily-check.sh - 日常运维检查脚本

echo "=== 公安专网知识库系统日常检查 ==="
echo "检查时间: $(date)"

# 1. 检查应用健康状态
echo -e "\n[1] 应用健康状态"
curl -s http://localhost:8080/actuator/health | jq '.status'

# 2. 检查SeekDB连接
echo -e "\n[2] SeekDB连接状态"
curl -s http://localhost:8080/actuator/health/seekdb | jq '.status'

# 3. 检查同步状态
echo -e "\n[3] 治安平台同步状态"
curl -s http://localhost:8080/api/v1/admin/monitor/sync | jq '.data.syncStatus'

# 4. 检查活跃用户数
echo -e "\n[4] 今日活跃用户"
curl -s http://localhost:8080/api/v1/admin/monitor/user-behavior?period=TODAY | jq '.data.activeUsers.daily'

# 5. 检查今日查询量
echo -e "\n[5] 今日查询量"
curl -s http://localhost:8080/api/v1/admin/monitor/query-heat?period=TODAY | jq '.data.totalQueries'

# 6. 检查磁盘使用
echo -e "\n[6] 磁盘使用情况"
df -h /data

# 7. 检查内存使用
echo -e "\n[7] 内存使用情况"
free -h

echo -e "\n=== 检查完成 ==="
```

### 10.2 故障处理流程

#### 10.2.1 同步失败处理

```bash
# 1. 检查同步日志
tail -f /app/logs/sync.log

# 2. 查看失败文档
curl -s http://localhost:8080/api/v1/admin/monitor/sync | jq '.data.failedDocuments'

# 3. 手动重试同步
curl -X POST http://localhost:8080/api/v1/admin/monitor/sync/trigger

# 4. 如果持续失败，检查网络连通性
ping security-platform.moj.gov.cn
```

#### 10.2.2 应用故障处理

```bash
# 1. 查看应用日志
tail -f /app/logs/app.log

# 2. 检查JVM状态
jps -l
jstat -gc <pid>

# 3. 线程堆栈分析
jstack <pid> > thread-dump.log

# 4. 内存堆转储
jmap -heap <pid>
jmap -dump:format=b,file=heap.hprof <pid>

# 5. 重启应用
kubectl rollout restart deployment/knowledge-base-system -n knowledge-base
```

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日
