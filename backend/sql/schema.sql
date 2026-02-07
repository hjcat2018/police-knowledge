-- Police Knowledge Base System Database Schema
-- Database: police_kb
-- Character Set: utf8mb4
-- Collation: utf8mb4_unicode_ci

CREATE DATABASE IF NOT EXISTS police_kb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE police_kb;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `dept_id` BIGINT COMMENT '部门ID',
    `station_id` BIGINT COMMENT '派出所ID',
    `gender` TINYINT COMMENT '性别: 0-未知 1-男 2-女',
    `id_card` VARCHAR(20) COMMENT '身份证号',
    `police_no` VARCHAR(50) COMMENT '警号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_police_no` (`police_no`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_station_id` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) COMMENT '描述',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `description` VARCHAR(200) COMMENT '描述',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: menu-菜单 button-按钮',
    `parent_id` BIGINT COMMENT '父级ID',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `path` VARCHAR(200) COMMENT '路由路径',
    `icon` VARCHAR(100) COMMENT '图标',
    `component` VARCHAR(200) COMMENT '组件路径',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `code` VARCHAR(50) NOT NULL COMMENT '知识库编码',
    `description` VARCHAR(500) COMMENT '描述',
    `cover` VARCHAR(500) COMMENT '封面图',
    `category_id` BIGINT COMMENT '分类ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
    `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开: 0-否 1-是',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `doc_count` INT NOT NULL DEFAULT 0 COMMENT '文档数量',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 文档表
CREATE TABLE IF NOT EXISTS `document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `content` LONGTEXT COMMENT '文档内容',
    `summary` VARCHAR(500) COMMENT '摘要',
    `cover` VARCHAR(500) COMMENT '封面图',
    `category_id` BIGINT COMMENT '分类ID',
    `kb_id` BIGINT NOT NULL COMMENT '所属知识库ID',
    `tags` VARCHAR(500) COMMENT '标签，逗号分隔',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-待审核',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门: 0-否 1-是',
    `source` VARCHAR(200) COMMENT '来源',
    `author` VARCHAR(100) COMMENT '作者',
    `publish_time` DATETIME COMMENT '发布时间',
    `vector_id` VARCHAR(100) COMMENT '向量ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_kb_id` (`kb_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `code` VARCHAR(50) COMMENT '分类编码',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `type` VARCHAR(20) NOT NULL DEFAULT 'kb' COMMENT '类型: kb-知识库分类 doc-文档分类',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(100) COMMENT '会话标题',
    `message_count` INT NOT NULL DEFAULT 0 COMMENT '消息数量',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `sys_role` VARCHAR(20) NOT NULL COMMENT '角色: user-用户 assistant-助手',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 发布审核表
