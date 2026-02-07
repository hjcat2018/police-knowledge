-- =============================================================================
-- Police Knowledge Base System - New Tables SQL Script
-- Database: police_kb
-- Character Set: utf8mb4
-- Collation: utf8mb4_general_ci
-- =============================================================================
-- Author: 
-- Date: 2026-02-03
-- Description: 提示词模板、MCP服务、对话设置相关表结构
-- =============================================================================

USE police_kb;

-- =============================================================================
-- 一、提示词模板表
-- =============================================================================
DROP TABLE IF EXISTS `prompt_template`;

CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `variables` JSON COMMENT '变量列表',
    `description` VARCHAR(500) COMMENT '模板描述',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模板: 0-否 1-是',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统模板: 0-否 1-是（系统模板不可删除）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_by` BIGINT COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提示词模板表';

-- =============================================================================
-- 二、对话设置表
-- =============================================================================
DROP TABLE IF EXISTS `conversation_settings`;

CREATE TABLE IF NOT EXISTS `conversation_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `template_id` BIGINT COMMENT '使用的模板ID',
    `custom_prompt` TEXT COMMENT '自定义提示词（覆盖模板）',
    `temperature` DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '温度参数: 0-1之间',
    `max_tokens` INT NOT NULL DEFAULT 2000 COMMENT '最大生成tokens',
    `top_p` DECIMAL(3,2) NOT NULL DEFAULT 0.95 COMMENT 'topP参数: 0-1之间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对话设置表';

-- =============================================================================
-- 三、MCP服务表
-- =============================================================================
DROP TABLE IF EXISTS `mcp_service`;

CREATE TABLE IF NOT EXISTS `mcp_service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `code` VARCHAR(50) NOT NULL COMMENT '服务编码',
    `description` VARCHAR(500) COMMENT '服务描述',
    `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用: 0-禁用 1-启用',
    `config_url` VARCHAR(500) COMMENT 'API URL',
    `config_auth_type` VARCHAR(20) NOT NULL DEFAULT 'api_key' COMMENT '认证方式: api_key-API Key bearer-Bearer Token oauth2-OAuth2',
    `config_credentials` VARCHAR(500) COMMENT '认证凭证',
    `config_timeout` INT NOT NULL DEFAULT 60 COMMENT '超时时间（秒）',
    `config_method` VARCHAR(10) NOT NULL DEFAULT 'POST' COMMENT '请求方法: GET/POST',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MCP服务表';

-- =============================================================================
-- 四、MCP服务调用日志表
-- =============================================================================
DROP TABLE IF EXISTS `mcp_service_log`;

CREATE TABLE IF NOT EXISTS `mcp_service_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `service_id` BIGINT NOT NULL COMMENT '服务ID',
    `request_data` TEXT COMMENT '请求数据',
    `response_data` TEXT COMMENT '响应数据',
    `latency` INT COMMENT '响应延迟（毫秒）',
    `status` VARCHAR(20) COMMENT '状态: success-成功 failed-失败',
    `error_message` TEXT COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MCP服务调用日志表';

-- =============================================================================
-- 五、对话评估结果表
-- =============================================================================
DROP TABLE IF EXISTS `conversation_evaluation`;

