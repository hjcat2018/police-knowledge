-- MCP 服务表示例数据（适配现有表结构）
-- 表已存在，请直接执行 INSERT 语句

INSERT INTO `mcp_service` (`name`, `code`, `description`, `enabled`, `config_url`, `config_auth_type`, `config_timeout`, `config_method`, `sort`) VALUES
('文件解析服务', 'file_parser', '解析PDF、Word、Excel等文件', 1, 'http://localhost:9000/mcp/file-parser', 'api_key', 60, 'POST', 1),
('法规查询服务', 'law_search', '查询公安相关法规条文', 1, 'http://localhost:9000/mcp/law-search', 'api_key', 60, 'POST', 2),
('案例检索服务', 'case_search', '检索相关案例依据', 0, 'http://localhost:9000/mcp/case-search', 'api_key', 60, 'POST', 3);