CREATE TABLE IF NOT EXISTS `publish_audit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核ID',
    `doc_id` BIGINT NOT NULL COMMENT '文档ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
    `applicant_reason` VARCHAR(500) COMMENT '申请理由',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核 1-通过 2-拒绝',
    `auditor_id` BIGINT COMMENT '审核人ID',
    `audit_reason` VARCHAR(500) COMMENT '审核理由',
    `audit_time` DATETIME COMMENT '审核时间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_doc_id` (`doc_id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发布审核表';

-- 审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `username` VARCHAR(50) COMMENT '用户名',
    `operation` VARCHAR(50) COMMENT '操作类型',
    `method` VARCHAR(100) COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `user_agent` VARCHAR(500) COMMENT 'User-Agent',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-失败 1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation` (`operation`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `doc_id` BIGINT NOT NULL COMMENT '文档ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_doc` (`user_id`, `doc_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_doc_id` (`doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 搜索历史表
CREATE TABLE IF NOT EXISTS `search_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '历史ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    `result_count` INT NOT NULL DEFAULT 0 COMMENT '搜索结果数',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_keyword` (`keyword`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史表';

-- 插入默认管理员用户
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 1);

-- 插入默认角色
INSERT INTO `sys_role` (`code`, `name`, `description`, `sort`, `status`) VALUES
('ADMIN', '系统管理员', '拥有所有权限', 1, 1),
('MANAGER', '知识管理员', '管理知识库和文档', 2, 1),
('USER', '普通用户', '基础用户权限', 3, 1);

-- 插入默认权限
INSERT INTO `sys_permission` (`code`, `name`, `type`, `parent_id`, `sort`, `path`, `component`, `status`) VALUES
('dashboard', '仪表盘', 'menu', 0, 1, '/dashboard', 'dashboard/index', 1),
('kb-manage', '知识库管理', 'menu', 0, 2, '/kb', NULL, 1),
('kb-list', '知识库列表', 'menu', 2, 1, '/kb/list', 'kb/list', 1),
('doc-manage', '文档管理', 'menu', 0, 3, '/doc', NULL, 1),
('doc-list', '文档列表', 'menu', 4, 1, '/doc/list', 'doc/list', 1),
('chat-manage', '智能问答', 'menu', 0, 4, '/chat', 'chat/index', 1),
('user-manage', '用户管理', 'menu', 0, 5, '/system/user', NULL, 1),
('user-list', '用户列表', 'menu', 7, 1, '/system/user/list', 'system/user/list', 1),
('role-manage', '角色管理', 'menu', 0, 6, '/system/role', NULL, 1),
('role-list', '角色列表', 'menu', 9, 1, '/system/role/list', 'system/role/list', 1),
('perm-manage', '权限管理', 'menu', 0, 7, '/system/permission', NULL, 1),
('perm-list', '权限列表', 'menu', 11, 1, '/system/permission/list', 'system/permission/list', 1),
('system-manage', '系统管理', 'menu', 0, 8, '/system', NULL, 1),
('audit-log', '审计日志', 'menu', 13, 1, '/system/audit', 'system/audit/index', 1);

-- 给管理员用户分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 给角色分配权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6),
(3, 1), (3, 6);

-- 插入默认分类
INSERT INTO `category` (`name`, `code`, `parent_id`, `sort`, `status`, `type`) VALUES
('法律法规', 'law', 0, 1, 1, 'kb'),
('业务规范', 'business', 0, 2, 1, 'kb'),
('操作指南', 'guide', 0, 3, 1, 'kb'),
('常见问题', 'faq', 0, 4, 1, 'kb');

-- ============================================================
-- OceanBase SeekDB AI 函数配置
-- 官方文档: https://www.oceanbase.ai/docs/experience-ai-function
-- 更新日期: 2026-01-21
-- ============================================================

-- AI 函数列表:
-- AI_EMBED: 将文本转换为向量 (使用端点名称)
-- AI_COMPLETE: 调用文本生成大模型
-- AI_PROMPT: 组织提示模板
-- AI_RERANK: 文本重排序

-- ============================================================
-- Step 1: 清理旧模型和端点
-- ============================================================

-- 删除旧模型和端点（如果存在）
CALL DBMS_AI_SERVICE.DROP_AI_MODEL('ob_embed');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL('ob_rerank');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL_ENDPOINT('ob_embed_endpoint');
CALL DBMS_AI_SERVICE.DROP_AI_MODEL_ENDPOINT('ob_rerank_endpoint');

-- ============================================================
-- Step 2: 注册 AI 模型 (text-embedding-v3)
-- DashScope 官方文档: https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-v3
-- ============================================================

CALL DBMS_AI_SERVICE.CREATE_AI_MODEL(
    'ob_embed',
    '{
      "type": "dense_embedding",
      "model_name": "text-embedding-v3"
    }'
);

-- ============================================================
-- Step 3: 注册 AI 端点 (DashScope OpenAI 兼容接口)
-- ============================================================

CALL DBMS_AI_SERVICE.CREATE_AI_MODEL_ENDPOINT(
    'ob_embed_endpoint',
    '{
      "ai_model_name": "ob_embed",
      "url": "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings",
      "access_key": "sk-e8d86d11e21540d9b72bc83f78bf1154",
      "provider": "openai"
    }'
);

-- ============================================================
-- Step 4: 测试 AI_EMBED 函数
-- ============================================================

-- 测试向量生成（使用模型名称）
SELECT AI_EMBED('ob_embed', 'Hello world') AS embedding;

-- ============================================================
-- SeekDB 向量库表
-- ============================================================

