-- ===============================
-- Document表字段变更脚本
-- 文件: 004_document_scope_fields.sql
-- 说明: 为document表新增归属地和来源部门字段
-- 日期: 2026-01-31
-- ===============================

-- ----------------------------
-- 1. 新增origin_scope字段（归属地）
-- ----------------------------
ALTER TABLE document 
ADD COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地: national-国家级, ministerial-部级, provincial-省级, municipal-市级, county-县级, unit-单位级' AFTER source;

-- ----------------------------
-- 2. 新增origin_department字段（来源部门）
-- ----------------------------
ALTER TABLE document 
ADD COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门(如: 治安管理支队-二大队)' AFTER origin_scope;

-- ----------------------------
-- 3. 创建索引
-- ----------------------------
CREATE INDEX idx_doc_origin_scope ON document(origin_scope);

CREATE INDEX idx_doc_origin_department ON document(origin_department);

-- ----------------------------
-- 4. 添加注释
-- ----------------------------
ALTER TABLE document 
MODIFY COLUMN origin_scope VARCHAR(20) NULL COMMENT '归属地: national-国家级, ministerial-部级, provincial-省级, municipal-市级, county-县级, unit-单位级';

ALTER TABLE document 
MODIFY COLUMN origin_department VARCHAR(200) NULL COMMENT '来源部门(如: 治安管理支队-二大队)';

-- ----------------------------
-- 5. 验证字段
-- ----------------------------
DESCRIBE document;

-- ----------------------------
-- 6. 查询示例
-- ----------------------------
SELECT id, title, origin_scope, origin_department, source 
FROM document 
WHERE deleted = 0 
LIMIT 10;

SELECT 'Document表字段变更完成' AS result;
