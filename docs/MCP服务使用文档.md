# MCP 服务功能使用文档

## 1. 功能概述

MCP（Model Context Protocol）服务是一种大模型上下文协议服务，允许大模型与外部工具和数据源进行交互。本系统实现了MCP服务的管理功能，支持HTTP调用模式和进程启动模式两种配置方式。

### 核心功能

- **服务管理**：添加、编辑、删除MCP服务
- **状态控制**：启用/禁用服务
- **认证配置**：支持API Key、Bearer Token、OAuth2三种认证方式
- **两种模式**：HTTP调用模式（调用远程API）和进程启动模式（本地启动服务）
- **对话集成**：在普通对话模式下选择启用的MCP服务

### 使用场景

- 文件解析服务（PDF、Word、Excel等）
- 法规查询服务（公安相关法规条文）
- 案例检索服务（检索相关案例依据）
- 任何需要大模型调用外部工具的场景

## 2. 数据库配置

### 2.1 表结构

系统使用现有的 `mcp_service` 表，结构如下：

```sql
CREATE TABLE `mcp_service` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` varchar(100) NOT NULL COMMENT '服务名称',
  `code` varchar(50) NOT NULL COMMENT '服务编码',
  `description` varchar(500) DEFAULT NULL COMMENT '服务描述',
  `enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否启用: 0-禁用 1-启用',
  `config_url` varchar(500) DEFAULT NULL COMMENT 'API URL（HTTP模式使用）',
  `config_auth_type` varchar(20) NOT NULL DEFAULT 'api_key' COMMENT '认证方式: api_key-API Key bearer-Bearer Token oauth2-OAuth2',
  `config_credentials` varchar(500) DEFAULT NULL COMMENT '认证凭证',
  `config_timeout` int(11) NOT NULL DEFAULT '60' COMMENT '超时时间（秒）',
  `config_method` varchar(10) NOT NULL DEFAULT 'POST' COMMENT '请求方法: GET/POST',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `command` varchar(500) DEFAULT NULL COMMENT '启动命令（进程模式使用）',
  `args` text DEFAULT NULL COMMENT '命令参数',
  `env` text DEFAULT NULL COMMENT '环境变量',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '删除标记: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP服务表';
```

### 2.2 初始化示例数据

```sql
-- 示例数据
INSERT INTO `mcp_service` (`name`, `code`, `description`, `enabled`, `config_url`, `config_auth_type`, `config_timeout`, `config_method`, `sort`) VALUES
('文件解析服务', 'file_parser', '解析PDF、Word、Excel等文件', 1, 'http://localhost:9000/mcp/file-parser', 'api_key', 60, 'POST', 1),
('法规查询服务', 'law_search', '查询公安相关法规条文', 1, 'http://localhost:9000/mcp/law-search', 'api_key', 60, 'POST', 2),
('案例检索服务', 'case_search', '检索相关案例依据', 0, 'http://localhost:9000/mcp/case-search', 'api_key', 60, 'POST', 3);
```

## 3. 后端API接口

### 3.1 接口列表

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/v1/mcp/services` | 获取所有启用的MCP服务列表 | 否 |
| GET | `/v1/mcp/services/{id}` | 获取MCP服务详情 | 否 |
| POST | `/v1/mcp/services` | 新增MCP服务 | 是 |
| PUT | `/v1/mcp/services/{id}` | 更新MCP服务 | 是 |
| DELETE | `/v1/mcp/services/{id}` | 删除MCP服务 | 是 |
| PUT | `/v1/mcp/services/{id}/toggle` | 切换MCP服务状态 | 是 |

### 3.2 接口详情

#### 3.2.1 获取服务列表

**GET** `/v1/mcp/services`

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1000001,
      "name": "文件解析服务",
      "code": "file_parser",
      "description": "解析PDF、Word、Excel等文件",
      "enabled": 1,
      "configUrl": "http://localhost:9000/mcp/file-parser",
      "configAuthType": "api_key",
      "configTimeout": 60,
      "configMethod": "POST",
      "sort": 1,
      "command": null,
      "args": null,
      "env": null,
      "createdTime": "2026-02-06T10:00:00",
      "updatedTime": "2026-02-06T10:00:00"
    }
  ]
}
```

#### 3.2.2 获取服务详情

