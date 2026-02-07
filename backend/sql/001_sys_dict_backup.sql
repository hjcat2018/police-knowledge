-- ===============================
-- 字典表备份脚本
-- 文件: 001_sys_dict_backup.sql
-- 说明: 备份现有sys_dict表
-- 日期: 2026-01-31
-- ===============================

-- 重命名原表为备份表
RENAME TABLE sys_dict TO sys_dict_backup;

-- 验证备份
SELECT COUNT(*) AS backup_count FROM sys_dict_backup;

-- 备份完成提示
SELECT '字典表备份完成，原表已重命名为 sys_dict_backup' AS result;
