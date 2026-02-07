# 公安专网知识库系统开发文档

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **Spring AI**: 1.1.2 | **Spring AI Alibaba**: 1.1.2.0 | **Spring Boot**: 3.5.7  
> **应用场景**: 公安专网 | 面向基层民警 | 内部知识管理

---

## 一、项目概述

### 1.1 项目背景

本项目是面向**公安专网**的内部知识管理系统，面向**基层民警**提供文档管理和智能检索服务。系统基于OceanBase SeekDB数据库，结合Spring AI Alibaba技术栈，构建企业级RAG（检索增强生成）知识库。

**核心定位**：
- 民警日常查询法律法规、规章制度
- 业务学习培训资料检索
- 执法依据推荐和参考
- 知识录入、审核、发布管理

### 1.2 核心特性

- **公安专网适配**: 符合公安专网安全要求，支持物理隔离环境部署
- **分类分级管理**: 按警种、业务、来源、类型多维度分类
- **权限精细控制**: 私有文档（仅本人）+ 共享文档（全局），支持审核发布流程
- **多格式文档支持**: 支持Word、PDF、TXT、Excel等格式
- **混合搜索能力**: 向量搜索 + 全文搜索，支持语义理解
- **双模式交互**: Web表单搜索 + 智能对话式问答
- **外部系统对接**: 支持与治安平台定时数据同步
- **完整监控体系**: 同步状态监控、查询热度统计、用户行为分析
- **安全合规**: 等保适配，完整审计日志

### 1.3 技术栈

| 层级 | 技术选型 | 版本 | 说明 |
|------|----------|------|------|
| 数据库 | OceanBase SeekDB | 1.0+ | AI原生混合搜索引擎 |
| 后端框架 | Spring Boot | 3.5.7 | 企业级应用框架 |
| AI框架 | Spring AI | 1.1.2 | AI应用开发框架 |
| AI框架扩展 | Spring AI Alibaba | 1.1.2.0 | 阿里云集成 |
| 向量处理 | DashScope Embedding | 最新 | 阿里云向量模型服务 |
| 大模型 | 通义千问 (Qwen) | 最新 | 阿里云大语言模型 |
| 文档处理 | Apache Tika | 2.9+ | 通用文档解析 |
| 任务调度 | Spring Async | 内置 | 异步任务处理 |
| 缓存 | Caffeine + Redis | 最新 | 高性能缓存 |
| 监控 | Spring Actuator + Micrometer | 内置 | 应用监控 |
| 编程语言 | Java | 21 | JDK 21+ |

### 1.4 文档分类体系

| 分类维度 | 分类示例 |
|----------|----------|
| **警种** | 治安、刑侦、交警、禁毒、社区、网安、特警 |
| **业务** | 户籍管理、治安管理、刑事侦查、网络安全、禁毒工作 |
| **来源** | 公安部、省厅、市局、区县局、派出所 |
| **类型** | 法律法规、规章制度、培训资料、总结文件、技战法 |

### 1.5 权限模型

| 文档类型 | 可见范围 | 发布规则 |
|----------|----------|----------|
| **私有文档** | 仅创建者本人 | 创建后自动私有 |
| **共享文档** | 全局可见 | 需管理员审核后发布 |
| **机制私有** | 仅创建者本人 | 机制类强制私有 |
| **机制共享** | 全局可见 | 必须经管理员审核 |

---

## 二、系统架构

### 2.1 整体架构图（公安专网适配）

