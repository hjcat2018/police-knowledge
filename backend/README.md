# Police Knowledge Base System - Backend

公安专网知识库系统后端服务

## 项目概述

公安专网知识库系统是一个基于RAG（检索增强生成）技术的智能知识库管理平台，为基层民警提供法律法规和业务规范的智能问答、搜索服务。

### 核心特性

- **混合检索**：向量搜索 + 全文搜索，RRF融合算法
- **级联查询**：知识库支持多级嵌套，父级包含子级
- **异步处理**：文档向量化异步执行，不阻塞上传
- **流式对话**：SSE流式输出，实时显示AI回答
- **文档摘要**：智能分块 + LLM摘要，降低Token消耗

---

## 技术栈

### 核心框架

| 技术         | 版本   | 用途     |
| ------------ | ------ | -------- |
| Spring Boot  | 3.5.7  | 应用框架 |
| Java         | 17     | 开发语言 |
| MyBatis-Plus | 3.5.5  | ORM框架  |
| Sa-Token     | 1.38.0 | 认证授权 |

### AI与向量

| 技术                        | 用途                    |
| --------------------------- | ----------------------- |
| Spring AI Alibaba DashScope | 通义千问LLM + Embedding |
| OceanBase SeekDB            | 混合搜索（向量+全文）   |
| Ansj Seg                    | 中文分词                |

### 存储与中间件

| 技术              | 用途       |
| ----------------- | ---------- |
| MySQL / OceanBase | 主数据库   |
| Redis             | 缓存、会话 |
| MinIO             | 文件存储   |

### 文档处理

| 技术          | 用途           |
| ------------- | -------------- |
| Apache PDFBox | PDF解析        |
| Apache POI    | Word/Excel解析 |

---

## 项目结构

```
backend/
├── src/main/java/com/police/kb/
│   ├── PoliceKBApplication.java          # 启动类
│   │
│   ├── common/                            # 公共模块
│   │   ├── ChineseSegmenter.java          # 中文分词器
│   │   ├── ErrorCode.java                 # 错误码定义
│   │   ├── GlobalExceptionHandler.java    # 全局异常处理
│   │   ├── OriginScope.java               # 来源范围枚举
│   │   └── Result.java                    # 统一响应封装
│   │
│   ├── config/                            # 配置模块
│   │   ├── AsyncConfig.java               # 异步任务配置（线程池）
│   │   ├── BatchProcessingConfig.java     # 批处理配置
│   │   ├── DependencyChecker.java         # 依赖检查器
│   │   ├── JacksonConfig.java             # JSON配置
│   │   ├── MinioConfig.java               # MinIO配置
│   │   ├── MybatisPlusConfig.java         # MyBatis-Plus配置
│   │   ├── MyMetaObjectHandler.java       # 自动填充
│   │   ├── RedisConfig.java               # Redis配置
│   │   ├── RestTemplateConfig.java        # HTTP客户端
│   │   ├── SaTokenConfig.java             # 认证配置
│   │   ├── SeekDBConfig.java              # 向量库配置
│   │   └── WebMvcConfig.java              # Web配置
│   │
│   ├── controller/                        # 控制器层
│   │   ├── AuthController.java            # 认证接口（登录/登出/用户信息）
│   │   ├── ConversationController.java    # 对话管理（CRUD）
│   │   ├── DictController.java            # 字典管理
│   │   ├── DocumentController.java        # 文档管理（CRUD/上传/解析）
│   │   ├── DocumentSummaryController.java # 文档摘要API（方案1）
│   │   ├── KnowledgeBaseController.java   # 知识库管理
│   │   ├── SearchController.java          # 搜索接口（语义/混合搜索）
│   │   ├── StreamChatController.java      # 流式对话（SSE）
│   │   ├── SystemController.java          # 系统接口（健康检查）
│   │   ├── UserController.java            # 用户管理
│   │   └── VectorController.java          # 向量统计（调试接口）
│   │
│   ├── domain/
│   │   ├── entity/                        # 实体类
│   │   │   ├── BaseEntity.java            # 基础实体（通用字段）
│   │   │   ├── Conversation.java          # 对话
│   │   │   ├── Document.java              # 文档
│   │   │   ├── DocumentChunks.java       # 文档分块（方案1）
│   │   │   ├── DocumentVector.java        # 文档向量
│   │   │   ├── KnowledgeBase.java         # 知识库
│   │   │   ├── Message.java               # 消息
│   │   │   ├── Permission.java            # 权限
│   │   │   ├── Role.java                  # 角色
│   │   │   ├── SysDict.java               # 字典
│   │   │   ├── User.java                  # 用户
│   │   │   ├── UserRole.java              # 用户角色
│   │   │   └── RolePermission.java        # 角色权限
│   │   │
│   │   ├── dto/                           # 数据传输对象
│   │   │   ├── ChangePasswordRequest.java
│   │   │   ├── DictDTO.java
│   │   │   ├── DocumentDTO.java
│   │   │   ├── KnowledgeBaseDTO.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── UserInfoDTO.java
│   │   │
│   │   └── vo/                            # 视图对象
│   │       ├── ChatMessage.java
│   │       ├── DocumentVO.java
│   │       ├── KnowledgeBaseVO.java
│   │       ├── RagAnswer.java
│   │       └── SearchResult.java
│   │
│   ├── mapper/                            # MyBatis Mapper
│   │   ├── ConversationMapper.java
│   │   ├── DocumentMapper.java            # 文档查询（含级联）
│   │   ├── DocumentVectorMapper.java
│   │   ├── KnowledgeBaseMapper.java
│   │   ├── MessageMapper.java
│   │   ├── PermissionMapper.java
│   │   ├── RoleMapper.java
│   │   ├── RolePermissionMapper.java
│   │   ├── SysDictMapper.java
│   │   ├── UserMapper.java
│   │   └── UserRoleMapper.java
│   │
│   └── service/                           # 业务逻辑层
│       ├── AuthService.java               # 认证服务
│       ├── ConversationService.java       # 对话服务
│       ├── DictService.java               # 字典服务
│       ├── DocumentChunkingService.java   # 文档分块服务
│       ├── DocumentService.java           # 文档服务（含异步向量化）
│       ├── DocumentSummaryService.java    # 文档摘要服务（方案1）
│       ├── FileParseService.java          # 文件解析服务
│       ├── KnowledgeBaseService.java      # 知识库服务
│       ├── MinioService.java              # MinIO文件服务
│       ├── RagService.java                # RAG服务（检索增强生成）
│       ├── UserRoleService.java           # 用户角色服务
│       ├── UserService.java               # 用户服务
│       ├── VectorService.java             # 向量服务（混合搜索）
│       └── impl/                          # 服务实现
│           ├── AuthServiceImpl.java
│           ├── ConversationServiceImpl.java
│           ├── DictServiceImpl.java
│           ├── DocumentServiceImpl.java
│           ├── FileParseServiceImpl.java
│           ├── KnowledgeBaseServiceImpl.java
│           ├── RagServiceImpl.java
│           ├── UserRoleServiceImpl.java
│           ├── UserServiceImpl.java
│           └── VectorServiceImpl.java     # 核心搜索实现
│
├── src/main/resources/
│   ├── application.yml                    # 应用配置
│   └── mapper/                            # MyBatis XML
│       └── DocumentMapper.xml
│
├── sql/                                   # 数据库脚本
│   ├── 001_sys_dict_backup.sql
│   ├── 002_sys_dict_optimize.sql
│   ├── 003_sys_dict_init.sql
│   ├── 004_document_scope_fields.sql
│   ├── 005_document_vectors_fields.sql
│   ├── 006_document_kb_path.sql           # 级联查询迁移
│   └── ...
│
├── pom.xml
└── Dockerfile
```

