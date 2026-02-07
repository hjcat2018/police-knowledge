# 数据库设计文档

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **数据库**: MySQL 8.0+ | OceanBase SeekDB 1.0+

---

## 一、ER图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           数据库 ER 图                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────┐      ┌─────────────┐      ┌─────────────┐            │
│  │    user     │      │    role     │      │ permission  │            │
│  ├─────────────┤      ├─────────────┤      ├─────────────┤            │
│  │ id (PK)     │◄────►│ id (PK)     │◄────►│ id (PK)     │            │
│  │ username    │      │ name        │      │ name        │            │
│  │ password    │      │ code        │      │ code        │            │
│  │ police_no   │      │ description │      │ description │            │
│  │ department  │      └─────────────┘      └─────────────┘            │
│  │ role_id (FK)│             ▲                                           │
│  │ status      │             │                                           │
│  │ created_at  │      ┌───────┴───────┐                                 │
│  └─────────────┘      │ user_role     │                                 │
│         │             ├───────────────┤                                 │
│         │             │ user_id (FK)  │                                 │
│         │             │ role_id (FK)  │                                 │
│         │             └───────────────┘                                 │
│         │                                                                   │
│         │◄──────────────────────────────────────────┐                    │
│  ┌──────┴──────────────────────────────────────────┴─────────────┐      │
│  │                   knowledge_base                          │            │
│  ├─────────────────────────────────────────────────────────────┤            │
│  │ id (PK)                                                    │            │
│  │ name                                                       │            │
│  │ description                                                │            │
│  │ owner_id (FK) → user.id                                    │            │
│  │ visibility (PRIVATE/SHARED)                                │            │
│  │ status (CREATING/READY/ARCHIVED)                           │            │
│  │ settings (JSON)                                            │            │
│  │ document_count                                             │            │
│  │ created_at                                                 │            │
│  │ updated_at                                                 │            │
│  └─────────────────────────────────────────────────────────────┘            │
│                                │                                           │
│                                ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                      document                                   │      │
│  ├─────────────────────────────────────────────────────────────────┤      │
│  │ id (PK)                                                        │      │
│  │ knowledge_base_id (FK) → knowledge_base.id                     │      │
│  │ title                                                          │      │
│  │ content                                                        │      │
│  │ content_vector (VECTOR)                                        │      │
│  │ police_type (警种分类)                                          │      │
│  │ business_type (业务分类)                                        │      │
│  │ source_level (来源级别)                                         │      │
│  │ doc_type (文档类型)                                             │      │
│  │ source_url (原文链接)                                           │      │
│  │ source_platform (来源平台)                                      │      │
│  │ visibility (PRIVATE/SHARED/PENDING)                            │      │
│  │ is_mechanism (是否机制类)                                       │      │
│  │ status (PENDING/PROCESSING/COMPLETED/FAILED)                   │      │
│  │ chunk_count                                                    │      │
│  │ view_count                                                     │      │
│  │ tags (JSON)                                                    │      │
│  │ creator_id (FK) → user.id                                      │      │
│  │ created_at                                                     │      │
│  │ updated_at                                                     │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                │                                           │
│                                ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                   chat_session                                  │      │
│  ├─────────────────────────────────────────────────────────────────┤      │
│  │ id (PK)                                                        │      │
│  │ user_id (FK) → user.id                                          │      │
│  │ knowledge_base_id (FK) → knowledge_base.id                      │      │
│  │ title                                                          │      │
│  │ message_count                                                  │      │
│  │ created_at                                                     │      │
│  │ updated_at                                                     │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                │                                           │
│                                ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                   chat_message                                  │      │
│  ├─────────────────────────────────────────────────────────────────┤      │
│  │ id (PK)                                                        │      │
│  │ session_id (FK) → chat_session.id                              │      │
│  │ role (USER/ASSISTANT)                                          │      │
│  │ content                                                        │      │
│  │ sources (JSON)                                                 │      │
│  │ tokens (JSON)                                                  │      │
│  │ created_at                                                     │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                   publish_audit                                 │      │
│  ├─────────────────────────────────────────────────────────────────┤      │
│  │ id (PK)                                                        │      │
│  │ document_id (FK) → document.id                                  │      │
│  │ applicant_id (FK) → user.id                                     │      │
│  │ audit_type (PUBLISH/MECHANISM)                                  │      │
│  │ reason                                                         │      │
│  │ status (PENDING/APPROVED/REJECTED)                              │      │
│  │ auditor_id (FK) → user.id                                       │      │
│  │ audit_comment                                                  │      │
│  │ applied_at                                                     │      │
│  │ audited_at                                                     │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐      │
│  │                   audit_log                                     │      │
│  ├─────────────────────────────────────────────────────────────────┤      │
│  │ id (PK)                                                        │      │
│  │ user_id (FK) → user.id                                          │      │
│  │ operation_type                                                 │      │
│  │ operation_desc                                                 │      │
│  │ resource_type                                                  │      │
│  │ resource_id                                                    │      │
│  │ ip_address                                                     │      │
│  │ user_agent                                                     │      │
│  │ status (SUCCESS/FAILED)                                        │      │
│  │ error_message                                                  │      │
│  │ created_at                                                     │      │
│  └─────────────────────────────────────────────────────────────────┘      │
│                                                                         │
│  ┌──────────────────────────┐  ┌──────────────────────────┐            │
│  │     user_favorite        │  │     search_history       │            │
│  ├──────────────────────────┤  ├──────────────────────────┤            │
│  │ id (PK)                  │  │ id (PK)                  │            │
│  │ user_id (FK)             │  │ user_id (FK)             │            │
│  │ document_id (FK)         │  │ keyword                  │            │
│  │ created_at               │  │ result_count             │            │
│  └──────────────────────────┘  │ created_at               │            │
│                                └──────────────────────────┘            │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 二、数据表DDL

