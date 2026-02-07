-- 添加 model_id 字段到 chat_message 表
-- 执行时间: 2026-02-04

-- 检查字段是否已存在
SELECT COUNT(*) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'chat_message'
  AND COLUMN_NAME = 'model_id';

-- 如果字段不存在则添加
ALTER TABLE `chat_message`
ADD COLUMN `model_id` varchar(100) NULL COMMENT '模型ID' AFTER `deleted`;

-- 为 model_id 添加索引（可选，加速查询）
ALTER TABLE `chat_message`
ADD INDEX `idx_model_id` (`model_id`);

-- 验证修改
DESCRIBE `chat_message`;
