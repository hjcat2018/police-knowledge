-- 添加对话模式字段，支持专业模式和普通模式历史记录分离
-- 执行时间: 2026-02-03

-- 1. 添加 mode 字段，默认值为 'normal'
ALTER TABLE conversation ADD COLUMN mode VARCHAR(20) DEFAULT 'normal' COMMENT '对话模式: professional-专业模式, normal-普通模式';

-- 2. 为已有数据设置默认值
UPDATE conversation SET mode = 'professional' WHERE mode IS NULL;

-- 3. 创建索引优化查询性能
CREATE INDEX idx_conversation_mode ON conversation(mode);

-- 4. 验证数据
SELECT id, title, mode, created_time, updated_time
FROM conversation
ORDER BY updated_time DESC
LIMIT 10;
