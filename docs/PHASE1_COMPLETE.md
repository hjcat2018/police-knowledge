# 公安专网知识库系统 - 第一阶段完成模块功能文档

## 文档信息

| 项目 | 内容 |
|-----|------|
| **文档版本** | V1.0 |
| **创建日期** | 2026-01-20 |
| **开发阶段** | 第一阶段：基础框架 + 认证模块 + 向量库集成 |
| **技术栈** | Spring Boot 3.5.7 + Sa-Token 1.38.0 + MyBatis-Plus 3.5.5 + OceanBase SeekDB |

---

## 一、项目架构总览

### 1.1 技术架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                          前端层 (Frontend)                           │
│  Vue 3.5+ + TypeScript 5.x + Vite 6.x + Element Plus 2.x + Pinia │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          API 网关层                                   │
│                    Spring Boot 3.5.7 REST API                       │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  - 认证模块 (AuthController)                               │    │
│  │  - 用户模块 (UserController)                               │    │
│  │  - 系统模块 (SystemController)                            │    │
│  │  - 向量搜索模块 (VectorService)                           │    │
│  └─────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   认证/授权层    │  │   数据持久层     │  │  向量数据库层    │
│  Sa-Token JWT   │  │ MyBatis-Plus    │  │ OceanBase SeekDB │
│  Redis Session  │  │   MySQL 8.0     │  │   (AI Functions) │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

### 1.2 目录结构

```
backend/src/main/java/com/police/kb/
├── common/                 # 公共组件
│   ├── Result.java        # 统一响应结果封装
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── config/                # 配置类
│   ├── SaTokenConfig.java       # Sa-Token + JWT 配置
│   ├── RedisConfig.java         # Redis 配置
│   ├── MybatisPlusConfig.java   # MyBatis-Plus 配置
│   ├── WebMvcConfig.java        # Web MVC 配置
│   ├── MyMetaObjectHandler.java # 自动填充处理器
│   └── SeekDBConfig.java        # SeekDB 向量库配置
├── controller/            # 控制器层
│   ├── AuthController.java      # 认证控制器
│   ├── UserController.java     # 用户管理控制器
│   └── SystemController.java   # 系统控制器
├── dto/                   # 数据传输对象
│   ├── LoginRequest.java       # 登录请求DTO
│   ├── LoginResponse.java      # 登录响应DTO
│   ├── UserInfoDTO.java        # 用户信息DTO
│   └── ChangePasswordRequest.java  # 修改密码请求DTO
├── entity/                # 实体类
│   ├── base/                   # 基础实体
│   │   └── BaseEntity.java     # 基础实体（含主键、审计字段）
│   ├── User.java              # 用户实体
│   ├── Role.java              # 角色实体
│   ├── Permission.java        # 权限实体
│   ├── Category.java          # 分类实体
│   ├── Document.java          # 文档实体
│   ├── DocumentVector.java    # 文档向量实体
│   ├── UserRole.java          # 用户角色关联实体
│   └── RolePermission.java    # 角色权限关联实体
├── repository/            # 数据访问层 (MyBatis Mapper)
│   ├── UserMapper.java         # 用户Mapper
│   ├── RoleMapper.java         # 角色Mapper
│   ├── PermissionMapper.java   # 权限Mapper
│   ├── CategoryMapper.java     # 分类Mapper
│   ├── DocumentMapper.java     # 文档Mapper
│   ├── DocumentVectorMapper.java # 向量Mapper
│   ├── UserRoleMapper.java     # 用户角色Mapper
│   └── RolePermissionMapper.java # 角色权限Mapper
└── service/               # 业务逻辑层
    ├── AuthService.java        # 认证服务
    ├── UserService.java        # 用户服务
    ├── UserRoleService.java    # 用户角色服务
    └── VectorService.java      # 向量服务 (SeekDB)
```

---

## 二、认证授权模块 (核心模块)

### 2.1 模块概述

认证授权模块是系统的核心安全模块，提供完整的用户认证、授权和会话管理功能。

| 功能项 | 说明 |
|-------|------|
| **认证方式** | JWT Token + Redis Session |
| **安全框架** | Sa-Token 1.38.0 |
| **密码加密** | BCrypt (Hutool) |
| **Token超时** | 30分钟 (可配置) |

### 2.2 核心功能

#### 2.2.1 用户登录

**接口信息**