```
┌─────────────────────────────────────────────────────────────────┐
│                     公安专网边界                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                      客户端层                            │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐              │   │
│  │  │  Web应用  │  │  移动终端 │  │  管理后台 │              │   │
│  │  │ (民警查询)│  │ (执法现场)│  │ (管理员)  │              │   │
│  │  └──────────┘  └──────────┘  └──────────┘              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │ HTTPS / WebSocket                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    接入层 (Gateway)                     │   │
│  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │    公安专网网关 + Spring Cloud Gateway           │  │   │
│  │  │  (认证授权、限流、日志记录、审计)                 │  │   │
│  │  └──────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                      服务层                             │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐          │   │
│  │  │ 知识库服务  │ │  文档服务  │ │  问答服务  │          │   │
│  │  │    KB      │ │  Document  │ │  Chat      │          │   │
│  │  │   Service  │ │   Service  │ │  Service   │          │   │
│  │  └────────────┘ └────────────┘ └────────────┘          │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐          │   │
│  │  │ RAG服务    │ │ 权限服务   │ │ 监控服务   │          │   │
│  │  │   RAG      │ │ Permission │ │ Monitor    │          │   │
│  │  │   Service  │ │   Service  │ │   Service  │          │   │
│  │  └────────────┘ └────────────┘ └────────────┘          │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    AI处理层                             │   │
│  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │          Spring AI Alibaba Core                  │  │   │
│  │  │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │  │   │
│  │  │  │ChatClient│  │Embedding │  │VectorStore│       │  │   │
│  │  │  │+Advisors │  │  Client  │  │          │       │  │   │
│  │  │  └──────────┘  └──────────┘  └──────────┘       │  │   │
│  │  └──────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                            │                                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    数据层 (SeekDB)                      │   │
│  │  ┌──────────────────────────────────────────────────┐  │   │
│  │  │  ┌────────────┐  ┌────────────┐  ┌────────────┐ │  │   │
│  │  │  │ 向量存储    │  │ 全文索引   │  │ 结构化数据 │ │  │   │
│  │  │  └────────────┘  └────────────┘  └────────────┘ │  │   │
│  │  └──────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层 (Client Layer)                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Web UI  │  │  Mobile  │  │   API    │  │  SDK     │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
└────────────────────────────┬────────────────────────────────────┘
                             │ REST API / WebSocket
┌────────────────────────────┴────────────────────────────────────┐
│                        接入层 (Gateway Layer)                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Spring Cloud Gateway                   │  │
│  │           (认证授权、限流、路由、日志记录、可观测性)        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────┐
│                        服务层 (Service Layer)                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ 知识库服务   │  │ 文档服务    │  │ 问答服务    │            │
│  │KnowledgeBase│  │ Document    │  │  Chat       │            │
│  │   Service   │  │   Service   │  │   Service   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ RAG服务     │  │ 用户服务    │  │ 监控服务    │            │
│  │   RAG       │  │   User      │  │ Monitor     │            │
│  │   Service   │  │   Service   │  │   Service   │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────┐
│                       AI处理层 (AI Processing Layer)            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Spring AI Alibaba Core                      │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │  │
│  │  │ChatClient│  │Embedding │  │VectorStore│  │Document  │ │  │
│  │  │          │  │  Client  │  │          │  │ Reader   │ │  │
│  │  │ Advisors │  │   MCP    │  │ ToolCall │  │ Observ.  │ │  │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────┴────────────────────────────────────┐
│                        数据层 (Data Layer)                      │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   OceanBase SeekDB                       │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────────────┐ │  │
│  │  │ 向量存储    │  │ 全文索引   │  │ 结构化数据         │ │  │
│  │  │ Vector     │  │ Full-Text  │  │ Structured Data   │ │  │
│  │  │ Storage     │  │ Search     │  │ + Chat Memory     │ │  │
│  │  └────────────┘  └────────────┘  └────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 文档处理流水线架构

```
┌──────────────────────────────────────────────────────────────────────┐
│                      文档处理流水线 (ETL Pipeline)                    │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐        │
│   │  Extract │ → │ Transform│ → │  Enrich │ → │   Load  │        │
│   └─────────┘    └─────────┘    └─────────┘    └─────────┘        │
│       │              │              │              │               │
│   ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐        │
│   │TikaReader│   │ Token    │   │Keyword   │   │ Vector  │        │
│   │Markdown  │   │ Splitter │   │Metadata  │   │ Store   │        │
│   │PDF Reader│   │ Semantic │   │Structure │   │ Database│        │
│   │Cloud     │   │ Chunking │   │Info      │   │ + MCP   │        │
│   │Reader    │   │ Batching │   │Observer  │   │ Tracing │        │
│   └─────────┘    └─────────┘    └─────────┘    └─────────┘        │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 三、环境要求

