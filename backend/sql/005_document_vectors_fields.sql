-- ===============================
-- Document_Vectors表字段变更脚本
-- 文件: 005_document_vectors_fields.sql
-- 说明: 为document_vectors表新增归属地和来源部门字段
-- 日期: 2026-01-31
-- ===============================

-- ----------------------------
-- 1. 新增origin_scope字段（归属地）
-- ----------------------------
ALTER TABLE document_vectors 
ADD COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地' AFTER kb_id;

-- ----------------------------
-- 2. 新增origin_department字段（来源部门）
-- ----------------------------
ALTER TABLE document_vectors 
ADD COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门' AFTER origin_scope;

-- ----------------------------
-- 3. 创建索引
-- ----------------------------
CREATE INDEX idx_vec_origin_scope ON document_vectors(origin_scope);

CREATE INDEX idx_vec_origin_department ON document_vectors(origin_department);

-- ----------------------------
-- 4. 添加注释
-- ----------------------------
ALTER TABLE document_vectors 
MODIFY COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地: national-国家级, ministerial-部级, provincial-省级, municipal-市级, county-县级, unit-单位级';

ALTER TABLE document_vectors 
MODIFY COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门(如: 治安管理支队-二大队)';

-- ----------------------------
-- 5. 验证字段
-- ----------------------------
DESCRIBE document_vectors;

-- ----------------------------
-- 6. 查询示例
-- ----------------------------
SELECT id, document_id, kb_id, origin_scope, origin_department, title 
FROM document_vectors 
WHERE deleted = 0 
LIMIT 10;

-- ----------------------------
-- 7. 更新现有向量的元数据（可选）
-- 从document表同步到document_vectors表
-- 仅更新已向量化但元数据为空的记录
-- ----------------------------
UPDATE document_vectors dv
INNER JOIN document d ON dv.document_id = d.id
SET dv.origin_scope = d.origin_scope,
    dv.origin_department = d.origin_department
WHERE dv.deleted = 0
  AND (dv.origin_scope IS NULL OR dv.origin_scope = '')
  AND d.origin_scope IS NOT NULL AND d.origin_scope != '';

-- ----------------------------
-- 8. 同步验证
-- ----------------------------
SELECT 
  '更新前' AS 状态,
  COUNT(*) AS 数量
FROM document_vectors 
WHERE deleted = 0 
  AND (origin_scope IS NULL OR origin_scope = '');

SELECT 
  '更新后' AS 状态,
  COUNT(*) AS 数量
FROM document_vectors 
WHERE deleted = 0 
  AND origin_scope IS NOT NULL AND origin_scope != '';

SELECT 'Document_Vectors表字段变更完成' AS result;