**GET** `/v1/mcp/services/{id}`

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1000001,
    "name": "文件解析服务",
    "code": "file_parser",
    "description": "解析PDF、Word、Excel等文件",
    "enabled": 1,
    "configUrl": "http://localhost:9000/mcp/file-parser",
    "configAuthType": "api_key",
    "configCredentials": "your-api-key-here",
    "configTimeout": 60,
    "configMethod": "POST",
    "sort": 1,
    "command": null,
    "args": null,
    "env": null,
    "createdTime": "2026-02-06T10:00:00",
    "updatedTime": "2026-02-06T10:00:00"
  }
}
```

#### 3.2.3 新增服务

**POST** `/v1/mcp/services`

**请求体**：

```json
{
  "name": "文件解析服务",
  "code": "file_parser",
  "description": "解析PDF、Word、Excel等文件",
  "enabled": 1,
  "configUrl": "http://localhost:9000/mcp/file-parser",
  "configAuthType": "api_key",
  "configCredentials": "your-api-key-here",
  "configTimeout": 60,
  "configMethod": "POST",
  "sort": 1,
  "command": null,
  "args": null,
  "env": null
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 服务名称 |
| code | String | 是 | 服务编码（唯一） |
| description | String | 否 | 服务描述 |
| enabled | Integer | 否 | 是否启用（0-禁用 1-启用），默认1 |
| configUrl | String | 否 | API URL（HTTP模式使用） |
| configAuthType | String | 否 | 认证方式（api_key/bearer/oauth2），默认api_key |
| configCredentials | String | 否 | 认证凭证 |
| configTimeout | Integer | 否 | 超时时间（秒），默认60 |
| configMethod | String | 否 | 请求方法（GET/POST），默认POST |
| sort | Integer | 否 | 排序，默认0 |
| command | String | 否 | 启动命令（进程模式使用） |
| args | String | 否 | 命令参数 |
| env | String | 否 | 环境变量 |

**HTTP模式 vs 进程模式**：

- **HTTP模式**：设置 `configUrl`、`configAuthType`、`configCredentials`、`configMethod`
- **进程模式**：设置 `command`、`args`、`env`

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": 1000002
}
```

#### 3.2.4 更新服务

**PUT** `/v1/mcp/services/{id}`

**请求体**：同新增服务

#### 3.2.5 删除服务

**DELETE** `/v1/mcp/services/{id}`

**说明**：逻辑删除，将 `deleted` 字段置为1

#### 3.2.6 切换状态

**PUT** `/v1/mcp/services/{id}/toggle`

**说明**：切换服务的启用/禁用状态

## 4. 前端使用说明

### 4.1 打开MCP服务面板

在普通对话模式下：

1. 点击页面顶部的 **"MCP服务"** 按钮
2. 左侧面板会显示MCP服务列表

### 4.2 选择MCP服务

1. 在MCP服务面板中，显示所有启用的服务
2. **在线**状态（绿色标签）：服务已启用，可以选择
3. **离线**状态（灰色标签）：服务已禁用，不可以选择
4. 点击服务可以选中/取消选中
5. 选中的服务会显示勾选图标

### 4.3 使用选中的MCP服务

1. 选中需要的MCP服务
2. 在对话框中输入问题
3. 发送问题后，系统会自动调用选中的MCP服务
4. MCP服务的返回结果会作为上下文，帮助大模型回答问题

### 4.4 示例场景

#### 场景一：法规查询

1. 在MCP面板中选中 **"法规查询服务"**
2. 输入："查找关于盗窃罪的量刑标准"
3. 系统调用法规查询服务，获取相关法规条文
4. 大模型根据法规条文生成回答

#### 场景二：案例检索

1. 在MCP面板中选中 **"案例检索服务"**
2. 输入："查找过失致人重伤的典型案例"
3. 系统调用案例检索服务，获取相关案例
4. 大模型根据案例生成回答

#### 场景三：组合使用

可以同时选中多个MCP服务：

1. 同时选中 **"法规查询服务"** 和 **"案例检索服务"**
2. 输入："关于故意伤害罪的量刑标准和典型案例"
3. 系统同时调用两个服务，获取法规和案例
4. 大模型综合回答

## 5. 配置说明

### 5.1 HTTP模式配置

适用于调用远程MCP服务器：

```json
{
  "name": "法规查询服务",
  "code": "law_search",
  "description": "查询公安相关法规条文",
  "enabled": 1,
  "configUrl": "http://192.168.1.100:9000/mcp/law-search",
  "configAuthType": "api_key",
  "configCredentials": "sk-xxxxxxxxxxxxx",
  "configTimeout": 30,
  "configMethod": "POST",
  "sort": 1
}
```

### 5.2 进程模式配置

适用于本地启动的MCP服务：

```json
{
  "name": "本地文件解析服务",
  "code": "local_file_parser",
  "description": "本地文件解析服务",
  "enabled": 1,
  "command": "/usr/bin/python3",
  "args": "/opt/mcp/file-parser/server.py",
  "env": "MCP_API_KEY=your-key\nMCP_TIMEOUT=60",
  "sort": 1
}
```

### 5.3 认证方式说明

| 认证方式 | 说明 | 适用场景 |
|----------|------|----------|
| api_key | API Key认证 | 最常用的方式，如OpenAI API |
| bearer | Bearer Token认证 | OAuth2.0的access_token |
| oauth2 | OAuth2.0认证 | 企业级API认证 |

## 6. 最佳实践

### 6.1 服务命名规范

- 使用有意义的名称，如："法规查询服务"、"案例检索服务"
- 编码使用小写字母和下划线，如：`law_search`

### 6.2 启用/禁用策略

- 新添加的服务默认启用
- 需要维护的服务可以临时禁用（enabled=0）
- 禁用的服务不会出现在MCP服务面板中

### 6.3 超时时间配置

- 根据服务的实际响应时间设置
- 文件解析等服务建议设置60秒以上
- 简单查询服务可以设置30秒

### 6.4 排序使用

- 使用 `sort` 字段控制服务在面板中的显示顺序
- 数字越小越靠前
- 常用服务设置较小的sort值

## 7. 常见问题

### Q1: MCP服务不显示？

**可能原因**：
1. 服务未启用（enabled != 1）
2. 服务已被删除（deleted == 1）
3. 数据库连接异常
4. 前端缓存问题

**解决方案**：
1. 检查数据库中的 `enabled` 字段是否为1
2. 刷新页面或清除浏览器缓存
3. 检查后端服务是否正常运行

### Q2: 调用MCP服务失败？

**可能原因**：
1. `configUrl` 配置错误
2. 认证凭证过期或错误
3. MCP服务未启动
4. 网络连接问题

**解决方案**：
1. 检查 `configUrl` 是否可以正常访问
2. 检查认证凭证是否正确
3. 检查MCP服务是否正常运行
4. 检查超时时间是否设置合理

### Q3: 如何添加新的MCP服务？

**步骤**：
1. 确保MCP服务已经部署并可以正常访问
2. 登录系统管理员账户
3. 调用POST `/v1/mcp/services` 接口添加服务
4. 在普通对话模式下选中该服务
5. 测试调用是否正常

### Q4: 可以同时使用多少个MCP服务？

**建议**：同时选中的MCP服务不超过3个

**原因**：
- 每个服务都会增加响应时间
- 过多的服务可能导致超时
- 大模型的上下文长度有限制

### Q5: MCP服务返回的结果格式有要求吗？

**建议返回格式**：

```json
{
  "success": true,
  "data": {
    "result": "查询结果内容"
  },
  "error": null
}
```

**错误返回格式**：

```json
{
  "success": false,
  "data": null,
  "error": "错误信息"
}
```

## 8. 集成到对话流程

### 8.1 当前集成方式

当前版本中，MCP服务管理功能已实现，但与对话流程的深度集成需要进一步开发：

1. **服务列表获取**：前端通过API获取启用的MCP服务列表
2. **服务选择**：用户可以在MCP面板中选择要使用的服务
3. **后续集成**：将用户选择的服务ID传递给后端，后端在处理对话时调用对应的MCP服务

### 8.2 待集成功能

以下功能需要在后续版本中实现：

1. **后端对话集成**：在 `StreamChatController` 中集成MCP服务调用
2. **工具自动调用**：大模型根据对话内容自动判断需要调用哪些MCP服务
3. **结果处理**：将MCP服务返回的结果格式化为大模型可理解的格式
4. **流式输出**：将MCP服务调用结果流式返回给前端

## 9. API调用示例

### 9.1 cURL示例

#### 获取服务列表

```bash
curl -X GET "http://localhost:8080/v1/mcp/services" \
  -H "Authorization: Bearer <token>"