### 3.1 硬件要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核心 | 8核心+ |
| 内存 | 8GB | 16GB+ |
| 磁盘 | 50GB SSD | 200GB+ SSD |
| 网络 | 100Mbps | 1Gbps |

### 3.2 软件要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21 | 必需（Spring Boot 3.5要求） |
| Maven | 3.8+ | 项目构建工具 |
| SeekDB | 1.0+ | 向量数据库 |
| MySQL Client | 8.0+ | JDBC连接器 |

### 3.3 外部服务依赖

| 服务 | 说明 | 必需性 |
|------|------|--------|
| OceanBase SeekDB | 向量数据库 | 必需 |
| 阿里云DashScope | 嵌入模型和大模型API | 必需 |
| 飞书API | 飞书文档读取 | 可选 |
| Prometheus | 监控数据采集 | 可选 |

---

## 四、项目结构

### 4.1 目录结构

```
knowledge-base-system/
├── src/main/
│   ├── java/com/knowledgebase/
│   │   ├── KnowledgeBaseApplication.java
│   │   │
│   │   ├── config/
│   │   │   ├── SeekDBConfig.java          # SeekDB配置
│   │   │   ├── SpringAIConfig.java        # Spring AI配置
│   │   │   ├── DocumentProcessingConfig.java # 文档处理配置
│   │   │   ├── AsyncConfig.java           # 异步配置
│   │   │   ├── CacheConfig.java           # 缓存配置
│   │   │   ├── ObservabilityConfig.java   # 可观测性配置 (新增)
│   │   │   └── SecurityConfig.java        # 安全配置 (新增)
│   │   │
│   │   ├── controller/
│   │   │   ├── KnowledgeBaseController.java     # 知识库管理
│   │   │   ├── DocumentController.java          # 文档管理
│   │   │   ├── ChatController.java              # 问答接口
│   │   │   └── AdminController.java             # 管理接口
│   │   │
│   │   ├── service/
│   │   │   ├── KnowledgeBaseService.java
│   │   │   ├── DocumentService.java
│   │   │   ├── RAGService.java
│   │   │   ├── ChatService.java
│   │   │   ├── DocumentETLService.java
│   │   │   ├── DocumentProcessingService.java
│   │   │   └── ObservabilityService.java  # 新增
│   │   │
│   │   ├── processor/
│   │   │   ├── document/
│   │   │   │   ├── UniversalDocumentProcessor.java
│   │   │   │   ├── MarkdownDocumentProcessor.java
│   │   │   │   ├── PdfDocumentProcessor.java
│   │   │   │   └── CloudDocumentProcessor.java
│   │   │   │
│   │   │   ├── chunker/
│   │   │   │   ├── TokenTextSplitterProcessor.java
│   │   │   │   ├── SemanticChunkingProcessor.java
│   │   │   │   ├── MarkdownAwareChunkingProcessor.java
│   │   │   │   └── HybridChunkingStrategy.java   # 新增
│   │   │   │
│   │   │   └── enricher/
│   │   │       ├── KeywordMetadataEnricher.java
│   │   │       ├── StructureMetadataEnricher.java
│   │   │       ├── EmailMetadataProcessor.java
│   │   │       └── DocumentObserverEnricher.java  # 新增
│   │   │
│   │   ├── repository/
│   │   │   ├── KnowledgeBaseRepository.java
│   │   │   ├── DocumentRepository.java
│   │   │   └── SeekDBRepository.java
│   │   │
│   │   ├── model/
│   │   │   ├── entity/
│   │   │   │   ├── KnowledgeBase.java
│   │   │   │   ├── Document.java
│   │   │   │   └── DocumentChunk.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── CreateKnowledgeBaseRequest.java
│   │   │   │   ├── UploadDocumentRequest.java
│   │   │   │   ├── ChatRequest.java
│   │   │   │   └── ChatResponse.java
│   │   │   │
│   │   │   └── enums/
│   │   │       ├── DocumentStatus.java
│   │   │       └── ProcessingStatus.java
│   │   │
│   │   ├── mcp/                              # 新增MCP模块
│   │   │   ├── MCPServerConfig.java
│   │   │   ├── MCPToolHandler.java
│   │   │   └── KnowledgeBaseMCPTool.java
│   │   │
│   │   ├── tool/                             # 新增工具调用模块
│   │   │   ├── ToolCallingService.java
│   │   │   └── CustomToolExecutor.java
│   │   │
│   │   └── monitor/
│   │       ├── DocumentProcessingMonitor.java
│   │       ├── PerformanceMetrics.java
│   │       └── TracingConfig.java           # 新增
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       ├── application-observability.yml    # 新增
│       └── prompts/
│           ├── system-prompt.txt
│           ├── keyword-extraction-prompt.txt
│           └── rag-context-prompt.txt
│
├── src/test/
│   ├── java/com/knowledgebase/
│   │   ├── DocumentETLServiceTest.java
│   │   ├── RAGServiceTest.java
│   │   ├── MCPIntegrationTest.java          # 新增
│   │   └── IntegrationTest.java
│   │
│   └── resources/
│       ├── test-documents/
│       └── test-config.yml
│
├── scripts/
│   ├── start.sh
│   ├── stop.sh
│   ├── backup.sh
│   └── monitoring-setup.sh                  # 新增
│
├── docs/
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── DEPLOYMENT.md
│   ├── VERSION_NOTES.md                     # 新增
│   └── CHANGELOG.md                         # 新增
│
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── pom.xml
```