---

## 数据库设计

### 核心表结构

#### document（文档表）

| 字段              | 类型          | 说明                      |
| ----------------- | ------------- | ------------------------- |
| id                | BIGINT        | 主键                      |
| title             | VARCHAR(500)  | 标题                      |
| content           | LONGTEXT      | 内容                      |
| summary           | VARCHAR(1000) | 摘要                      |
| kb_id             | BIGINT        | 知识库ID                  |
| kb_path           | VARCHAR(500)  | 知识库路径（级联查询）    |
| category_id       | BIGINT        | 分类ID                    |
| vector_id         | VARCHAR(100)  | 向量ID                    |
| doc_url           | VARCHAR(1000) | 源文件URL                 |
| source            | VARCHAR(50)   | 来源                      |
| origin_scope      | VARCHAR(50)   | 来源范围                  |
| origin_department | VARCHAR(200)  | 来源部门                  |
| author            | VARCHAR(100)  | 作者                      |
| publish_time      | DATETIME      | 发布时间                  |
| status            | TINYINT       | 状态(0草稿/1发布/2待审核) |
| view_count        | INT           | 浏览量                    |
| favorite_count    | INT           | 收藏量                    |
| deleted           | TINYINT       | 逻辑删除                  |

#### document_vectors（文档向量表）

| 字段        | 类型     | 说明     |
| ----------- | -------- | -------- |
| id          | BIGINT   | 主键     |
| document_id | BIGINT   | 文档ID   |
| vector      | BLOB     | 向量数据 |
| content     | LONGTEXT | 切片内容 |
| chunk_index | INT      | 切片索引 |
| deleted     | TINYINT  | 逻辑删除 |

#### conversation（对话表）