CREATE TABLE IF NOT EXISTS `conversation_evaluation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评估ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `message_id` BIGINT COMMENT '消息ID',
    `model` VARCHAR(50) NOT NULL COMMENT '模型标识',
    `score` INT COMMENT '评分: 0-100',
    `evaluation` JSON COMMENT '详细评估结果',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_model` (`model`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对话评估结果表';

-- =============================================================================
-- 六、初始化数据
-- =============================================================================

-- 初始化默认提示词模板
INSERT INTO `prompt_template` (`name`, `content`, `variables`, `description`, `is_default`, `is_system`, `sort`, `created_by`) VALUES
(
    '默认模板',
    '你是公安专网知识库的智能助手，专注于法律法规和业务规范的精准解答。请根据参考文档，准确、完整地回答用户问题。

## 用户问题
{{question}}

## 回答要求
1. **准确性**: 严格基于参考文档内容回答，不要添加文档中没有的信息
2. **完整性**: 如果涉及多条法规或多项规定，请逐一列出
3. **清晰性**: 使用清晰的标题和小标题组织内容，重要条款用**加粗**强调
4. **引用**: 在回答末尾明确标注引用来源
5. **诚实**: 如果参考文档中没有明确答案，请明确告知',
    '["{{question}}", "{{kb_name}}", "{{user_name}}", "{{current_time}}"]',
    '公安知识库默认提示词模板',
    1,
    1,
    0,
    1
);

-- 初始化示例MCP服务（可根据实际需要启用）
INSERT INTO `mcp_service` (`name`, `code`, `description`, `enabled`, `config_url`, `config_auth_type`, `config_timeout`, `config_method`, `sort`, `created_by`) VALUES
(
    'Jina搜索',
    'jina_search',
    'Jina AI提供的网络搜索服务',
    0,
    'https://api.jina.ai/search',
    'api_key',
    30,
    'POST',
    0,
    1
);

-- =============================================================================
-- 六、快捷指令表（本次新增）
-- =============================================================================
DROP TABLE IF EXISTS `quick_command`;

CREATE TABLE IF NOT EXISTS `quick_command` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指令ID',
    `name` VARCHAR(100) NOT NULL COMMENT '指令名称',
    `command` VARCHAR(500) NOT NULL COMMENT '指令内容（支持变量替换）',
    `description` VARCHAR(500) COMMENT '指令描述',
    `icon` VARCHAR(100) COMMENT '图标',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '颜色',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统指令: 0-否 1-是（系统指令不可删除）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `created_by` BIGINT COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_sort` (`sort`),
    KEY `idx_usage_count` (`usage_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='快捷指令表';

-- =============================================================================
-- 六、初始化快捷指令数据（本次新增）
-- =============================================================================

INSERT INTO `quick_command` (`name`, `command`, `description`, `icon`, `color`, `is_system`, `sort`, `created_by`) VALUES
(
    '解释这个概念',
    '请详细解释以下概念：\n{{selected_text}}\n要求：\n1. 提供准确的定义\n2. 说明适用范围\n3. 给出具体例子',
    '解释选中的概念或术语',
    'InfoFilled',
    '#909399',
    1,
    1,
    1
),
(
    '总结这段话',
    '请总结以下内容：\n{{selected_text}}\n要求：\n1. 提取核心要点\n2. 保持逻辑完整\n3. 控制在100字以内',
    '总结选中的文本内容',
    'Document',
    '#67C23A',
    1,
    2,
    1
),
(
    '翻译成英文',
    '请将以下内容翻译成英文：\n{{selected_text}}\n要求：\n1. 翻译准确\n2. 保持专业术语的一致性',
    '将选中内容翻译成英文',
    'Language',
    '#E6A23C',
    1,
    3,
    1
),
(
    '翻译成中文',
    '请将以下内容翻译成中文：\n{{selected_text}}\n要求：\n1. 翻译准确\n2. 符合中文表达习惯',
    '将选中内容翻译成中文',
    'ChatLineRound',
    '#409EFF',
    1,
    4,
    1
),
(
    '查找相关法规',
    '请查找与以下内容相关的法律法规：\n{{selected_text}}\n要求：\n1. 列出相关法规名称\n2. 说明具体条款',
    '查找相关法律法规',
    'Search',
    '#F56C6C',
    1,
    5,
    1
),
(
    '案例分析',
    '请对以下案例进行分析：\n{{selected_text}}\n要求：\n1. 案情概述\n2. 法律适用\n3. 处理结果',
    '分析案例并给出法律适用建议',
    'DataAnalysis',
    '#909399',
    1,
    6,
    1
);

-- =============================================================================
-- 七、创建索引（性能优化）
-- =============================================================================

-- prompt_template索引已在上方创建

-- conversation_settings索引已在上方创建

-- mcp_service索引已在上方创建

-- mcp_service_log索引已在上方创建

-- conversation_evaluation索引已在上方创建

-- quick_command索引已在上方创建

-- =============================================================================
-- 八、添加外键约束（如需要）
-- =============================================================================

-- 如果需要添加外键约束，请取消以下注释
-- ALTER TABLE `conversation_settings` ADD CONSTRAINT `fk_cs_template` FOREIGN KEY (`template_id`) REFERENCES `prompt_template`(`id`);
-- ALTER TABLE `mcp_service_log` ADD CONSTRAINT `fk_msl_service` FOREIGN KEY (`service_id`) REFERENCES `mcp_service`(`id`);

-- =============================================================================
-- END
-- =============================================================================