### 2.1 用户表 (user)

```sql
-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `police_no` VARCHAR(20) UNIQUE COMMENT '警号',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `department` VARCHAR(100) COMMENT '所属部门',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role_id` BIGINT DEFAULT 1 COMMENT '角色ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_username` (`username`),
    INDEX `idx_police_no` (`police_no`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始管理员用户（密码: admin123）
INSERT INTO `user` (`username`, `password`, `real_name`, `role_id`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1);
```

### 2.2 角色表 (role)

```sql
-- 角色表
CREATE TABLE `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 初始角色数据
INSERT INTO `role` (`name`, `code`, `description`) VALUES
('普通民警', 'USER', '普通用户，可查询知识、进行问答'),
('知识管理员', 'MANAGER', '知识管理员，可管理知识、审核发布'),
('系统管理员', 'ADMIN', '系统管理员，拥有全部权限');
```

### 2.3 权限表 (permission)

```sql
-- 权限表
CREATE TABLE `permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    `description` VARCHAR(200) COMMENT '权限描述',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
    `type` TINYINT DEFAULT 1 COMMENT '类型：1-菜单，2-按钮',
    `path` VARCHAR(200) COMMENT '路由路径',
    `icon` VARCHAR(100) COMMENT '图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_code` (`code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 初始权限数据
INSERT INTO `permission` (`name`, `code`, `description`, `type`, `path`, `sort`) VALUES
('首页', 'dashboard', '首页仪表盘', 1, '/dashboard', 1),
('知识搜索', 'knowledge:search', '知识搜索功能', 1, '/search', 2),
('智能问答', 'knowledge:chat', '智能问答功能', 1, '/chat', 3),
('知识管理', 'knowledge:manage', '知识管理', 1, '/knowledge', 4),
('知识列表', 'knowledge:list', '知识列表', 2, '', 1),
('知识创建', 'knowledge:create', '创建知识', 2, '', 2),
('知识删除', 'knowledge:delete', '删除知识', 2, '', 3),
('审核管理', 'audit:manage', '审核管理', 1, '/audit', 5),
('审核操作', 'audit:operate', '审核操作', 2, '', 1),
('统计分析', 'statistics:view', '统计分析', 1, '/statistics', 6),
('用户管理', 'user:manage', '用户管理', 1, '/users', 7),
('用户操作', 'user:operate', '用户操作', 2, '', 1),
('系统设置', 'system:setting', '系统设置', 1, '/settings', 8);
```

### 2.4 角色权限关联表 (role_permission)

```sql
-- 角色权限关联表
CREATE TABLE `role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 为管理员角色分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `permission`;
```

### 2.5 知识库表 (knowledge_base)

```sql
-- 知识库表
CREATE TABLE `knowledge_base` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '知识库ID',
    `name` VARCHAR(200) NOT NULL COMMENT '知识库名称',
    `description` TEXT COMMENT '知识库描述',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `visibility` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '可见性：PRIVATE-私有，SHARED-共享',
    `status` VARCHAR(20) DEFAULT 'CREATING' COMMENT '状态：CREATING-创建中，READY-就绪，ARCHIVED-归档',
    `settings` JSON COMMENT '配置信息',
    `document_count` INT DEFAULT 0 COMMENT '文档数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_visibility` (`visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';
```

### 2.6 文档表 (document)

```sql
-- 文档表
CREATE TABLE `document` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
    `title` VARCHAR(500) NOT NULL COMMENT '文档标题',
    `content` LONGTEXT COMMENT '文档内容（文本）',
    `content_vector` VECTOR(1024) COMMENT '向量数据（SeekDB存储，此处可选保留）',
    
    -- 分类维度
    `police_type` VARCHAR(50) COMMENT '警种：治安/刑侦/交警/禁毒/社区/网安/特警',
    `business_type` VARCHAR(50) COMMENT '业务类型：户籍管理/治安管理/刑事侦查/网络安全等',
    `source_level` VARCHAR(20) COMMENT '来源级别：公安部/省厅/市局/区县局/派出所',
    `doc_type` VARCHAR(50) COMMENT '文档类型：法律法规/规章制度/培训资料/总结文件/技战法',
    
    -- 来源信息
    `source_url` VARCHAR(1000) COMMENT '原文链接（治安平台同步）',
    `source_platform` VARCHAR(50) COMMENT '来源平台',
    `external_id` VARCHAR(100) COMMENT '外部系统ID',
    
    -- 权限控制
    `visibility` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '可见性：PRIVATE/SHARED/PENDING',
    `is_mechanism` TINYINT DEFAULT 0 COMMENT '是否机制类：0-否，1-是',
    
    -- 状态
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/FAILED',
    `chunk_count` INT DEFAULT 0 COMMENT '分块数量',
    `error_message` TEXT COMMENT '处理错误信息',
    
    -- 统计
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
    
    -- 元数据
    `tags` JSON COMMENT '标签',
    `metadata` JSON COMMENT '扩展元数据',
    
    -- 审计字段
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_kb_id` (`knowledge_base_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_visibility` (`visibility`),
    INDEX `idx_creator` (`creator_id`),
    INDEX `idx_police_type` (`police_type`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_source_level` (`source_level`),
    INDEX `idx_doc_type` (`doc_type`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_title` (`title`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';
```

### 2.7 对话会话表 (chat_session)

```sql
-- 对话会话表
CREATE TABLE `chat_session` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '会话ID（UUID）',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `knowledge_base_id` BIGINT COMMENT '关联知识库ID',
    `title` VARCHAR(200) COMMENT '会话标题',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_kb_id` (`knowledge_base_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';
```

### 2.8 对话消息表 (chat_message)

```sql
-- 对话消息表
CREATE TABLE `chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `session_id` VARCHAR(36) NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：USER/ASSISTANT',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `sources` JSON COMMENT '来源文档',
    `tokens` JSON COMMENT 'Token使用统计',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息表';
```

### 2.9 发布审核表 (publish_audit)

```sql
-- 发布审核表
CREATE TABLE `publish_audit` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `audit_type` VARCHAR(20) NOT NULL COMMENT '审核类型：PUBLISH/MECHANISM',
    `reason` TEXT COMMENT '申请理由',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    `auditor_id` BIGINT COMMENT '审核人ID',
    `audit_comment` TEXT COMMENT '审核意见',
    `applied_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `audited_at` DATETIME COMMENT '审核时间',
    
    INDEX `idx_document_id` (`document_id`),
    INDEX `idx_applicant_id` (`applicant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_applied_at` (`applied_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布审核表';
```

### 2.10 审计日志表 (audit_log)

```sql
-- 审计日志表
CREATE TABLE `audit_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `operation_desc` VARCHAR(500) COMMENT '操作描述',
    `resource_type` VARCHAR(50) COMMENT '资源类型',
    `resource_id` VARCHAR(100) COMMENT '资源ID',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS/FAILED',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_resource_type` (`resource_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- 审计日志操作类型
-- LOGIN, LOGOUT, KNOWLEDGE_CREATE, KNOWLEDGE_UPDATE, KNOWLEDGE_DELETE,
-- DOCUMENT_UPLOAD, DOCUMENT_DELETE, CHAT_QUERY, AUDIT_APPROVE, AUDIT_REJECT
```

### 2.11 用户收藏表 (user_favorite)

```sql
-- 用户收藏表
CREATE TABLE `user_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    
    UNIQUE KEY `uk_user_doc` (`user_id`, `document_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';
```

### 2.12 搜索历史表 (search_history)

```sql
-- 搜索历史表
CREATE TABLE `search_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT COMMENT '用户ID（可为空）',
    `keyword` VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    `result_count` INT DEFAULT 0 COMMENT '结果数量',
    `search_type` VARCHAR(20) DEFAULT 'HYBRID' COMMENT '搜索类型：HYBRID/VECTOR/FT',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    
    INDEX `idx_keyword` (`keyword`(100)),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';
```

---

## 三、SeekDB向量存储设计

### 3.1 向量索引配置

```sql
-- SeekDB向量索引配置
CREATE TABLE `document_vectors` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `document_id` BIGINT NOT NULL UNIQUE COMMENT '文档ID',
    `content` TEXT COMMENT '分块内容',
    `vector` VECTOR(1024) NOT NULL COMMENT '向量数据',
    `metadata` JSON COMMENT '元数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX `idx_vector` USING HNSW (`vector`),
    INDEX `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档向量表';
```

### 3.2 混合搜索查询

```sql
-- 混合搜索示例（向量搜索 + 全文搜索）
SELECT 
    d.id,
    d.title,
    d.content,
    d.police_type,
    d.business_type,
    d.source_level,
    d.doc_type,
    d.source_platform,
    d.created_at,
    -- 融合分数 (RRF算法)
    COALESCE((1.0 / (1.0 + vs.rank)) * 0.5, 0) + 
    COALESCE((1.0 / (1.0 + ft.rank)) * 0.5, 0) AS fusion_score
FROM document d
LEFT JOIN (
    -- 向量搜索排名
    SELECT document_id, ROW_NUMBER() OVER (ORDER BY vector_similarity(vector, ?)) AS rank
    FROM document_vectors
    WHERE vector_similarity(vector, ?) < 0.8
    LIMIT 100
) vs ON d.id = vs.document_id
LEFT JOIN (
    -- 全文搜索排名
    SELECT id AS document_id, ROW_NUMBER() OVER (ORDER BY MATCH(content) AGAINST(? IN NATURAL LANGUAGE MODE)) AS rank
    FROM document
    WHERE MATCH(content) AGAINST(? IN NATURAL LANGUAGE MODE)
    LIMIT 100
) ft ON d.id = ft.document_id
WHERE d.status = 'COMPLETED' 
    AND (d.visibility = 'SHARED' OR d.creator_id = ?)
    AND (d.police_type = ? OR ? IS NULL)
ORDER BY fusion_score DESC
LIMIT 20;
```

---

## 四、初始化数据

### 4.1 初始化脚本

```sql
-- 运行初始化脚本
-- mysql -u root -p police_kb < init.sql

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS police_kb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE police_kb;

-- 2. 执行表创建DDL
-- (执行上述所有CREATE TABLE语句)

-- 3. 插入初始数据
-- (执行角色、权限初始数据INSERT语句)

-- 4. 创建视图
CREATE VIEW v_document_with_kb AS
SELECT 
    d.*,
    kb.name AS knowledge_base_name,
    kb.visibility AS kb_visibility,
    u.real_name AS creator_name,
    u.department AS creator_department
FROM document d
LEFT JOIN knowledge_base kb ON d.knowledge_base_id = kb.id
LEFT JOIN user u ON d.creator_id = u.id;

-- 5. 创建存储过程
DELIMITER //

-- 获取热门关键词
CREATE PROCEDURE sp_get_hot_keywords(IN p_top INT)
BEGIN
    SELECT 
        keyword,
        COUNT(*) AS search_count
    FROM search_history
    WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    GROUP BY keyword
    ORDER BY search_count DESC
    LIMIT p_top;
END //

-- 清理过期会话
CREATE PROCEDURE sp_cleanup_expired_sessions()
BEGIN
    DELETE FROM chat_session 
    WHERE updated_at < DATE_SUB(NOW(), INTERVAL 30 DAY)
    AND message_count = 0;
END //

DELIMITER ;
```

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日