| 字段          | 类型         | 说明     |
| ------------- | ------------ | -------- |
| id            | BIGINT       | 主键     |
| user_id       | BIGINT       | 用户ID   |
| title         | VARCHAR(200) | 对话标题 |
| model         | VARCHAR(50)  | AI模型   |
| kb_id         | BIGINT       | 知识库ID |
| category_id   | BIGINT       | 分类ID   |
| message_count | INT          | 消息数   |
| deleted       | TINYINT      | 逻辑删除 |

#### message（消息表）

| 字段            | 类型         | 说明                      |
| --------------- | ------------ | ------------------------- |
| id              | BIGINT       | 主键                      |
| conversation_id | BIGINT       | 对话ID                    |
| role            | VARCHAR(20)  | 角色(user/assistant/tool) |
| content         | LONGTEXT     | 内容                      |
| token_count     | INT          | Token数量                 |
| doc_ids         | VARCHAR(500) | 引用文档ID                |
| deleted         | TINYINT      | 逻辑删除                  |

#### sys_dict（字典表）

| 字段      | 类型         | 说明                |
| --------- | ------------ | ------------------- |
| id        | BIGINT       | 主键                |
| kind      | VARCHAR(50)  | 类型(kb_category等) |
| code      | VARCHAR(100) | 编码                |
| detail    | VARCHAR(200) | 名称                |
| value     | TEXT         | 值                  |
| parent_id | BIGINT       | 父ID                |
| sort      | INT          | 排序                |

---

## 核心功能模块

### 1. 文档管理（DocumentServiceImpl）

**功能描述**：

- 文档上传、解析、存储
- 自动生成摘要
- 向量化处理（异步）
- 知识库级联查询支持

**关键方法**：

```java
// 创建文档（异步向量化）
createDocument(DocumentDTO dto)

// 异步向量化，不阻塞文档创建返回
@Async("vectorExecutor")
vectorizeDocumentAsync(Long documentId)

// 构建知识库路径（级联查询支持）
buildKbPath(Long kbId)
```

### 2. 混合检索（VectorServiceImpl）

**功能描述**：

- 向量相似度搜索（SeekDB）
- 全文搜索（MySQL LIKE）
- 并行检索（CompletableFuture）
- 加权融合（RRF算法）
- 级联知识库查询

**核心算法：并行检索 + RRF融合**

```java
// 并行执行向量搜索和全文搜索
CompletableFuture<List<SearchResult>> vectorFuture = CompletableFuture.supplyAsync(() -> {
    return vectorSimilaritySearch(...);
}, searchExecutor);

CompletableFuture<List<SearchResult>> textFuture = CompletableFuture.supplyAsync(() -> {
    return fullTextSearch(...);
}, searchExecutor);

// 归一化分数
normalizedVectorScore = vectorScore / maxVectorScore
normalizedTextScore = textScore / maxTextScore

// 加权融合（单搜索源权重补偿）
fusedScore = normalizedVectorScore * vectorWeight
           + normalizedTextScore * fulltextWeight
```

**配置参数**：
| 参数 | 默认值 | 说明 |
|------|--------|------|
| vectorWeight | 0.7 | 向量搜索权重 |
| fulltextWeight | 0.3 | 全文搜索权重 |
| hybridScoreThreshold | 0.5 | 综合分数阈值 |
| topK | 10 | 返回结果数 |

### 3. RAG对话（RagServiceImpl）

**功能描述**：

- 流式对话输出
- 上下文管理
- 参考文档引用
- 历史消息记忆

**处理流程**：

```
用户问题 → 生成向量 → 并行检索 → RRF融合 → 分数过滤
         → 构建Prompt → 调用LLM → 流式返回 → 保存对话
```

### 4. 认证授权（Sa-Token）

**功能描述**：

- JWT Token认证
- 角色权限管理
- 接口访问控制

**注解使用**：

```java
@SaCheckLogin        // 需要登录
@SaCheckRole("admin") // 需要admin角色
@SaCheckPermission("user:list") // 需要权限
```

### 5. 异步处理（AsyncConfig）