```

#### 新增服务

```bash
curl -X POST "http://localhost:8080/v1/mcp/services" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "法规查询服务",
    "code": "law_search",
    "description": "查询公安相关法规条文",
    "enabled": 1,
    "configUrl": "http://localhost:9000/mcp/law-search",
    "configAuthType": "api_key",
    "configCredentials": "sk-xxxxx",
    "configTimeout": 60,
    "configMethod": "POST",
    "sort": 1
  }'
```

#### 更新服务

```bash
curl -X PUT "http://localhost:8080/v1/mcp/services/1000001" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "法规查询服务（更新）",
    "description": "更新后的描述"
  }'
```

#### 删除服务

```bash
curl -X DELETE "http://localhost:8080/v1/mcp/services/1000001" \
  -H "Authorization: Bearer <token>"
```

#### 切换状态

```bash
curl -X PUT "http://localhost:8080/v1/mcp/services/1000001/toggle" \
  -H "Authorization: Bearer <token>"
```

### 9.2 前端调用示例

```typescript
import { getMcpServices, createMcpService, toggleMcpService } from '@/api/mcpService'

// 获取服务列表
const loadServices = async () => {
  const res = await getMcpServices()
  console.log(res.data)
}

// 添加服务
const addService = async () => {
  await createMcpService({
    name: '法规查询服务',
    code: 'law_search',
    description: '查询公安相关法规条文',
    enabled: 1,
    configUrl: 'http://localhost:9000/mcp/law-search',
    configAuthType: 'api_key',
    configCredentials: 'sk-xxxxx'
  })
}

// 切换状态
const toggle = async (id: number) => {
  await toggleMcpService(id)
}
```

## 10. 版本信息

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0.0 | 2026-02-06 | 初始版本，实现MCP服务管理功能 |

---

**文档编制日期**：2026-02-06
**适用版本**：Police KB System v1.0+
