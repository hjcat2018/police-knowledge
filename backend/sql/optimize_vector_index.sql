-- ================================================
-- SeekDB document_vectors 核心索引优化脚本
-- 执行时间: 2026-01-25
-- 说明: 向量检索和混合搜索必需的索引优化
-- ================================================

-- ================================================
-- 第1步: 添加 kb_id 列（必须）
-- 原因: 当前表没有 kb_id，无法按知识库筛选
-- ================================================
ALTER TABLE document_vectors
ADD COLUMN kb_id bigint(20) DEFAULT NULL COMMENT '知识库ID' AFTER document_id;

-- 根据 metadata 更新 kb_id
UPDATE document_vectors
SET kb_id = CAST(JSON_EXTRACT(metadata, '$.kb_id') AS UNSIGNED)
WHERE metadata IS NOT NULL AND JSON_CONTAINS_PATH(metadata, 'one', '$.kb_id');

-- 添加索引
ALTER TABLE document_vectors
ADD KEY idx_kb_id (kb_id) BLOCK_SIZE 16384 LOCAL;

-- ================================================
-- 第2步: 创建向量索引（必须）
-- 原因: 没有向量索引，向量检索会全表扫描
-- ================================================
ALTER TABLE document_vectors
ADD VECTOR INDEX idx_vector (vector)
WITH (distance=l2, type=hnsw, m=16, efConstruction=200, efSearch=100);

-- ================================================
-- 第3步: 创建全文索引（推荐）
-- 原因: 关键词搜索需要，否则只能用 LIKE %xxx% 低效查询
-- ================================================
ALTER TABLE document_vectors
ADD FULLTEXT INDEX idx_content (content);
ALTER TABLE document_vectors
ADD FULLTEXT INDEX idx_title (title);

-- ================================================
-- 验证索引创建结果
-- ================================================
SHOW INDEX FROM document_vectors;

-- ================================================
-- 确认 kb_id 数据已同步
-- ================================================
SELECT
    COUNT(*) AS total,
    COUNT(kb_id) AS with_kb_id,
    COUNT(*) - COUNT(kb_id) AS without_kb_id
FROM document_vectors;