**线程池配置**：

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "vectorExecutor")
    public Executor vectorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("vector-async-");
        return executor;
    }

    @Bean(name = "searchExecutor")
    public Executor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("search-async-");
        return executor;
    }
}
```

**性能提升**：
| 场景 | 优化前 | 优化后 |
|------|--------|--------|
| 文档上传响应 | ~1000ms | ~50ms |
| 混合检索耗时 | 800ms | 500ms |
| 并发处理能力 | 串行阻塞 | 并行处理 |

---

## API接口文档

### 认证模块

| 接口                           | 方法 | 说明         |
| ------------------------------ | ---- | ------------ |
| `/api/v1/auth/login`           | POST | 登录         |
| `/api/v1/auth/logout`          | POST | 登出         |
| `/api/v1/auth/info`            | GET  | 获取用户信息 |
| `/api/v1/auth/change-password` | POST | 修改密码     |

### 知识库模块

| 接口              | 方法   | 说明       |
| ----------------- | ------ | ---------- |
| `/api/v1/kb`      | GET    | 知识库列表 |
| `/api/v1/kb/list` | GET    | 全部知识库 |
| `/api/v1/kb/{id}` | GET    | 知识库详情 |
| `/api/v1/kb`      | POST   | 创建知识库 |
| `/api/v1/kb/{id}` | PUT    | 更新知识库 |
| `/api/v1/kb/{id}` | DELETE | 删除知识库 |

### 文档模块

| 接口                                 | 方法   | 说明           |
| ------------------------------------ | ------ | -------------- |
| `/api/v1/documents`                  | GET    | 分页查询       |
| `/api/v1/documents/{id}`             | GET    | 文档详情       |
| `/api/v1/documents`                  | POST   | 创建文档       |
| `/api/v1/documents/{id}`             | PUT    | 更新文档       |
| `/api/v1/documents/{id}`             | DELETE | 删除文档       |
| `/api/v1/documents/parse-file`       | POST   | 解析文件       |
| `/api/v1/documents/create-with-file` | POST   | 创建并解析文件 |
| `/api/v1/documents/{id}/download`    | GET    | 下载文件       |

### 搜索模块

| 接口                      | 方法 | 说明     |
| ------------------------- | ---- | -------- |
| `/api/v1/search/semantic` | POST | 语义搜索 |
| `/api/v1/search/hybrid`   | POST | 混合搜索 |

### 对话模块

| 接口                                  | 方法   | 说明     |
| ------------------------------------- | ------ | -------- |
| `/api/v1/conversations`               | GET    | 对话列表 |
| `/api/v1/conversations`               | POST   | 创建对话 |
| `/api/v1/conversations/{id}`          | GET    | 对话详情 |
| `/api/v1/conversations/{id}`          | DELETE | 删除对话 |
| `/api/v1/conversations/{id}/messages` | GET    | 消息列表 |
| `/api/v1/conversations/{id}/messages` | POST   | 发送消息 |
| `/api/v1/chat/stream`                 | POST   | 流式对话 |

### 系统模块

| 接口                   | 方法   | 说明     |
| ---------------------- | ------ | -------- |
| `/api/v1/users`        | GET    | 用户列表 |
| `/api/v1/users/{id}`   | GET    | 用户详情 |
| `/api/v1/users`        | POST   | 创建用户 |
| `/api/v1/users/{id}`   | PUT    | 更新用户 |
| `/api/v1/users/{id}`   | DELETE | 删除用户 |
| `/api/v1/dict`         | GET    | 字典列表 |
| `/api/v1/vector/stats` | GET    | 向量统计 |

---

## 部署方案

### 环境要求

- JDK 17+
- MySQL 8.0+ / OceanBase
- Redis 6.0+
- MinIO
- 内存 4GB+

### 配置文件修改（application.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_MYSQL_HOST:3306/police_kb
    username: YOUR_USERNAME
    password: YOUR_PASSWORD

  data:
    redis:
      host: YOUR_REDIS_HOST
      port: 6379

ai:
  dashscope:
    api-key: YOUR_DASHSCOPE_API_KEY

minio:
  endpoint: http://YOUR_MINIO:9000
  access-key: YOUR_ACCESS_KEY
  secret-key: YOUR_SECRET_KEY

seekdb:
  host: YOUR_OCEANBASE_HOST
  port: 2881
  username: YOUR_OB_USER
  password: YOUR_OB_PASSWORD
```

### 方式一：JAR部署

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar police-kb-1.0.0.jar
```

### 方式二：Docker部署

```dockerfile
# Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

```bash
# 构建运行
docker build -t police-kb:latest .
docker run -d -p 8080:8080 police-kb:latest
```

### 方式三：Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: police-kb
spec:
  replicas: 2
  selector:
    matchLabels:
      app: police-kb
  template:
    metadata:
      labels:
        app: police-kb
    spec:
      containers:
        - name: police-kb
          image: police-kb:latest
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: prod
---
apiVersion: v1
kind: Service
metadata:
  name: police-kb
spec:
  selector:
    app: police-kb
  ports:
    - port: 8080
      targetPort: 8080
```

---

## 数据库初始化

### 执行顺序

```bash
# 1. 创建数据库
CREATE DATABASE police_kb DEFAULT CHARACTER SET utf8mb4;

# 2. 执行建表脚本（参考sql/schema.sql）