| 项目 | 内容 |
|-----|------|
| **接口地址** | `POST /api/v1/auth/login` |
| **请求方式** | POST |
| **Content-Type** | application/json |

**请求参数**

```json
{
  "username": "admin",
  "password": "123456",
  "deviceType": "PC",
  "captcha": "",
  "captchaKey": ""
}
```

**响应结果**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "avatar": "",
    "phone": "13800000000",
    "email": "admin@police.gov.cn",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 1800,
    "tokenType": "jwt",
    "roles": ["ADMIN"],
    "permissions": ["user:list", "user:add", "user:edit"]
  }
}
```

**处理流程**

```
1. 验证用户名是否存在
2. 验证密码是否正确 (BCrypt)
3. 检查用户状态 (是否禁用)
4. 执行登录 (StpUtil.login)
5. 更新最后登录时间和IP
6. 获取用户角色和权限
7. 返回Token和用户信息
```

#### 2.2.2 用户登出

**接口信息**

| 项目 | 内容 |
|-----|------|
| **接口地址** | `POST /api/v1/auth/logout` |
| **请求方式** | POST |
| **是否需要登录** | 是 |

**处理逻辑**

```java
public void logout() {
    if (StpUtil.isLogin()) {
        StpUtil.logout();  // 清除Redis Session
    }
}
```

#### 2.2.3 获取当前用户信息

**接口信息**

| 项目 | 内容 |
|-----|------|
| **接口地址** | `GET /api/v1/auth/info` |
| **请求方式** | GET |
| **是否需要登录** | 是 |

**响应结果**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "phone": "13800000000",
    "email": "admin@police.gov.cn",
    "gender": 1,
    "idCard": "110101199001011234",
    "policeNo": "PJ000001",
    "roles": ["ADMIN"],
    "permissions": ["user:list", "user:add", "user:edit", "user:delete"]
  }
}
```

#### 2.2.4 修改密码

**接口信息**

| 项目 | 内容 |
|-----|------|
| **接口地址** | `POST /api/v1/auth/change-password` |
| **请求方式** | POST |
| **是否需要登录** | 是 |

**请求参数**

```json
{
  "oldPassword": "123456",
  "newPassword": "abcdef123456",
  "confirmPassword": "abcdef123456"
}
```

**验证规则**

1. 旧密码必须正确
2. 新密码和确认密码必须一致
3. 密码修改后自动踢出所有登录会话

### 2.3 Sa-Token 配置

**配置文件 (application.yml)**

```yaml
sa-token:
  token-name: X-Token      # Token Header名称
  timeout: 1800            # Token有效期(秒)
  is-concurrent: true       # 是否允许并发登录
  is-share: false          # 是否共享Session
  token-style: jwt          # Token风格 (jwt)
  jwt-secret-key: police-kb-jwt-secret-key-2025-xxx  # JWT密钥
```

**核心配置类**

```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/v1/auth/login",
                "/api/v1/auth/logout",
                "/api/v1/system/**"
            );
    }
}
```

### 2.4 权限模型

**RBAC (Role-Based Access Control) 权限模型**

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │──────▶│    Role     │──────▶│ Permission │
│   (用户)    │       │   (角色)    │       │   (权限)    │
└─────────────┘       └─────────────┘       └─────────────┘
     │                     │                     │
     │  一对多             │  一对多            │
     ▼                     ▼                     ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│ user_role   │       │ role_perm   │       │             │
