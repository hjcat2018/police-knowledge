-- ===============================
-- 字典数据初始化脚本
-- 文件: 003_sys_dict_init.sql
-- 说明: 初始化归属地、来源部门、知识库分类字典数据
-- 日期: 2026-01-31
-- ===============================

-- ----------------------------
-- 1. 归属地字典（origin_scope）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('origin_scope', 'national', '国家级', 0, NULL, 1, 1, 1),
('origin_scope', 'ministerial', '部级', 0, NULL, 1, 2, 1),
('origin_scope', 'provincial', '省级', 0, NULL, 1, 3, 1),
('origin_scope', 'municipal', '市级', 0, NULL, 1, 4, 1),
('origin_scope', 'county', '县级', 0, NULL, 1, 5, 1),
('origin_scope', 'unit', '单位级', 0, NULL, 1, 6, 1);

-- ----------------------------
-- 2. 来源部门字典（origin_department）- 一级部门
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('origin_department', 'zazhi', '治安管理支队', 0, NULL, 1, 1, 1),
('origin_department', 'xingzheng', '刑侦支队', 0, NULL, 1, 2, 1),
('origin_department', 'jingji', '经侦支队', 0, NULL, 1, 3, 1),
('origin_department', 'jiaojing', '交警支队', 0, NULL, 1, 4, 1),
('origin_department', 'jidu', '禁毒支队', 0, NULL, 1, 5, 1),
('origin_department', 'chukujing', '出入境管理支队', 0, NULL, 1, 6, 1),
('origin_department', 'fazhi', '法制支队', 0, NULL, 1, 7, 1),
('origin_department', 'zhihui', '指挥中心', 0, NULL, 1, 8, 1),
('origin_department', 'qinshi', '勤务支撑', 0, NULL, 1, 9, 1),
('origin_department', 'xunlian', '训练支队', 0, NULL, 1, 10, 1);

-- ----------------------------
-- 3. 来源部门字典 - 二级部门（治安管理支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_1', '一大队', id, 'zazhi', 2, 1, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_2', '二大队', id, 'zazhi', 2, 2, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_3', '三大队', id, 'zazhi', 2, 3, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_4', '四大队', id, 'zazhi', 2, 4, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'zazhi_5', '五大队', id, 'zazhi', 2, 5, 1 FROM sys_dict WHERE code = 'zazhi' AND kind = 'origin_department';

-- ----------------------------
-- 4. 来源部门字典 - 二级部门（刑侦支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_1', '一大队', id, 'xingzheng', 2, 1, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_2', '二大队', id, 'xingzheng', 2, 2, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_3', '三大队', id, 'xingzheng', 2, 3, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'xingzheng_6', '六大队', id, 'xingzheng', 2, 6, 1 FROM sys_dict WHERE code = 'xingzheng' AND kind = 'origin_department';

-- ----------------------------
-- 5. 来源部门字典 - 二级部门（经侦支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'jingji_1', '一大队', id, 'jingji', 2, 1, 1 FROM sys_dict WHERE code = 'jingji' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'jingji_2', '二大队', id, 'jingji', 2, 2, 1 FROM sys_dict WHERE code = 'jingji' AND kind = 'origin_department';

-- ----------------------------
-- 6. 来源部门字典 - 二级部门（交警支队）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'jiaojing_1', '事故处理大队', id, 'jiaojing', 2, 1, 1 FROM sys_dict WHERE code = 'jiaojing' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'jiaojing_2', '秩序管理大队', id, 'jiaojing', 2, 2, 1 FROM sys_dict WHERE code = 'jiaojing' AND kind = 'origin_department';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'origin_department', 'jiaojing_3', '车管所', id, 'jiaojing', 2, 3, 1 FROM sys_dict WHERE code = 'jiaojing' AND kind = 'origin_department';

-- ----------------------------
-- 7. 知识库分类字典（kb_category）- 一级分类
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status) VALUES
('kb_category', 'falvfagui', '法律法规', 0, NULL, 1, 1, 1),
('kb_category', 'yewuzhinan', '业务指引', 0, NULL, 1, 2, 1),
('kb_category', 'anjianchuli', '案件处理', 0, NULL, 1, 3, 1),
('kb_category', 'gongzuozhidao', '工作指导', 0, NULL, 1, 4, 1),
('kb_category', 'peixunjiaocai', '培训教材', 0, NULL, 1, 5, 1),
('kb_category', 'anquanzhishi', '安全知识', 0, NULL, 1, 6, 1);

-- ----------------------------
-- 8. 知识库分类字典 - 二级分类（法律法规）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_xingfa', '刑法', id, 'falvfagui', 2, 1, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_minshi', '民法', id, 'falvfagui', 2, 2, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_xingzheng', '行政法', id, 'falvfagui', 2, 3, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_xingshi', '刑事诉讼法', id, 'falvfagui', 2, 4, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'falvfagui_minshi_susong', '民事诉讼法', id, 'falvfagui', 2, 5, 1 FROM sys_dict WHERE code = 'falvfagui' AND kind = 'kb_category';

-- ----------------------------
-- 9. 知识库分类字典 - 二级分类（业务指引）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'yewuzhinan_bumen', '部门规定', id, 'yewuzhinan', 2, 1, 1 FROM sys_dict WHERE code = 'yewuzhinan' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'yewuzhinan_dashang', '上级指引', id, 'yewuzhinan', 2, 2, 1 FROM sys_dict WHERE code = 'yewuzhinan' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'yewuzhinan_diqu', '地区规定', id, 'yewuzhinan', 2, 3, 1 FROM sys_dict WHERE code = 'yewuzhinan' AND kind = 'kb_category';

-- ----------------------------
-- 10. 知识库分类字典 - 二级分类（案件处理）
-- ----------------------------
INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_anjian', '案件办理', id, 'anjianchuli', 2, 1, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_chengxu', '程序规定', id, 'anjianchuli', 2, 2, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_zhengju', '证据规则', id, 'anjianchuli', 2, 3, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

INSERT INTO sys_dict (kind, code, detail, parent_id, parent_code, level, sort, status)
SELECT 'kb_category', 'anjianchuli_pezui', '批捕起诉', id, 'anjianchuli', 2, 4, 1 FROM sys_dict WHERE code = 'anjianchuli' AND kind = 'kb_category';

-- ----------------------------
-- 11. 验证数据
-- ----------------------------
SELECT 
  kind AS 字典类型,
  COUNT(*) AS 数量
FROM sys_dict 
WHERE deleted = 0 
GROUP BY kind 
ORDER BY kind;

-- ----------------------------
-- 12. 详细查询
-- ----------------------------
SELECT 
  kind AS 类型,
  code AS 编码,
  detail AS 名称,
  parent_code AS 父编码,
  level AS 层级,
  sort AS 排序
FROM sys_dict 
WHERE deleted = 0 
ORDER BY kind, level, sort;

SELECT '字典数据初始化完成' AS result;
