-- 文档表 kb_path 字段数据迁移
-- 说明：为现有文档数据填充 kb_path 字段
-- 执行时间：2026-02-02

-- 场景1：如果知识库是二级结构（parent_id > 0）
UPDATE document d
INNER JOIN sys_dict child ON d.kb_id = child.id
INNER JOIN sys_dict parent ON child.parent_id = parent.id
SET d.kb_path = CONCAT(parent.id, ',', child.id)
WHERE d.deleted = 0 AND child.parent_id > 0;

-- 场景2：如果知识库是一级结构（parent_id = 0 或 null）
UPDATE document d
INNER JOIN sys_dict dict ON d.kb_id = dict.id
SET d.kb_path = CONCAT(dict.id)
WHERE d.deleted = 0
  AND (dict.parent_id = 0 OR dict.parent_id IS NULL)
  AND d.kb_path IS NULL;

-- 验证迁移结果
SELECT id, kb_id, kb_path, title FROM document WHERE kb_id IS NOT NULL LIMIT 10;