│ 关联表      │       │ 关联表      │       │             │
└─────────────┘       └─────────────┘       └─────────────┘
```

**默认角色**

| 角色编码 | 角色名称 | 描述 |
|---------|---------|------|
| ADMIN | 系统管理员 | 拥有所有权限 |
| MANAGER | 知识管理员 | 管理知识库和文档 |
| USER | 普通用户 | 基础用户权限 |

### 2.5 核心代码结构

**AuthService 核心方法**

| 方法名 | 功能 | 事务 |
|-------|------|------|
| `login(LoginRequest)` | 用户登录 | ✅ |
| `logout()` | 用户登出 | ❌ |
| `getCurrentUserInfo()` | 获取当前用户信息 | ❌ |
| `changePassword(ChangePasswordRequest)` | 修改密码 | ✅ |
| `hasRole(String)` | 检查角色 | ❌ |
| `hasPermission(String)` | 检查权限 | ❌ |

---

## 三、用户管理模块

### 3.1 功能列表

| 功能 | 接口地址 | 方法 | 说明 |
|-----|---------|------|------|
| 分页查询 | `/api/v1/users` | GET | 支持关键词搜索 |
| 获取详情 | `/api/v1/users/{id}` | GET | 根据ID获取 |
| 创建用户 | `/api/v1/users` | POST | 新增用户 |
| 更新用户 | `/api/v1/users/{id}` | PUT | 更新用户 |
| 删除用户 | `/api/v1/users/{id}` | DELETE | 删除用户 |
| 批量删除 | `/api/v1/users/batch` | DELETE | 批量删除 |
| 修改状态 | `/api/v1/users/{id}/status` | PUT | 启用/禁用 |
| 重置密码 | `/api/v1/users/{id}/reset-password` | PUT | 重置为123456 |

### 3.2 分页查询

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| keyword | String | 否 | 搜索关键词 |

**响应结果**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "phone": "13800000000",
        "status": 1,
        "lastLoginTime": "2026-01-20 10:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

## 四、向量搜索模块 (SeekDB集成)

### 4.1 模块概述

基于 OceanBase SeekDB 的 AI Native 向量搜索功能，支持混合搜索（向量+全文）。

| 功能 | 说明 |
|-----|------|
| **向量生成** | 使用 SeekDB AI_EMBED 函数 |
| **向量存储** | 存储在 document_vectors 表 |
| **向量搜索** | 使用 DBMS_HYBRID_SEARCH.SEARCH |
| **全文搜索** | SeekDB 原生全文索引 |
| **混合搜索** | 向量 + 全文融合 |

### 4.2 SeekDB 配置

**配置文件 (application.yml)**

```yaml
seekdb:
  host: 192.168.6.201
  port: 2881
  username: root@test
  password: hj761209
  database: police_kb
  collection: document_vectors
  dimension: 1024
  metric: COSINE
```

### 4.3 核心功能

#### 4.3.1 生成并保存向量

```java
public void embedAndSave(Long documentId, String content) {
    String sql = "INSERT INTO document_vectors " +
                "(document_id, content, vector, created_time) " +
                "VALUES (?, ?, AI_EMBED(?, ?), NOW())";
    jdbcTemplate.update(sql, documentId, content, "ob_embed", content);
}
```

#### 4.3.2 混合搜索

```java
public List<SearchResult> hybridSearch(String query, int topK) {
    // 1. 使用 AI_EMBED 生成查询向量
    String queryVector = AI_EMBED('ob_embed', query);
    
    // 2. 构建混合搜索参数
    Map<String, Object> param = new HashMap<>();
    param.put("query", {
        "query_string": {
            "fields": ["content"],
            "query": query,
            "boost": 1.0
        }
    });
    param.put("knn", {
        "field": "vector",
        "k": topK,
        "query_vector": queryVector,
        "boost": 1.0
    });
    
    // 3. 执行混合搜索
    String result = DBMS_HYBRID_SEARCH.SEARCH('document_vectors', param);
    return parseSearchResults(result);
}
```

#### 4.3.3 重排序搜索

```java
public List<SearchResult> rerankSearch(String query, List<String> documents, int topK) {
    String result = AI_RERANK('ob_rerank', query, 
        JSON.toJSONString(documents), topK);
    return parseRerankResults(result);
}
```

### 4.4 API 接口

| 功能 | 接口地址 | 方法 | 说明 |
|-----|---------|------|------|
| 混合搜索 | `/api/v1/search/hybrid` | POST | 向量+全文融合 |
| 向量搜索 | `/api/v1/search/vector` | POST | 纯向量搜索 |
| 全文搜索 | `/api/v1/search/fulltext` | POST | 纯全文搜索 |
| 重排序 | `/api/v1/search/rerank` | POST | AI重排序 |

---

## 五、数据库设计

### 5.1 核心表结构

#### 5.1.1 用户表 (user)

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 (唯一) |
| password | VARCHAR(100) | 密码 (BCrypt) |
| real_name | VARCHAR(50) | 真实姓名 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| status | TINYINT | 状态: 0-禁用 1-启用 |
| last_login_time | DATETIME | 最后登录时间 |
| last_login_ip | VARCHAR(50) | 最后登录IP |

#### 5.1.2 文档表 (document)

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 文档标题 |
| content | LONGTEXT | 文档内容 |
| kb_id | BIGINT | 所属知识库ID |
| category_id | BIGINT | 分类ID |
| status | TINYINT | 状态: 0-草稿 1-已发布 |
| vector_id | VARCHAR(100) | 向量ID |

#### 5.1.3 向量表 (document_vectors)

| 字段 | 类型 | 说明 |
|-----|------|------|
| id | BIGINT | 主键 |
| document_id | BIGINT | 文档ID |
| content | TEXT | 分块内容 |
| vector | VECTOR(1024) | 向量数据 |
| metadata | JSON | 元数据 |

### 5.2 数据库脚本

数据库初始化脚本位于 `backend/sql/schema.sql`，包含：

- 12张核心业务表
- SeekDB AI 模型注册存储过程
- 混合搜索存储过程
- 初始用户、角色、权限数据

---

## 六、API文档

### 6.1 认证模块 API

#### 6.1.1 用户登录

```
POST /api/v1/auth/login

