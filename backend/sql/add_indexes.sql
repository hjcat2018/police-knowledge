-- 为document_vectors表添加FULLTEXT索引和VECTOR索引
USE police_kb;

-- 添加content字段的FULLTEXT索引
ALTER TABLE document_vectors ADD FULLTEXT INDEX idx_fulltext_content (content);

-- 添加title字段的FULLTEXT索引
ALTER TABLE document_vectors ADD FULLTEXT INDEX idx_fulltext_title (title);

-- 验证索引是否创建成功
SHOW INDEX FROM document_vectors;