# 3. 执行数据迁移脚本
sql/001_sys_dict_backup.sql
sql/002_sys_dict_optimize.sql
sql/003_sys_dict_init.sql
sql/004_document_scope_fields.sql
sql/005_document_vectors_fields.sql
sql/006_document_kb_path.sql  # 级联查询迁移
```

### 级联查询数据迁移

```sql
-- 二级知识库文档
UPDATE document d
INNER JOIN sys_dict child ON d.kb_id = child.id
INNER JOIN sys_dict parent ON child.parent_id = parent.id
SET d.kb_path = CONCAT(parent.id, ',', child.id)
WHERE d.deleted = 0 AND child.parent_id > 0;

-- 一级知识库文档
UPDATE document d
INNER JOIN sys_dict dict ON d.kb_id = dict.id
SET d.kb_path = CONCAT(dict.id)
WHERE d.deleted = 0 AND (dict.parent_id = 0 OR dict.parent_id IS NULL);
```

---

## 下步优化与完善

### 1. 检索优化

#### Reranker重排序

```java
// 引入交叉编码器对初筛结果重排序
// 提升检索精度
// 推荐：Jina Reranker、阿里云DashScope Rerank
```

#### 查询扩展

```java
// 使用LLM生成查询同义词/改写
// 解决词汇鸿沟问题
// 示例："打架" → "打架、斗殴、互殴"
```

#### 动态权重调节

```java
// 根据查询类型自动调整权重
// 专业问题 → 向量权重↑
// 关键词查询 → 全文权重↑
```

### 2. 性能优化

#### 向量索引优化

```yaml
seekdb:
  ef-search: 200 # 提升搜索精度
  m: 24 # 提升索引质量
```

#### 缓存策略

```java
// 热门文档向量缓存
// 搜索结果缓存
// 对话上下文缓存
```

### 3. 功能完善

#### 多模态检索

```java
// 图片检索（CLIP模型）
// 表格检索
// 公式识别
```

#### 知识图谱

```java
// 实体抽取
// 关系构建
// 知识推理
```

#### 个性化推荐

```java
// 用户兴趣建模
// 查询历史利用
// 相似文档推荐
```

### 4. 运维监控

#### 健康检查

```java
// 数据库连接池监控
// 向量库状态监控
// API响应时间监控
```

#### 日志优化

```yaml
// 结构化日志（JSON格式）
// 链路追踪（SkyWalking/Zipkin）
// 慢查询日志
```

### 5. 安全增强

#### 数据安全

```java
// 敏感信息加密存储
// 访问日志审计
// 数据脱敏处理
```

#### 接口安全

```java
// 限流熔断（Sentinel）
// XSS/CSRF防护
// 接口签名验证
```

---

## 代码规范

### 注释规范

所有类和方法必须添加JavaDoc注释：

```java
/**
 * 类描述
 * <p>
 * 详细说明...
 * </p>
 *
 * @author 作者名
 * @date 日期
 */
@Service
public class ExampleService {

    /**
     * 方法描述
     *
     * @param param1 参数1说明
     * @param param2 参数2说明
     * @return 返回值说明
     */
    public ReturnType methodName(Type1 param1, Type2 param2) {
        // 方法实现
    }
}
```

### 实体类字段注释

```java
/**
 * 字段描述
 */
private String fieldName;
```

---

## 常见问题

### Q1: 向量搜索无结果

**解决**：检查文档是否已向量化，确认SeekDB连接配置

### Q2: 混合检索分数异常

**解决**：检查向量分数归一化逻辑，参考`VectorServiceImpl.java`

### Q3: 级联查询不生效

**解决**：执行`sql/006_document_kb_path.sql`迁移脚本

### Q4: 流式对话中断

**解决**：检查网络超时配置，增加`read-timeout`

### Q5: 异步任务未执行

**解决**：确认`@EnableAsync`注解已添加，检查线程池配置

---

## 参考资料

- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Spring AI官方文档](https://spring.io/projects/spring-ai)
- [Spring AI Alibaba文档](https://ai.alibaba.com/docs/)
- [Spring AI Alibaba Admin项目示例](https://github.com/spring-ai-alibaba/spring-ai-alibaba-admin)
- [Spring AI Alibaba 功能示例](https://github.com/alibaba/spring-ai-alibaba.git)
- [OceanBase SeekDB文档](https://www.oceanbase.ai/docs/)
- [MyBatis-Plus文档](https://baomidou.com/)
- [Sa-Token文档](https://sa-token.dev33.cn/)
- [Ansj中文分词](https://github.com/NLPchina/ansj_seg)
- ***

## License

MIT License

---

## 方案1：文档分块摘要（已实现）

### 功能概述

大文档 → 智能分块（每块~2000字符） → 并发LLM摘要 → 拼接摘要 → 最终摘要

### 核心特性

- **智能分块**：保持法律条文完整性，支持overlap
- **并发摘要**：每块并发调用LLM生成100-200字摘要
- **异步处理**：`@Async` + `CompletableFuture`，大文件不阻塞
- **进度反馈**：WebSocket实时推送生成进度
- **缓存优化**：Redis缓存摘要，避免重复生成

### 新增文件

| 文件路径 | 说明 |
| -------- | ---- |
| `controller/DocumentSummaryController.java` | 摘要API控制器 |
| `service/DocumentSummaryService.java` | 摘要服务接口 |
| `service/impl/DocumentSummaryServiceImpl.java` | 摘要服务实现 |
| `domain/entity/DocumentChunks.java` | 文档分块实体 |
| `websocket/SummaryProgressWebSocket.java` | WebSocket进度推送 |
| `config/WebSocketConfig.java` | WebSocket配置 |

### API端点

| 接口 | 方法 | 说明 |
| ---- | ---- | ---- |
| `/api/v1/summary/sync` | POST | 同步生成摘要 |
| `/api/v1/summary/async/{docId}` | POST | 异步生成摘要 |
| `/api/v1/summary/status/{docId}` | GET | 获取摘要状态 |
| `/api/v1/summary/regenerate/{docId}` | POST | 重新生成摘要 |
| `/api/v1/summary/batch` | POST | 批量生成摘要 |
| `/ws/summary/progress/{docId}` | WebSocket | 进度推送 |

### Redis缓存

| Key | 类型 | 说明 |
| --- | ---- | ---- |
| `police:kb:summary:{docId}` | String | 摘要内容缓存 |
| `police:kb:summary:status:{docId}` | Hash | 生成状态 |

### 数据库变更

```sql
ALTER TABLE document MODIFY COLUMN summary LONGTEXT;
ALTER TABLE document ADD COLUMN summary_status VARCHAR(20);
ALTER TABLE document ADD COLUMN summary_chunks INT;
ALTER TABLE document ADD COLUMN summary_length INT;