Request:
{
  "username": "admin",
  "password": "123456"
}

Response (200):
{
  "code": 200,
  "data": {
    "token": "xxx",
    "expiresIn": 1800,
    "roles": ["ADMIN"]
  }
}
```

#### 6.1.2 用户登出

```
POST /api/v1/auth/logout
Authorization: X-Token xxx

Response (200):
{
  "message": "登出成功"
}
```

#### 6.1.3 获取用户信息

```
GET /api/v1/auth/info
Authorization: X-Token xxx

Response (200):
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "roles": ["ADMIN"],
    "permissions": ["user:list"]
  }
}
```

### 6.2 用户管理 API

#### 6.2.1 分页查询

```
GET /api/v1/users?pageNum=1&pageSize=10&keyword=admin

Response (200):
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

---

## 七、部署配置

### 7.1 环境要求

| 软件 | 版本要求 |
|-----|---------|
| JDK | 17+ |
| Maven | 3.9+ |
| MySQL/OceanBase | 8.0+ |
| Redis | 7.x |

### 7.2 配置文件

主要配置文件：`backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:oceanbase://192.168.6.201:2881/police_kb
    username: root
    password: hj761209

  data:
    redis:
      host: 192.168.6.201
      port: 6379

sa-token:
  token-name: X-Token
  timeout: 1800
  token-style: jwt
```

---

## 八、第一阶段完成清单

### ✅ 已完成模块

| 模块 | 状态 | 功能点 |
|-----|------|--------|
| **认证授权模块** | ✅ 完成 | JWT登录、登出、密码修改、RBAC权限 |
| **用户管理模块** | ✅ 完成 | CRUD、分页、状态管理、密码重置 |
| **向量搜索模块** | ✅ 完成 | AI_EMBED、混合搜索、AI_RERANK |
| **基础框架** | ✅ 完成 | 项目配置、异常处理、统一响应 |
| **SeekDB集成** | ✅ 完成 | 向量生成、存储、搜索 |

### ⏳ 待完成模块

| 模块 | 预估工时 | 功能点 |
|-----|----------|--------|
| **知识库管理** | 3天 | 知识库CRUD、分类管理 |
| **文档管理** | 5天 | 文档CRUD、文件上传、版本管理 |
| **智能问答** | 5天 | RAG流程、对话历史 |
| **系统管理** | 2天 | 角色权限配置、操作日志 |
| **前端界面** | 10天 | 完整UI界面 |

---

## 九、开发规范

### 9.1 代码规范

- Controller 层只处理请求/响应
- Service 层处理业务逻辑
- Repository 层只做数据访问
- Entity 保持纯净，不含业务逻辑

### 9.2 分层结构

```
Controller → Service → Repository → Database
   ▲            │           │
   └────────────┴───────────┘
              异常处理
```

### 9.3 命名规范

| 类型 | 规范 | 示例 |
|-----|------|------|
| Controller | XxxController | AuthController |
| Service | XxxService | AuthService |
| Mapper | XxxMapper | UserMapper |
| Entity | Xxx | User |
| DTO | XxxDTO | LoginRequest |

---

## 十、版本记录

### V1.0 (2026-01-20) - 第一阶段

**新增功能**

- ✅ 用户认证登录/登出
- ✅ JWT Token 认证
- ✅ RBAC 权限模型
- ✅ 用户CRUD管理
- ✅ OceanBase SeekDB 向量集成
- ✅ AI_EMBED 向量生成
- ✅ DBMS_HYBRID_SEARCH 混合搜索
- ✅ 统一响应封装
- ✅ 全局异常处理

---

*文档生成时间: 2026-01-20*
*如有疑问，请联系系统管理员*