CREATE TABLE IF NOT EXISTS `document_vectors` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `content` TEXT NOT NULL COMMENT '分块内容',
    `vector` VECTOR(1024) NOT NULL COMMENT '向量数据',
    `chunk_index` INT NOT NULL DEFAULT 0 COMMENT '段落序号',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_created_time` (`created_time`),
    VECTOR INDEX `idx_vector` (`vector`) WITH (distance=COSINE, type=hnsw, lib=vsag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档向量表 (SeekDB)';


-- SeekDB 混合搜索存储过程
-- 官方文档: https://www.oceanbase.ai/docs/experience-hybrid-search
-- ============================================================

DELIMITER //

-- 使用 AI_EMBED 生成向量并存储
-- 示例: CALL sp_upsert_document_vector(1, '这是文档内容');
CREATE PROCEDURE sp_upsert_document_vector(
    IN p_document_id BIGINT,
    IN p_content TEXT
)
BEGIN
    DECLARE v_vector TEXT;
    
    -- 使用 SeekDB AI_EMBED 函数生成向量（使用模型名称）
    SET v_vector = AI_EMBED('ob_embed', p_content);
    
    -- 删除旧向量
    DELETE FROM document_vectors WHERE document_id = p_document_id;
    
    -- 插入新向量
    INSERT INTO document_vectors (document_id, content, vector, created_time)
    VALUES (p_document_id, p_content, v_vector, NOW());
END //

-- 混合搜索 (向量 + 全文)
-- 示例: CALL sp_hybrid_search('搜索关键词', 10);
CREATE PROCEDURE sp_hybrid_search(
    IN p_keyword VARCHAR(500),
    IN p_top_k INT
)
BEGIN
    DECLARE v_table_name VARCHAR(100) DEFAULT 'document_vectors';
    DECLARE v_param TEXT;
    
    -- 构建混合搜索参数
    SET v_param = CONCAT('{
      "query": {
        "query_string": {
          "fields": ["content"],
          "query": "', p_keyword, '",
          "boost": 1.0
        }
      },
      "knn": {
        "field": "vector",
        "k": ', p_top_k, ',
        "query_vector": AI_EMBED('ob_embed', "', p_keyword, '"),
        "boost": 1.0
      }
    }');
    
    SET @sql = CONCAT('SELECT json_pretty(DBMS_HYBRID_SEARCH.SEARCH(''', v_table_name, ''', ''', REPLACE(v_param, '''', ''''''), ''')) AS search_result');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE;
END //

-- 纯向量搜索
-- 示例: CALL sp_vector_search('搜索关键词', 10);
CREATE PROCEDURE sp_vector_search(
    IN p_keyword VARCHAR(500),
    IN p_top_k INT
)
BEGIN
    DECLARE v_table_name VARCHAR(100) DEFAULT 'document_vectors';
    DECLARE v_param TEXT;
    
    SET v_param = CONCAT('{
      "knn": {
        "field": "vector",
        "k": ', p_top_k, ',
        "query_vector": AI_EMBED('ob_embed', "', p_keyword, '"),
        "boost": 1.0
      }
    }');
    
    SET @sql = CONCAT('SELECT json_pretty(DBMS_HYBRID_SEARCH.SEARCH(''', v_table_name, ''', ''', REPLACE(v_param, '''', ''''''), ''')) AS search_result');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE;
END //

-- 纯全文搜索
-- 示例: CALL sp_fulltext_search('搜索关键词', 10);
CREATE PROCEDURE sp_fulltext_search(
    IN p_keyword VARCHAR(500),
    IN p_top_k INT
)
BEGIN
    DECLARE v_table_name VARCHAR(100) DEFAULT 'document_vectors';
    DECLARE v_param TEXT;
    
    SET v_param = CONCAT('{
      "query": {
        "query_string": {
          "fields": ["content"],
          "query": "', p_keyword, '",
          "boost": 1.0
        }
      }
    }');
    
    SET @sql = CONCAT('SELECT json_pretty(DBMS_HYBRID_SEARCH.SEARCH(''', v_table_name, ''', ''', REPLACE(v_param, '''', ''''''), ''')) AS search_result');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE;
END //

DELIMITER ;