CREATE TABLE document_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    created_time DATETIME,
    INDEX idx_document_id (document_id)
);
```


---

## 2026-02-05 代码清理与表名规范

### 代码清理

#### 删除无效文件
| 文件 | 说明 |
| ---- | ---- |
| `domain/entity/Category.java` | Category表已删除，分类功能使用sys_dict实现 |
| `mapper/CategoryMapper.java` | 从未使用，已删除 |

#### 修复编译错误
| 文件 | 修复内容 |
| ---- | ---- |
| `AuthController.java` | 移除多余的Result.success(String, null)参数 |
| `VectorServiceImpl.java` | ChineseSegmenter改为静态方法调用 |

### 表名规范

#### 重命名表（添加sys_前缀）
| 原表名 | 新表名 |
|--------|--------|
| user | **sys_user** |
| role | **sys_role** |
| permission | **sys_permission** |
| user_role | **sys_user_role** |
| role_permission | **sys_role_permission** |

#### 代码修改清单

**实体类（5个）**：
| 文件 | 修改 |
| ---- | ---- |
| `User.java` | `@TableName("user")` → `@TableName("sys_user")` |
| `Role.java` | `@TableName("role")` → `@TableName("sys_role")` |
| `Permission.java` | `@TableName("permission")` → `@TableName("sys_permission")` |
| `UserRole.java` | `@TableName("user_role")` → `@TableName("sys_user_role")` |
| `RolePermission.java` | `@TableName("role_permission")` → `@TableName("sys_role_permission")` |

**Mapper接口（5个，12处SQL）**：
| Mapper | 修改内容 |
| ------ | ---- |
| `UserMapper.java` | 4处 `FROM user` → `FROM sys_user` |
| `RoleMapper.java` | `FROM role` → `FROM sys_role`, `user_role` → `sys_user_role` |
| `PermissionMapper.java` | `FROM permission` → `FROM sys_permission`, `role_permission` → `sys_role_permission` |
| `UserRoleMapper.java` | 2处 `FROM user_role` → `FROM sys_user_role` |
| `RolePermissionMapper.java` | 1处 `FROM role_permission` → `FROM sys_role_permission` |

**SQL脚本（2个）**：
- `sql/schema.sql`
- `sql/police_kb.sql`

### 代码注释规范

为所有类和方法添加了完整的JavaDoc注释：
- 类注释：功能描述、@author、@version、@since
- 方法注释：@param、@return、@throws
- 常量注释：说明用途

---

## 2026-02-06 MCP服务管理与提示模板功能

### 一、MCP服务管理（McpServiceController）

#### 1.1 功能概述

MCP（Model Context Protocol）服务管理模块提供MCP服务的CRUD功能，支持通过HTTP或进程启动模式集成外部MCP服务。

#### 1.2 新增文件

| 文件路径 | 说明 |
| -------- | ---- |
| `domain/entity/McpService.java` | MCP服务实体类 |
| `mapper/McpServiceMapper.java` | MCP服务Mapper接口 |
| `service/McpServiceService.java` | MCP服务业务接口 |
| `service/impl/McpServiceServiceImpl.java` | MCP服务业务实现 |
| `controller/McpServiceController.java` | MCP服务REST控制器 |

#### 1.3 数据库表结构

```sql
CREATE TABLE `mcp_service` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `code` varchar(50) NOT NULL COMMENT '服务编码（唯一标识）',
  `description` varchar(500) DEFAULT NULL COMMENT '服务描述',
  `enabled` tinyint(4) DEFAULT 1 COMMENT '启用状态：0-禁用，1-启用',
  `config_url` varchar(500) DEFAULT NULL COMMENT 'API URL（HTTP模式）',
  `config_auth_type` varchar(20) DEFAULT 'api_key' COMMENT '认证方式：api_key/bearer/oauth2',
  `config_credentials` varchar(500) DEFAULT NULL COMMENT '认证凭证',
  `config_timeout` int(11) DEFAULT 60 COMMENT '超时时间（秒）',
  `config_method` varchar(10) DEFAULT 'POST' COMMENT '请求方法：GET/POST',
  `command` varchar(500) DEFAULT NULL COMMENT '启动命令（进程模式）',
  `args` text DEFAULT NULL COMMENT '命令参数（进程模式）',
  `env` text DEFAULT NULL COMMENT '环境变量（进程模式）',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP服务表';