---

## 五、Maven依赖配置

### 5.1 完整POM配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.7</version>
        <relativePath/>
    </parent>

    <groupId>com.knowledgebase</groupId>
    <artifactId>knowledge-base-system</artifactId>
    <version>2.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Knowledge Base System</name>
    <description>基于SeekDB和Spring AI Alibaba的知识库系统</description>

    <properties>
        <java.version>21</java.version>
        <spring-ai.version>1.1.2</spring-ai.version>
        <spring-ai-alibaba.version>1.1.2.0</spring-ai-alibaba.version>
        <kotlin.version>2.0.21</kotlin.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring AI BOM -->
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring AI Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud.ai</groupId>
                <artifactId>spring-ai-alibaba-bom</artifactId>
                <version>${spring-ai-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- ==================== Spring Boot Starters ==================== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- ==================== Spring AI Starters ==================== -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-graph-core</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-observability</artifactId>
        </dependency>

        <!-- ==================== Document Processing ==================== -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-tika-document-reader</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-markdown-document-reader</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-core</artifactId>
            <version>2.9.2</version>
        </dependency>

        <!-- ==================== Vector Store ==================== -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-jdbc-vector-store-spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- ==================== MCP Support ==================== -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-spring-boot-starter-mcp</artifactId>
        </dependency>

        <!-- ==================== Observability ==================== -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-otel</artifactId>
        </dependency>

        <dependency>
            <groupId>io.opentelemetry</groupId>
            <artifactId>opentelemetry-exporter-otlp</artifactId>
        </dependency>

        <!-- ==================== Monitoring ==================== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-prometheus</artifactId>
        </dependency>

        <!-- ==================== Cache ==================== -->
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>

        <!-- ==================== Utilities ==================== -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
        </dependency>

        <!-- ==================== Test Dependencies ==================== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 六、核心模块详细设计

### 6.1 配置模块

#### 6.1.1 主配置文件

```yaml
# application.yml
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
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
    timeout: 10000ms
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
          temperature: 0.7
          max-tokens: 4096
      embedding:
        options:
          model: text-embedding-v3
          dimension: 1024

    # 可观测性配置
    observability:
      enabled: true
      tracing:
        enabled: true
        export-otlp: true
      metrics:
        enabled: true
        export-prometheus: true

    # MCP配置
    mcp:
      server:
        enabled: true
        name: knowledge-base-mcp-server
        version: 1.0.0

# 服务器配置
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,circuitbreakers
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0

# 日志配置
logging:
  level:
    root: INFO
    com.knowledgebase: DEBUG
    org.springframework.ai: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId}] %-5level %logger{36} - %msg%n"
```

#### 6.1.2 可观测性配置

```yaml
# application-observability.yml
spring:
  ai:
    observability:
      enabled: true
      
      # 链路追踪配置
      tracing:
        enabled: true
        sampling-ratio: 1.0
        export-otlp: true
        otlp:
          endpoint: ${OTLP_ENDPOINT:http://localhost:4318/v1/traces}
          protocol: http/protobuf
      
      # 指标配置
      metrics:
        enabled: true
        export-prometheus: true
        histogram:
          enabled: true
      
      # 日志配置
      logging:
        enabled: true
        include-payload: true
        include-headers: true

# 自定义指标
management:
  metrics:
    enable:
      all: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.9, 0.95, 0.99
```

#### 6.1.3 配置类实现

```java
@Configuration
@EnableConfigurationProperties
public class KnowledgeBaseConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "seekdb")
    public SeekDBProperties seekDBProperties() {
        return new SeekDBProperties();
    }
    
    @Bean
    public DataSource dataSource(SeekDBProperties properties) {
        return DataSourceBuilder.create()
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .url(properties.getJdbcUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public VectorStore vectorStore(
            EmbeddingModel embeddingModel,
            JdbcTemplate jdbcTemplate) {
        
        return SeekDBVectorStore.builder(jdbcTemplate, embeddingModel)
            .vectorType(SeekDBVectorStore.SeekDBVectorType.HNSW)
            .tableName("kb_doc_chunk")
            .initializeSchema(true)
            .build();
    }
    
    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy(
            EncodingType.CL100K_BASE,
            8000,  // 最大token数
            0.1    // 保留10%缓冲区
        );
    }
}
```

### 6.2 文档处理模块

#### 6.2.1 统一文档处理器

```java
@Component
@Scope("prototype")
public class UniversalDocumentProcessor implements DocumentProcessor {
    
    private final DocumentTransformer tokenTextSplitter;
    private final KeywordMetadataEnricher keywordEnricher;
    private final StructureMetadataEnricher structureEnricher;
    private final DocumentObserverEnricher observerEnricher;
    
    @Override
    public List<Document> process(Resource resource, Map<String, Object> metadata) {
        // 1. 使用Tika解析文档
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        
        // 2. 添加原始元数据
        documents.forEach(doc -> {
            doc.getMetadata().putAll(metadata);
            doc.getMetadata().put("processor", "universal");
            doc.getMetadata().put("process_start_time", System.currentTimeMillis());
        });
        
        // 3. 结构化元数据增强
        documents = documents.stream()
            .map(structureEnricher::enrichStructureMetadata)
            .collect(Collectors.toList());
        
        // 4. Token分块
        List<Document> chunkedDocuments = tokenTextSplitter.apply(documents);
        
        // 5. 关键词元数据增强（异步）
        List<Document> enrichedDocuments = chunkedDocuments.stream()
            .map(keywordEnricher::enrichWithKeywords)
            .collect(Collectors.toList());
        
        // 6. 可观测性数据增强
        return enrichedDocuments.stream()
            .map(observerEnricher::enrichWithObservability)
            .collect(Collectors.toList());
    }
    
    @Override
    public Set<String> supportedTypes() {
        return Set.of("*");
    }
}
```

#### 6.2.2 分块策略处理器

```java
@Configuration
public class DocumentChunkingConfig {
    
    @Bean
    @Scope("prototype")
    public DocumentTransformer tokenTextSplitter() {
        return new TokenTextSplitter(1000, 400, 10, 5000, true);
    }
    
    @Bean
    @Scope("prototype")
    public DocumentTransformer chineseTextSplitter() {
        return new ChineseTokenTextSplitter(800, 200, 5, 10000, true);
    }
    
    @Bean
    public BatchingStrategy batchingStrategy() {
        // 使用TokenCountBatchingStrategy进行优化
        return new TokenCountBatchingStrategy(
            EncodingType.CL100K_BASE,
            8000,
            0.1
        );
    }
    
    /**
     * 混合分块策略：语义分块 + Token细粒度分块
     */
    @Bean
    public HybridChunkingStrategy hybridChunkingStrategy(
            TokenTextSplitterProcessor tokenSplitter,
            SemanticChunkingProcessor semanticSplitter) {
        return new HybridChunkingStrategy(tokenSplitter, semanticSplitter);
    }
}
```

### 6.3 RAG服务模块

#### 6.3.1 RAG服务核心类

```java
@Service
public class RAGService {
    
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;
    private final RerankModel rerankModel;
    private final MeterRegistry meterRegistry;
    
    /**
     * 执行RAG检索和生成
     */
    public String queryWithRAG(String question, String knowledgeBaseId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 1. 问题向量化
            Embedding questionEmbedding = embeddingModel.embed(question);
            
            // 2. 向量检索
            List<Document> retrievedDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .topK(10)
                    .build()
            );
            
            // 3. 重排序（如果配置了RerankModel）
            if (rerankModel != null) {
                retrievedDocs = rerankDocuments(retrievedDocs, question);
            }
            
            // 4. 构建上下文
            String context = buildContext(retrievedDocs);
            
            // 5. 生成回答
            return generateAnswer(question, context);
            
        } finally {
            sample.stop(meterRegistry.timer("rag.query.duration"));
        }
    }
    
    /**
     * 流式RAG查询
     */
    public Flux<String> streamQueryWithRAG(String question, String knowledgeBaseId) {
        return chatClient.prompt()
            .user(u -> u.text(question))
            .advisors(new QuestionAnswerAdvisor(vectorStore))
            .stream()
            .content();
    }
    
    private String buildContext(List<Document> documents) {
        return documents.stream()
            .map(doc -> {
                String source = doc.getMetadata().getOrDefault("source", "unknown");
                String score = doc.getMetadata().getOrDefault("score", "0.0").toString();
                return String.format("[来源: %s, 相似度: %s]\n%s", 
                    source, score, doc.getText());
            })
            .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    private String generateAnswer(String question, String context) {
        String prompt = """
            基于以下上下文信息回答用户的问题。如果上下文中没有相关信息，请说明无法从知识库中找到答案。

            上下文信息：
            %s

            用户问题：%s

            回答：
            """.formatted(context, question);
        
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }
}
```

### 6.4 ChatClient服务模块

#### 6.4.1 ChatClient配置

```java
@Configuration
public class ChatClientConfig {
    
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            // 默认系统提示
            .defaultSystem("""
                你是一个专业的知识库助手。
                请基于提供的上下文信息回答用户的问题。
                如果上下文中没有相关信息，请明确告知用户。
                回答时，请引用信息来源。
                """)
            // 默认Advisor
            .defaultAdvisors(
                new SimpleLoggerAdvisor(),  // 调试日志
                new MessageChatMemoryAdvisor(chatMemory)  // 对话记忆
            )
            // 默认选项
            .defaultOptions(DashScopeChatOptions.builder()
                .temperature(0.7)
                .topP(0.8)
                .build())
            .build();
    }
    
    @Bean
    public ChatMemory chatMemory(JdbcTemplate jdbcTemplate) {
        // 使用JDBC存储对话记忆
        JdbcChatMemoryRepository repository = new JdbcChatMemoryRepository(jdbcTemplate);
        return MessageWindowChatMemory.builder()
            .id("knowledge-base")
            .chatMemoryRepository(repository)
            .maxMessages(20)
            .build();
    }
}
```

### 6.5 MCP集成模块（新增）

#### 6.5.1 MCP服务器配置

```java
@Configuration
@EnableMcpServer
public class MCPServerConfig {
    
    @Value("${spring.ai.mcp.server.name:knowledge-base-mcp-server}")
    private String serverName;
    
    @Bean
    public MCPServerProperties mcpServerProperties() {
        return new MCPServerProperties(serverName, "1.0.0");
    }
    
    @Bean
    public MCPServer mcpServer(
            MCPServerProperties properties,
            List<McpToolCallbackProvider> toolProviders) {
        
        return McpServer.builder(properties)
            .requestTimeout(Duration.ofSeconds(30))
            .toolCallbackProviders(toolProviders)
            .build();
    }
}
```

#### 6.5.2 知识库MCP工具

```java
@Component
public class KnowledgeBaseMCPTool {
    
    private final KnowledgeBaseService knowledgeBaseService;
    private final RAGService ragService;
    
    @Tool(description = "创建新的知识库")
    public KnowledgeBase createKnowledgeBase(
            @ToolParam(description = "知识库名称") String name,
            @ToolParam(description = "知识库描述", required = false) String description) {
        CreateKnowledgeBaseRequest request = new CreateKnowledgeBaseRequest();
        request.setName(name);
        request.setDescription(description);
        return knowledgeBaseService.createKnowledgeBase(request);
    }
    
    @Tool(description = "基于知识库进行问答")
    public String queryKnowledgeBase(
            @ToolParam(description = "知识库ID") Long knowledgeBaseId,
            @ToolParam(description = "用户问题") String question) {
        return ragService.queryWithRAG(question, knowledgeBaseId.toString());
    }
    
    @Tool(description = "上传文档到知识库")
    public Document uploadDocument(
            @ToolParam(description = "知识库ID") Long knowledgeBaseId,
            @ToolParam(description = "文档内容") String content,
            @ToolParam(description = "文档标题") String title) {
        // 处理文档上传逻辑
    }
    
    @Tool(description = "搜索知识库文档")
    public List<Document> searchDocuments(
            @ToolParam(description = "知识库ID") Long knowledgeBaseId,
            @ToolParam(description = "搜索关键词") String query) {
        return ragService.getRetrievedDocuments(query, knowledgeBaseId.toString());
    }
}
```

---

## 七、API接口设计

### 7.1 知识库管理API

#### 创建知识库

```yaml
POST /api/v1/knowledge-bases
Content-Type: application/json

Request:
{
  "name": "技术文档知识库",
  "description": "公司技术文档统一管理",
  "settings": {
    "embeddingModel": "text-embedding-v3",
    "chunkSize": 1000,
    "chunkOverlap": 200
  }
}

Response:
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "技术文档知识库",
    "description": "公司技术文档统一管理",
    "status": "READY",
    "documentCount": 0,
    "createdAt": "2025-01-20T10:30:00Z"
  }
}
```

### 7.2 文档管理API

#### 上传文档

```yaml
POST /api/v1/knowledge-bases/{kbId}/documents/upload
Content-Type: multipart/form-data

Response:
{
  "code": 200,
  "data": {
    "documentId": 100,
    "filename": "技术架构.pdf",
    "status": "PROCESSING",
    "chunks": 0,
    "processingJobId": "job-abc123"
  }
}
```

### 7.3 问答API

#### 知识库问答

```yaml
POST /api/v1/knowledge-bases/{kbId}/chat
Content-Type: application/json

Request:
{
  "message": "公司的技术架构是什么样的？",
  "sessionId": "session-abc123",
  "options": {
    "maxResults": 5,
    "searchType": "HYBRID",
    "enableRerank": true,
    "stream": false
  }
}

Response:
{
  "code": 200,
  "data": {
    "sessionId": "session-abc123",
    "messageId": "msg-xyz789",
    "response": "根据知识库内容，公司的技术架构主要包括...",
    "sources": [
      {
        "documentId": 100,
        "documentName": "技术架构.pdf",
        "chunkId": "chunk-001",
        "score": 0.85,
        "content": "相关段落内容..."
      }
    ],
    "tokens": {
      "prompt": 1500,
      "completion": 300
    },
    "traceId": "abc123def456"  // 新增：链路追踪ID
  }
}
```

#### 流式问答

```yaml
POST /api/v1/knowledge-bases/{kbId}/chat/stream
Content-Type: application/json

Request:
{
  "message": "详细介绍微服务架构"
}

Response (SSE):
data: {"chunk": "微服务架构是一", "traceId": "abc123def456"}
data: {"chunk": "种将大型应用", "traceId": "abc123def456"}
data: {"done": true, "sources": [...], "traceId": "abc123def456"}
```

---

## 八、数据库设计

### 8.1 SeekDB表结构

```sql
-- 知识库主表
CREATE TABLE kb_knowledge_base (
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
CREATE TABLE kb_document (
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
CREATE TABLE kb_doc_chunk (
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
    INDEX idx_vector (content_vector vector_cosine_ops),
    FULLTEXT INDEX idx_content (content),
    FOREIGN KEY (document_id) REFERENCES kb_document(id),
    FOREIGN KEY (knowledge_base_id) REFERENCES kb_knowledge_base(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 九、部署指南

### 9.1 Docker Compose部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  seekdb:
    image: oceanbase/seekdb:latest
    container_name: seekdb
    ports:
      - "3307:3307"
    volumes:
      - seekdb_data:/data
    environment:
      - SEEDB_DATA_DIR=/data
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

  knowledge-base:
    build: .
    image: knowledge-base-system:2.0.0
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
      - SPRING_REDIS_HOST=redis
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - OTLP_ENDPOINT=${OTLP_ENDPOINT:-http://localhost:4318/v1/traces}
    volumes:
      - uploads_data:/app/uploads
      - logs_data:/app/logs
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  seekdb_data:
  redis_data:
  uploads_data:
  logs_data:
```

---

## 十、监控与可观测性

### 10.1 核心监控指标

| 指标名称 | 类型 | 说明 |
|----------|------|------|
| `kb.document.processed.count` | Counter | 已处理文档数 |
| `kb.document.processing.duration` | Timer | 文档处理耗时 |
| `kb.vector.search.duration` | Timer | 向量检索耗时 |
| `kb.rag.query.duration` | Timer | RAG查询耗时 |
| `kb.chat.client.calls` | Counter | ChatClient调用次数 |
| `kb.mcp.tool.calls` | Counter | MCP工具调用次数 |
| `kb.cache.hit.ratio` | Gauge | 缓存命中率 |
| `kb.vector.index.size` | Gauge | 向量索引大小 |

### 10.2 链路追踪配置

```yaml
# application.yml 追加
spring:
  ai:
    observability:
      enabled: true
      tracing:
        enabled: true
        sampling-ratio: 1.0

management:
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

---

## 十一、版本更新说明

### 11.1 从1.0.0到2.0.0的主要变更

| 变更项 | 旧版本 | 新版本 | 说明 |
|--------|--------|--------|------|
| Spring AI | 1.0.0 | **1.1.2** | 新增MCP支持、可观测性增强 |
| Spring AI Alibaba | 1.0.0.2 | **1.1.2.0** | DashScope集成优化 |
| Spring Boot | 3.2.x | **3.5.7** | Kotlin兼容性修复 |
| JDK | 17 | **21** | 必需版本 |
| 新增MCP支持 | ❌ | ✅ | Model Context Protocol集成 |
| 新增可观测性 | ❌ ✅ | 完整的链路追踪和指标 |
| 新增Tool Calling | 基础 | **增强** | ToolCallAdvisor hook支持 |
| 安全修复 | 基础 | **CVE-2024-7254** | Milvus漏洞修复 |

### 11.2 升级注意事项

1. **JDK版本升级**：必须使用JDK 21
2. **Kotlin版本**：建议使用2.0.21避免兼容性问题
3. **API变更**：TTS API已迁移到共享接口
4. **配置变更**：可观测性配置已重构

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日  
**技术栈版本**: Spring AI 1.1.2 | Spring AI Alibaba 1.1.2.0 | Spring Boot 3.5.7