```

#### 1.4 API接口

| 接口 | 方法 | 说明 |
| ---- | ---- | ---- |
| `/api/v1/mcp/services` | GET | 获取所有启用的MCP服务列表 |
| `/api/v1/mcp/services/{id}` | GET | 获取MCP服务详情 |
| `/api/v1/mcp/services` | POST | 新增MCP服务 |
| `/api/v1/mcp/services/{id}` | PUT | 更新MCP服务 |
| `/api/v1/mcp/services/{id}` | DELETE | 删除MCP服务 |
| `/api/v1/mcp/services/{id}/toggle` | PUT | 切换MCP服务状态 |

#### 1.5 请求示例

```json
// 新增MCP服务请求
POST /api/v1/mcp/services
Content-Type: application/json

{
  "name": "文件系统服务",
  "code": "filesystem",
  "description": "提供文件读写能力的MCP服务",
  "enabled": 1,
  "configUrl": "http://localhost:9000/mcp/filesystem",
  "configAuthType": "bearer",
  "configCredentials": "your-token-here",
  "configTimeout": 60,
  "configMethod": "POST",
  "sort": 1
}

// 进程启动模式
{
  "name": "自定义MCP服务",
  "code": "custom-mcp",
  "description": "自定义MCP服务",
  "command": "/usr/bin/python3",
  "args": "/path/to/server.py",
  "env": "API_KEY=xxx\nDEBUG=true",
  "sort": 2
}
```

#### 1.6 服务配置说明

| 配置项 | 类型 | 说明 |
| ------ | ---- | ---- |
| name | string | 服务名称 |
| code | string | 服务编码（唯一标识） |
| enabled | int | 启用状态：0-禁用，1-启用 |
| configUrl | string | API URL（HTTP模式使用） |
| configAuthType | string | 认证方式：api_key/bearer/oauth2 |
| configCredentials | string | 认证凭证 |
| configTimeout | int | 超时时间（秒） |
| configMethod | string | 请求方法：GET/POST |
| command | string | 启动命令（进程模式使用） |
| args | string | 命令参数（进程模式使用） |
| env | string | 环境变量（进程模式使用） |
| sort | int | 排序序号 |

---

### 二、提示模板管理（PromptTemplateController）

#### 2.1 功能概述

提示模板管理模块支持创建、管理和使用提示词模板，支持三种模板类型：
- **我的模板（is_system=0）**：用户自己创建的模板
- **系统模板（is_system=1）**：系统预置的模板
- **共享模板（is_system=2）**：共享给其他用户的模板

#### 2.2 新增文件

| 文件路径 | 说明 |
| -------- | ---- |
| `domain/entity/PromptTemplate.java` | 提示模板实体类 |
| `mapper/PromptTemplateMapper.java` | 提示模板Mapper接口 |
| `service/PromptTemplateService.java` | 提示模板业务接口 |
| `service/impl/PromptTemplateServiceImpl.java` | 提示模板业务实现 |
| `controller/PromptTemplateController.java` | 提示模板REST控制器 |

#### 2.3 数据库表结构

```sql
CREATE TABLE `prompt_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `content` text NOT NULL COMMENT '模板内容，支持{{变量名}}占位符',
  `variables` json DEFAULT NULL COMMENT '变量列表JSON数组',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `is_default` tinyint(4) DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `is_system` tinyint(4) DEFAULT 0 COMMENT '模板类型：0-我的模板，1-系统模板，2-共享模板',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_is_system` (`is_system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提示模板表';
```

#### 2.4 is_system字段说明

| 值 | 类型 | 说明 | 创建者 |
| -- | ---- | ---- | ------ |
| 0 | 我的模板 | 当前用户创建的私有模板 | 当前登录用户 |
| 1 | 系统模板 | 系统预置的模板 | 系统 |
| 2 | 共享模板 | 共享给其他用户的模板 | 当前登录用户 |

#### 2.5 API接口

| 接口 | 方法 | 说明 |
| ---- | ---- | ---- |
| `/api/v1/templates?type=my\|system\|shared` | GET | 查询模板列表 |
| `/api/v1/templates/{id}` | GET | 获取模板详情 |
| `/api/v1/templates` | POST | 创建模板 |
| `/api/v1/templates/{id}` | PUT | 更新模板 |
| `/api/v1/templates/{id}` | DELETE | 删除模板 |
| `/api/v1/templates/{id}/default` | PUT | 设为默认模板 |

#### 2.6 请求示例

```json
// 创建模板请求
POST /api/v1/templates
Content-Type: application/json

{
  "name": "文档摘要模板",
  "content": "请用100字以内总结以下内容：\n\n{{content}}\n\n请用简洁的语言概括要点。",
  "variables": "[\"content\"]",
  "description": "用于生成文档摘要的提示词模板",
  "sort": 1
}

// 创建共享模板（isSystem=2）
POST /api/v1/templates
Content-Type: application/json

{
  "name": "共享摘要模板",
  "content": "请用{{language}}总结以下内容：\n\n{{content}}\n\n总结要点：",
  "variables": "[\"language\", \"content\"]",
  "description": "共享给所有用户的摘要模板",
  "isSystem": 2,
  "sort": 0
}

// 响应
{
  "code": 200,
  "data": 123,
  "message": "success"
}
```

#### 2.7 模板变量支持

模板支持使用 `{{变量名}}` 格式的占位符，支持的预定义变量：

| 变量名 | 说明 |
| ------ | ---- |
| content | 文档内容或用户输入 |
| question | 用户问题 |
| language | 语言（zh/en等） |
| username | 用户名 |
| date | 当前日期 |

示例模板：
```
请用{{language}}总结以下{{question}}相关内容：

{{content}}

总结要求：
1. 简洁明了
2. 突出重点
3. 格式清晰
```

---

### 三、前端集成

#### 3.1 新增前端文件

| 文件路径 | 说明 |
| -------- | ---- |
| `api/mcpService.ts` | MCP服务API客户端 |
| `api/promptTemplate.ts` | 提示模板API客户端 |
| `views/mcp-service/index.vue` | MCP服务管理页面 |
| `views/prompt-template/index.vue` | 提示模板管理页面 |
| `views/prompt-template/components/TemplateFormDialog.vue` | 模板表单对话框 |
| `views/prompt-template/components/TemplateList.vue` | 模板列表组件 |

#### 3.2 路由配置

```typescript
// router/index.ts
{
  path: '/system/mcp',
  component: () => import('@/layout/index.vue'),
  meta: { title: 'MCP服务管理', icon: 'Connection' },
  children: [
    {
      path: '',
      name: 'McpService',
      component: () => import('@/views/mcp-service/index.vue'),
      meta: { title: 'MCP服务管理' }
    }
  ]
},
{
  path: '/system/prompt-template',
  component: () => import('@/layout/index.vue'),
  meta: { title: '提示模板管理', icon: 'DocumentCopy' },
  children: [
    {
      path: '',
      name: 'PromptTemplate',
      component: () => import('@/views/prompt-template/index.vue'),
      meta: { title: '提示模板管理' }
    }
  ]
}
```

#### 3.3 API函数

```typescript
// mcpService.ts
export function getMcpServices()
export function getMcpService(id: number)
export function createMcpService(data: CreateMcpServiceRequest)
export function updateMcpService(id: number, data: CreateMcpServiceRequest)
export function deleteMcpService(id: number)
export function toggleMcpService(id: number)

// promptTemplate.ts
export function getTemplates(type: TemplateType)
export function getTemplate(id: number)
export function createTemplate(data: CreateTemplateRequest, isSystem?: 0 | 1 | 2)
export function createSharedTemplate(data: CreateTemplateRequest)
export function updateTemplate(id: number, data: CreateTemplateRequest)
export function deleteTemplate(id: number)
export function setDefault(id: number)
```

#### 3.4 功能特性

**MCP服务管理页面**：
- 服务列表展示（表格）
- 添加/编辑MCP服务
- 启用/禁用状态切换
- 删除服务
- 支持HTTP模式和进程启动模式

**提示模板管理页面**：
- 三个标签页：我的模板、系统模板、共享模板
- 创建个人模板或共享模板
- 编辑/删除模板
- 复制模板
- 设为默认模板
- 模板变量自动识别
- 创建时可选"共享给其他用户"

