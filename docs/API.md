# 知识库系统 API 接口文档

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **技术栈版本**: Spring AI 1.1.2 | Spring AI Alibaba 1.1.2.0 | Spring Boot 3.5.7

## 一、概述

本文档描述了知识库系统的RESTful API接口规范。所有API均采用JSON格式进行请求和响应。

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api/v1` |
| 认证方式 | Bearer Token |
| 响应格式 | JSON |
| 编码格式 | UTF-8 |
| 可观测性 | 支持链路追踪 (traceId) |

### 1.2 通用响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": { ... },
    "traceId": "abc123def456"  // 链路追踪ID (新增)
}
```

### 1.3 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 (限流) |

---

## 二、知识库管理 API

### 2.1 创建知识库

**接口路径**: `POST /api/v1/knowledge-bases`

**请求参数**:

```json
{
    "name": "技术文档知识库",
    "description": "公司技术文档统一管理",
    "settings": {
        "embeddingModel": "text-embedding-v2",
        "chunkSize": 1000,
        "chunkOverlap": 200
    }
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 知识库名称，最大200字符 |
| description | String | 否 | 知识库描述 |
| settings | Object | 否 | 知识库配置 |
| settings.embeddingModel | String | 否 | 嵌入模型名称 |
| settings.chunkSize | Integer | 否 | 分块大小，默认1000 |
| settings.chunkOverlap | Integer | 否 | 分块重叠大小，默认200 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "id": 1,
        "name": "技术文档知识库",
        "description": "公司技术文档统一管理",
        "status": "READY",
        "documentCount": 0,
        "settings": {
            "embeddingModel": "text-embedding-v2",
            "chunkSize": 1000,
            "chunkOverlap": 200
        },
        "createdAt": "2024-01-20T10:30:00Z",
        "updatedAt": "2024-01-20T10:30:00Z"
    }
}
```

### 2.2 获取知识库列表

**接口路径**: `GET /api/v1/knowledge-bases`

**查询参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 0 | 页码，从0开始 |
| size | Integer | 否 | 20 | 每页数量，最大100 |
| sort | String | 否 | createdAt | 排序字段 |
| order | String | 否 | DESC | 排序方向：ASC/DESC |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "id": 1,
                "name": "技术文档知识库",
                "description": "公司技术文档统一管理",
                "status": "READY",
                "documentCount": 150,
                "createdAt": "2024-01-20T10:30:00Z"
            },
            {
                "id": 2,
                "name": "产品手册知识库",
                "description": "产品使用手册和FAQ",
                "status": "READY",
                "documentCount": 85,
                "createdAt": "2024-01-19T14:20:00Z"
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 20,
            "totalElements": 2,
            "totalPages": 1
        }
    }
}
```

### 2.3 获取知识库详情

**接口路径**: `GET /api/v1/knowledge-bases/{id}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 知识库ID |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "id": 1,
        "name": "技术文档知识库",
        "description": "公司技术文档统一管理",
        "status": "READY",
        "documentCount": 150,
        "chunkCount": 4500,
        "storageUsed": "2.5GB",
        "settings": {
            "embeddingModel": "text-embedding-v2",
            "chunkSize": 1000,
            "chunkOverlap": 200
        },
        "owner": {
            "id": 1001,
            "username": "admin"
        },
        "createdAt": "2024-01-20T10:30:00Z",
        "updatedAt": "2024-01-20T15:45:00Z"
    }
}
```

### 2.4 更新知识库

**接口路径**: `PUT /api/v1/knowledge-bases/{id}`

**请求参数**:

```json
{
    "name": "更新后的知识库名称",
    "description": "更新后的描述信息",
    "settings": {
        "chunkSize": 800,
        "chunkOverlap": 150
    }
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "id": 1,
        "name": "更新后的知识库名称",
        "description": "更新后的描述信息",
        "status": "READY",
        "updatedAt": "2024-01-20T16:00:00Z"
    }
}
```

### 2.5 删除知识库

**接口路径**: `DELETE /api/v1/knowledge-bases/{id}`

**响应示例**:

```json
{
    "code": 200,
    "message": "知识库删除成功"
}
```

---

## 三、文档管理 API

### 3.1 上传文档

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/documents/upload`

**Content-Type**: `multipart/form-data`

**表单参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 上传的文件，支持PDF、DOCX、MD、TXT等格式 |
| metadata | String | 否 | JSON格式的元数据 |

**metadata 示例**:

```json
{
    "category": "技术文档",
    "tags": ["Java", "Spring", "AI"],
    "author": "张三",
    "version": "1.0"
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentId": 100,
        "filename": "技术架构.pdf",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "status": "PROCESSING",
        "progress": 0,
        "chunks": 0,
        "processingJobId": "job-abc123",
        "createdAt": "2024-01-20T16:00:00Z"
    }
}
```

### 3.2 批量上传文档

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/documents/batch-upload`

**Content-Type**: `multipart/form-data`

**表单参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | List<File> | 是 | 上传的文件列表，最多50个 |
| metadata | String | 否 | 通用元数据 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "jobId": "batch-job-xyz789",
        "totalFiles": 10,
        "processedFiles": 5,
        "failedFiles": 0,
        "status": "PROCESSING",
        "createdAt": "2024-01-20T16:00:00Z"
    }
}
```

### 3.3 查询文档处理状态

**接口路径**: `GET /api/v1/documents/{documentId}/status`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentId": 100,
        "filename": "技术架构.pdf",
        "status": "COMPLETED",
        "progress": 100,
        "chunks": 45,
        "error": null,
        "processedAt": "2024-01-20T16:05:00Z"
    }
}
```

**文档状态说明**:

| 状态 | 说明 |
|------|------|
| PENDING | 等待处理 |
| PROCESSING | 处理中 |
| COMPLETED | 处理完成 |
| FAILED | 处理失败 |

### 3.4 获取文档列表

**接口路径**: `GET /api/v1/knowledge-bases/{kbId}/documents`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |
| status | String | 否 | 状态筛选 |
| category | String | 否 | 分类筛选 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "id": 100,
                "filename": "技术架构.pdf",
                "fileSize": 1024000,
                "mimeType": "application/pdf",
                "category": "技术文档",
                "status": "COMPLETED",
                "chunks": 45,
                "metadata": {
                    "author": "张三",
                    "version": "1.0"
                },
                "uploadedAt": "2024-01-20T16:00:00Z",
                "processedAt": "2024-01-20T16:05:00Z"
            },
            {
                "id": 101,
                "filename": "使用指南.md",
                "fileSize": 51200,
                "mimeType": "text/markdown",
                "category": "用户手册",
                "status": "COMPLETED",
                "chunks": 12,
                "metadata": {},
                "uploadedAt": "2024-01-20T16:10:00Z",
                "processedAt": "2024-01-20T16:12:00Z"
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 20,
            "totalElements": 150
        }
    }
}
```

### 3.5 获取文档详情

**接口路径**: `GET /api/v1/documents/{documentId}`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "id": 100,
        "filename": "技术架构.pdf",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "knowledgeBaseId": 1,
        "knowledgeBaseName": "技术文档知识库",
        "status": "COMPLETED",
        "chunks": 45,
        "metadata": {
            "author": "张三",
            "version": "1.0",
            "pages": 15,
            "language": "zh"
        },
        "chunks": [
            {
                "id": 1001,
                "index": 0,
                "content": "第一章 技术架构概述...",
                "metadata": {
                    "page": 1,
                    "keywords": ["架构", "微服务", "分布式"]
                }
            }
        ],
        "uploadedAt": "2024-01-20T16:00:00Z",
        "processedAt": "2024-01-20T16:05:00Z"
    }
}
```

### 3.6 删除文档

**接口路径**: `DELETE /api/v1/documents/{documentId}`

**响应示例**:

```json
{
    "code": 200,
    "message": "文档删除成功"
}
```

### 3.7 重新处理文档

**接口路径**: `POST /api/v1/documents/{documentId}/reprocess`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentId": 100,
        "status": "PROCESSING",
        "message": "文档重新处理任务已创建"
    }
}
```

---

## 四、问答 API

### 4.1 知识库问答

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/chat`

**请求参数**:

```json
{
    "message": "公司的技术架构是什么样的？",
    "sessionId": "session-abc123",
    "options": {
        "maxResults": 5,
        "searchType": "HYBRID",
        "enableRerank": true,
        "stream": false
    }
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 用户问题，最大2000字符 |
| sessionId | String | 否 | 会话ID，用于连续对话 |
| options | Object | 否 | 问答选项 |
| options.maxResults | Integer | 否 | 返回结果数量，默认5 |
| options.searchType | String | 否 | 搜索类型：VECTOR/FT/HYBRID |
| options.enableRerank | Boolean | 否 | 是否启用重排序，默认true |
| options.stream | Boolean | 否 | 是否流式输出，默认false |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "sessionId": "session-abc123",
        "messageId": "msg-xyz789",
        "response": "根据知识库内容，公司的技术架构主要包括以下几个层次：\n\n1. 前端层：采用React框架，支持Web和移动端\n2. 网关层：使用Spring Cloud Gateway\n3. 服务层：基于Spring Boot的微服务架构\n4. 数据层：包括SeekDB向量数据库和MySQL关系数据库",
        "sources": [
            {
                "documentId": 100,
                "documentName": "技术架构.pdf",
                "chunkId": "chunk-001",
                "score": 0.85,
                "content": "相关段落内容..."
            },
            {
                "documentId": 101,
                "documentName": "系统设计.md",
                "chunkId": "chunk-015",
                "score": 0.78,
                "content": "相关段落内容..."
            }
        ],
        "tokens": {
            "prompt": 1500,
            "completion": 300
        },
        "createdAt": "2024-01-20T16:30:00Z"
    }
}
```

### 4.2 流式问答

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/chat/stream`

**请求参数**:

```json
{
    "message": "详细介绍微服务架构",
    "sessionId": "session-abc123",
    "options": {
        "maxResults": 5
    }
}
```

**响应 (SSE 格式)**:

```
data: {"chunk": "微", "done": false}
data: {"chunk": "服务", "done": false}
data: {"chunk": "架构", "done": false}
data: {"chunk": "是一", "done": false}
data: {"chunk": "种将", "done": false}
data: {"chunk": "大型应", "done": false}
data: {"chunk": "用拆分", "done": false}
data: {"chunk": "为多个", "done": false}
data: {"chunk": "小型服务", "done": false}
data: {"chunk": "的架构", "done": false}
data: {"chunk": "模式。", "done": false}
...
data: {"done": true, "sources": [...], "tokens": {...}}
```

### 4.3 获取会话历史

**接口路径**: `GET /api/v1/chat/sessions/{sessionId}/messages`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "sessionId": "session-abc123",
        "messages": [
            {
                "id": 1,
                "role": "user",
                "content": "公司的技术架构是什么样的？",
                "createdAt": "2024-01-20T16:30:00Z"
            },
            {
                "id": 2,
                "role": "assistant",
                "content": "根据知识库内容...",
                "sources": [...],
                "tokens": {...},
                "createdAt": "2024-01-20T16:30:05Z"
            },
            {
                "id": 3,
                "role": "user",
                "content": "详细介绍微服务架构",
                "createdAt": "2024-01-20T16:31:00Z"
            }
        ]
    }
}
```

### 4.4 创建新会话

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/chat/sessions`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "sessionId": "session-new-456",
        "knowledgeBaseId": 1,
        "createdAt": "2024-01-20T17:00:00Z"
    }
}
```

### 4.5 删除会话

**接口路径**: `DELETE /api/v1/chat/sessions/{sessionId}`

**响应示例**:

```json
{
    "code": 200,
    "message": "会话删除成功"
}
```

### 4.6 问答反馈

**接口路径**: `POST /api/v1/chat/messages/{messageId}/feedback`

**请求参数**:

```json
{
    "rating": "POSITIVE",
    "comment": "回答很准确"
}
```

**rating 可选值**: POSITIVE / NEGATIVE

**响应示例**:

```json
{
    "code": 200,
    "message": "反馈已提交"
}
```

---

## 五、搜索 API

### 5.1 混合搜索

**接口路径**: `GET /api/v1/knowledge-bases/{kbId}/search`

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| q | String | 是 | 搜索关键词 |
| searchType | String | 否 | 搜索类型 |
| limit | Integer | 否 | 返回结果数量 |
| filters | String | 否 | 过滤条件JSON |

**filters 示例**:

```json
{
    "documentType": ["pdf", "md"],
    "language": "zh",
    "dateRange": {
        "start": "2024-01-01",
        "end": "2024-01-31"
    }
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "query": "微服务架构",
        "totalResults": 150,
        "results": [
            {
                "documentId": 100,
                "documentName": "技术架构.pdf",
                "chunkId": "chunk-001",
                "content": "微服务架构是一种将大型应用拆分为多个小型服务的架构模式...",
                "score": 0.92,
                "highlight": {
                    "content": "...<em>微服务架构</em>是一种将大型应用拆分..."
                }
            }
        ],
        "took": 15
    }
}
```

### 5.2 向量搜索

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/vector-search`

**请求参数**:

```json
{
    "vector": [0.1, 0.2, 0.3, ...],
    "limit": 10,
    "filters": {
        "language": "zh"
    }
}
```

### 5.3 全文搜索

**接口路径**: `POST /api/v1/knowledge-bases/{kbId}/fulltext-search`

**请求参数**:

```json
{
    "query": "微服务 架构 设计",
    "limit": 20,
    "highlight": true
}
```

---

## 六、管理 API

### 6.1 系统健康检查

**接口路径**: `GET /actuator/health`

**响应示例**:

```json
{
    "status": "UP",
    "components": {
        "seekdb": {
            "status": "UP",
            "details": {
                "database": "seekdb",
                "version": "1.0.1"
            }
        },
        "dashscope": {
            "status": "UP",
            "details": {
                "models": ["text-embedding-v2", "qwen-max"]
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 512000000000,
                "free": 256000000000,
                "threshold": 10000000000
            }
        }
    }
}
```

### 6.2 处理统计

**接口路径**: `GET /api/v1/admin/statistics`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "knowledgeBases": {
            "total": 10,
            "active": 8
        },
        "documents": {
            "total": 1500,
            "processing": 5,
            "completed": 1490,
            "failed": 5
        },
        "chunks": {
            "total": 45000
        },
        "processingQueue": {
            "pending": 10,
            "avgWaitTime": 5000
        },
        "storage": {
            "vectorIndexSize": "1.8GB",
            "documentStorage": "2.5GB"
        },
        "performance": {
            "avgDocumentProcessingTimeMs": 2500,
            "avgQueryTimeMs": 150
        }
    }
}
```

### 6.3 系统配置

**接口路径**: `GET /api/v1/admin/config`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "version": "1.0.0",
        "seekdb": {
            "host": "localhost",
            "port": 3307
        },
        "ai": {
            "embeddingModel": "text-embedding-v2",
            "chatModel": "qwen-max",
            "rerankModel": "cohere-rerank-v3"
        },
        "processing": {
            "maxFileSize": "100MB",
            "supportedFormats": ["pdf", "docx", "md", "txt", "html"],
            "defaultChunkSize": 1000,
            "defaultChunkOverlap": 200
        }
    }
}
```

### 6.4 清理任务

**接口路径**: `POST /api/v1/admin/cleanup`

**请求参数**:

```json
{
    "type": "failed_documents",
    "olderThan": "7d"
}
```

**type 可选值**: 
- failed_documents
- temp_files
- old_sessions
- all

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "type": "failed_documents",
        "deletedCount": 50,
        "freedSpace": "500MB",
        "executedAt": "2024-01-20T18:00:00Z"
    }
}
```

---

## 七、MCP API (新增)

### 7.1 MCP工具调用

**接口路径**: `POST /mcp/tools/call`

**请求参数**:

```json
{
    "tool": "queryKnowledgeBase",
    "arguments": {
        "knowledgeBaseId": 1,
        "question": "公司的技术架构是什么样的？"
    }
}
```

**可用工具**:

| 工具名称 | 描述 | 参数 |
|----------|------|------|
| `createKnowledgeBase` | 创建新的知识库 | name, description |
| `queryKnowledgeBase` | 基于知识库进行问答 | knowledgeBaseId, question |
| `uploadDocument` | 上传文档到知识库 | knowledgeBaseId, content, title |
| `searchDocuments` | 搜索知识库文档 | knowledgeBaseId, query |

**响应示例**:

```json
{
    "success": true,
    "result": "根据知识库内容，公司的技术架构主要包括...",
    "traceId": "abc123def456"
}
```

### 7.2 MCP工具列表

**接口路径**: `GET /mcp/tools`

**响应示例**:

```json
{
    "tools": [
        {
            "name": "queryKnowledgeBase",
            "description": "基于知识库进行问答",
            "parameters": {
                "knowledgeBaseId": {
                    "type": "long",
                    "description": "知识库ID"
                },
                "question": {
                    "type": "string",
                    "description": "用户问题"
                }
            }
        }
    ]
}
```

---

## 八、可观测性 API (新增)

### 8.1 指标数据

**接口路径**: `GET /actuator/prometheus`

**说明**: Prometheus格式的指标数据，用于监控和告警系统集成。

### 8.2 链路追踪

**接口路径**: `GET /actuator/httptrace`

**响应示例**:

```json
{
    "traces": [
        {
            "timestamp": "2025-01-20T16:30:00Z",
            "traceId": "abc123def456",
            "spanId": "span-001",
            "name": "chat",
            "duration": 150,
            "tags": {
                "kb.id": "1"
            }
        }
    ]
}
```

### 8.3 自定义指标

**接口路径**: `GET /api/v1/admin/metrics`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentProcessing": {
            "count": 1500,
            "avgDurationMs": 2500,
            "maxDurationMs": 10000
        },
        "vectorSearch": {
            "count": 5000,
            "avgDurationMs": 150,
            "p95DurationMs": 300
        },
        "ragQuery": {
            "count": 3000,
            "avgDurationMs": 2000,
            "p99DurationMs": 5000
        },
        "mcpCalls": {
            "count": 500,
            "successRate": 0.98
        }
    }
}
```

### 8.4 健康检查详情

**接口路径**: `GET /actuator/health/details`

**响应示例**:

```json
{
    "status": "UP",
    "components": {
        "seekdb": {
            "status": "UP",
            "details": {
                "database": "seekdb",
                "version": "1.0.1",
                "connections": 10,
                "vectorIndexCount": 45000
            }
        },
        "dashscope": {
            "status": "UP",
            "details": {
                "apiKeyConfigured": true,
                "modelsAvailable": ["text-embedding-v3", "qwen-max"]
            }
        },
        "redis": {
            "status": "UP",
            "details": {
                "connected": true,
                "memoryUsage": "45MB"
            }
        },
        "liveness": {
            "status": "UP"
        },
        "readiness": {
            "status": "UP"
        }
    }
}
```

---

## 九、权限管理 API (新增)

### 9.1 文档可见性设置

**接口路径**: `PUT /api/v1/documents/{documentId}/visibility`

**请求参数**:

```json
{
    "visibility": "PRIVATE",
    "isMechanism": false
}
```

**visibility 可选值**:
- PRIVATE - 私有文档，仅本人使用
- SHARED - 共享文档，全局可见
- MECHANISM_PRIVATE - 机制私有，仅本人使用
- MECHANISM_SHARED - 机制共享，审核后全局可见

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentId": 100,
        "visibility": "PRIVATE",
        "isMechanism": false,
        "requiresAudit": false,
        "updatedAt": "2025-01-20T10:00:00Z"
    }
}
```

### 9.2 申请发布共享

**接口路径**: `POST /api/v1/documents/{documentId}/publish`

**请求参数**:

```json
{
    "target": "SHARED",
    "reason": "这是治安业务相关知识，希望全局共享"
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "documentId": 100,
        "status": "PENDING_AUDIT",
        "auditId": 50,
        "message": "已提交发布申请，等待管理员审核"
    }
}
```

### 9.3 审核列表

**接口路径**: `GET /api/v1/admin/audit/pending`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| size | Integer | 每页数量 |
| type | String | 审核类型: PUBLISH/MECHANISM |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "auditId": 50,
                "documentId": 100,
                "documentTitle": "治安管理处罚法解读",
                "documentType": "SHARED",
                "applicant": {
                    "id": 1001,
                    "username": "zhangsan",
                    "policeNo": "012345",
                    "department": "XX分局治安大队"
                },
                "applyTime": "2025-01-20T09:00:00Z",
                "reason": "这是治安业务相关知识，希望全局共享"
            }
        ],
        "totalElements": 5
    }
}
```

### 9.4 审核操作

**接口路径**: `POST /api/v1/admin/audit/{auditId}`

**请求参数**:

```json
{
    "action": "APPROVE",
    "comment": "内容准确，符合发布要求"
}
```

**action 可选值**: APPROVE / REJECT

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "auditId": 50,
        "status": "APPROVED",
        "documentId": 100,
        "visibility": "SHARED",
        "auditor": {
            "id": 2001,
            "username": "admin"
        },
        "auditTime": "2025-01-20T10:30:00Z"
    }
}
```

### 9.5 我的文档

**接口路径**: `GET /api/v1/documents/my`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| visibility | String | 可见性筛选: PRIVATE/SHARED/PENDING |
| page | Integer | 页码 |
| size | Integer | 每页数量 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "id": 100,
                "title": "我的工作笔记",
                "visibility": "PRIVATE",
                "status": "PUBLISHED",
                "createdAt": "2025-01-15T10:00:00Z"
            }
        ],
        "totalElements": 10
    }
}
```

---

## 十、监控运维 API (新增)

### 10.1 同步状态监控

**接口路径**: `GET /api/v1/admin/monitor/sync`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "lastSyncTime": "2025-01-20T02:00:00Z",
        "syncStatus": "COMPLETED",
        "todayStats": {
            "successCount": 150,
            "failedCount": 2,
            "syncDurationSeconds": 120
        },
        "historyStats": {
            "last7Days": {
                "totalSync": 1050,
                "successRate": 0.98,
                "avgDurationSeconds": 115
            }
        },
        "failedDocuments": [
            {
                "documentId": "ext-doc-001",
                "title": "XX规定",
                "error": "文档格式解析失败",
                "retryCount": 3
            }
        ]
    }
}
```

### 10.2 手动触发同步

**接口路径**: `POST /api/v1/admin/monitor/sync/trigger`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "syncTaskId": "sync-task-20250120-001",
        "status": "STARTED",
        "message": "手动同步任务已启动",
        "estimatedDuration": 120
    }
}
```

### 10.3 查询热度统计

**接口路径**: `GET /api/v1/admin/monitor/query-heat`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| period | String | 时间范围: TODAY/WEEK/MONTH |
| top | Integer | 返回数量，默认10 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "period": "WEEK",
        "totalQueries": 15000,
        "hotKeywords": [
            {"keyword": "打架", "count": 1250, "trend": "UP"},
            {"keyword": "盗窃", "count": 980, "trend": "STABLE"},
            {"keyword": "赌博", "count": 750, "trend": "DOWN"}
        ],
        "hotDocuments": [
            {"documentId": 100, "title": "治安管理处罚法", "accessCount": 3500},
            {"documentId": 101, "title": "户籍办理指南", "accessCount": 2800}
        ],
        "zeroResultQueries": [
            {"keyword": "涉黄处理", "count": 50},
            {"keyword": "电动车管理", "count": 35}
        ]
    }
}
```

### 10.4 用户行为分析

**接口路径**: `GET /api/v1/admin/monitor/user-behavior`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| period | String | 时间范围: TODAY/WEEK/MONTH |
| granularity | String | 粒度: HOUR/DAY/WEEK |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "period": "WEEK",
        "activeUsers": {
            "daily": 1200,
            "weekly": 3500,
            "monthly": 8000
        },
        "usageDistribution": {
            "byPoliceType": [
                {"type": "治安", "percentage": 45},
                {"type": "刑侦", "percentage": 25},
                {"type": "交警", "percentage": 15},
                {"type": "社区", "percentage": 10},
                {"type": "其他", "percentage": 5}
            ],
            "byFunction": [
                {"function": "知识搜索", "percentage": 60},
                {"function": "智能问答", "percentage": 35},
                {"function": "文档管理", "percentage": 5}
            ]
        },
        "userActivityTrend": [
            {"date": "2025-01-14", "activeUsers": 1100},
            {"date": "2025-01-15", "activeUsers": 1250},
            {"date": "2025-01-16", "activeUsers": 1180}
        ]
    }
}
```

### 10.5 系统健康状态

**接口路径**: `GET /api/v1/admin/monitor/health`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "overallStatus": "HEALTHY",
        "components": {
            "seekdb": {
                "status": "UP",
                "connections": 10,
                "vectorIndexCount": 45000
            },
            "dashscope": {
                "status": "UP",
                "modelsAvailable": ["text-embedding-v3", "qwen-max"]
            },
            "redis": {
                "status": "UP",
                "memoryUsage": "45MB"
            },
            "cpu": {
                "usage": 35,
                "status": "NORMAL"
            },
            "memory": {
                "usage": 62,
                "status": "NORMAL"
            }
        },
        "alerts": []
    }
}
```

---

## 十一、治安平台对接 API (新增)

### 11.1 同步任务状态

**接口路径**: `GET /api/v1/admin/security-platform/sync-status`

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "syncEnabled": true,
        "syncSchedule": "0 0 2 * * ?",
        "lastSuccessfulSync": "2025-01-20T02:15:00Z",
        "syncStats": {
            "today": {
                "total": 150,
                "success": 148,
                "failed": 2
            },
            "totalSynced": 15000
        }
    }
}
```

### 11.2 手动同步触发

**接口路径**: `POST /api/v1/admin/security-platform/sync`

**请求参数**:

```json
{
    "fullSync": false,
    "documentTypes": ["REGULATION", "TRAINING"]
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "taskId": "sp-sync-20250120-001",
        "status": "RUNNING",
        "message": "同步任务已启动",
        "estimatedDocuments": 100
    }
}
```

### 11.3 同步历史记录

**接口路径**: `GET /api/v1/admin/security-platform/sync-history`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| size | Integer | 每页数量 |
| startDate | String | 开始日期 |
| endDate | String | 结束日期 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "syncId": "sp-sync-20250120-001",
                "syncType": "SCHEDULED",
                "startTime": "2025-01-20T02:00:00Z",
                "endTime": "2025-01-20T02:15:00Z",
                "status": "COMPLETED",
                "documentsProcessed": 150,
                "documentsFailed": 0
            }
        ],
        "totalElements": 30
    }
}
```

### 11.4 文档来源查询

**接口路径**: `GET /api/v1/admin/security-platform/documents`

**查询参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| size | Integer | 每页数量 |
| source | String | 来源筛选 |
| category | String | 分类筛选 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "content": [
            {
                "externalId": "sp-doc-001",
                "title": "治安管理处罚法（2023修订）",
                "source": "公安部",
                "category": "REGULATION",
                "originalUrl": "https://security-platform.moj.gov.cn/doc/001",
                "syncedAt": "2025-01-20T02:10:00Z",
                "localDocumentId": 100
            }
        ],
        "totalElements": 15000
    }
}
```

---

## 十二、字典管理 API (新增)

### 12.1 获取归属地列表

**接口路径**: `GET /v1/dict/origin-scope`

**说明**: 获取所有归属地选项，用于文档创建和搜索筛选。

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 状态筛选：0-停用，1-启用，默认1 |

**响应示例**:

```json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "kind": "origin_scope",
            "code": "national",
            "detail": "国家级",
            "alias": null,
            "description": null,
            "parentId": 0,
            "parentCode": null,
            "level": 1,
            "leaf": 1,
            "sort": 1,
            "status": 1,
            "isSystem": 0,
            "createdTime": "2025-01-20 10:00:00",
            "updatedTime": "2025-01-20 10:00:00"
        },
        {
            "id": 2,
            "kind": "origin_scope",
            "code": "provincial",
            "detail": "省级",
            "alias": null,
            "description": null,
            "parentId": 0,
            "parentCode": null,
            "level": 1,
            "leaf": 1,
            "sort": 3,
            "status": 1,
            "isSystem": 0,
            "createdTime": "2025-01-20 10:00:00",
            "updatedTime": "2025-01-20 10:00:00"
        }
    ]
}
```

**归属地编码说明**:

| code | 说明 |
|------|------|
| national | 国家级 |
| ministerial | 部级 |
| provincial | 省级 |
| municipal | 市级 |
| county | 县级 |
| unit | 单位级 |

### 12.2 获取来源部门树

**接口路径**: `GET /v1/dict/origin-department/tree`

**说明**: 获取完整的来源部门树形结构，支持多级部门。

**响应示例**:

```json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "kind": "origin_department",
            "code": "zazhi",
            "detail": "治安管理支队",
            "parentId": 0,
            "parentCode": null,
            "level": 1,
            "leaf": 0,
            "sort": 1,
            "status": 1,
            "children": [
                {
                    "id": 11,
                    "kind": "origin_department",
                    "code": "zazhi_1",
                    "detail": "一大队",
                    "parentId": 1,
                    "parentCode": "zazhi",
                    "level": 2,
                    "leaf": 1,
                    "sort": 1,
                    "status": 1
                },
                {
                    "id": 12,
                    "kind": "origin_department",
                    "code": "zazhi_2",
                    "detail": "二大队",
                    "parentId": 1,
                    "parentCode": "zazhi",
                    "level": 2,
                    "leaf": 1,
                    "sort": 2,
                    "status": 1
                }
            ]
        },
        {
            "id": 2,
            "kind": "origin_department",
            "code": "xingzheng",
            "detail": "刑侦支队",
            "parentId": 0,
            "parentCode": null,
            "level": 1,
            "leaf": 0,
            "sort": 2,
            "status": 1,
            "children": [
                {
                    "id": 21,
                    "kind": "origin_department",
                    "code": "xingzheng_1",
                    "detail": "一大队",
                    "parentId": 2,
                    "parentCode": "xingzheng",
                    "level": 2,
                    "leaf": 1,
                    "sort": 1,
                    "status": 1
                }
            ]
        }
    ]
}
```

### 12.3 获取知识库分类树

**接口路径**: `GET /v1/dict/kb-category/tree`

**说明**: 获取知识库分类的完整树形结构，用于文档分类管理。

**响应示例**:

```json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "kind": "kb_category",
            "code": "falvfagui",
            "detail": "法律法规",
            "parentId": 0,
            "level": 1,
            "leaf": 0,
            "sort": 1,
            "status": 1,
            "children": [
                {
                    "id": 11,
                    "kind": "kb_category",
                    "code": "falvfagui_xingfa",
                    "detail": "刑法",
                    "parentId": 1,
                    "parentCode": "falvfagui",
                    "level": 2,
                    "leaf": 1,
                    "sort": 1,
                    "status": 1
                },
                {
                    "id": 12,
                    "kind": "kb_category",
                    "code": "falvfagui_minshi",
                    "detail": "民法",
                    "parentId": 1,
                    "parentCode": "falvfagui",
                    "level": 2,
                    "leaf": 1,
                    "sort": 2,
                    "status": 1
                }
            ]
        }
    ]
}
```

### 12.4 获取字典类型列表

**接口路径**: `GET /v1/dict/types`

**说明**: 获取所有字典类型。

**响应示例**:

```json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "kind": "origin_scope",
            "detail": "归属地",
            "alias": null,
            "description": null,
            "icon": null,
            "color": null,
            "sort": 1,
            "status": 1
        },
        {
            "id": 2,
            "kind": "origin_department",
            "detail": "来源部门",
            "alias": null,
            "description": null,
            "icon": null,
            "color": null,
            "sort": 2,
            "status": 1
        },
        {
            "id": 3,
            "kind": "kb_category",
            "detail": "知识库分类",
            "alias": null,
            "description": null,
            "icon": null,
            "color": null,
            "sort": 3,
            "status": 1
        }
    ]
}
```

### 12.5 获取字典分页列表

**接口路径**: `GET /v1/dict`

**说明**: 分页查询字典数据。

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| kind | String | 否 | 字典类型 |
| keyword | String | 否 | 关键词搜索 |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "records": [
            {
                "id": 1,
                "kind": "origin_scope",
                "code": "national",
                "detail": "国家级",
                "alias": null,
                "description": null,
                "icon": null,
                "color": null,
                "parentId": 0,
                "parentCode": null,
                "level": 1,
                "leaf": 1,
                "sort": 1,
                "status": 1,
                "isSystem": 0,
                "isPublic": 0,
                "remark": null,
                "createdTime": "2025-01-20 10:00:00",
                "updatedTime": "2025-01-20 10:00:00"
            }
        ],
        "total": 10,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

---

## 十三、文档搜索 API (新增过滤参数)

### 13.1 获取文档列表（支持归属地过滤）

**接口路径**: `GET /v1/documents`

**说明**: 分页获取文档列表，支持按归属地和来源部门筛选。

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| kbId | Long | 否 | 知识库ID |
| keyword | String | 否 | 关键词搜索 |
| originScope | String | 否 | 归属地筛选：national/ministrial/provincial/municipal/county/unit |
| originDepartment | String | 否 | 来源部门筛选（支持模糊查询） |

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "records": [
            {
                "id": 100,
                "title": "治安管理处罚法解读",
                "content": "...",
                "summary": "...",
                "status": 1,
                "statusName": "已发布",
                "kbId": 1,
                "kbName": "法律法规知识库",
                "categoryId": 11,
                "categoryName": "刑法",
                "originScope": "provincial",
                "originDepartment": "治安管理支队-二大队",
                "tags": "治安,处罚",
                "viewCount": 150,
                "favoriteCount": 10,
                "isTop": 0,
                "isHot": 1,
                "publishTime": "2025-01-20 10:00:00",
                "sourceTime": "2025-01-15 08:00:00",
                "createdTime": "2025-01-20 10:00:00",
                "updatedTime": "2025-01-20 10:00:00"
            }
        ],
        "total": 50,
        "size": 10,
        "current": 1,
        "pages": 5
    }
}
```

### 13.2 创建文档（支持归属地和来源部门）

**接口路径**: `POST /v1/documents`

**请求参数**:

```json
{
    "title": "治安管理处罚法解读",
    "content": "文档内容...",
    "summary": "文档摘要",
    "kbId": 1,
    "categoryId": 11,
    "tags": "治安,处罚",
    "originScope": "provincial",
    "originDepartment": "治安管理支队-二大队",
    "source": "公安部",
    "author": "张三",
    "sourceTime": "2025-01-15 08:00:00"
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| originScope | String | 否 | 归属地：national/ministrial/provincial/municipal/county/unit |
| originDepartment | String | 否 | 来源部门，如：治安管理支队-二大队 |
| sourceTime | String | 否 | 文档来源时间，格式：yyyy-MM-dd HH:mm:ss |

### 13.3 更新文档（支持归属地和来源部门）

**接口路径**: `PUT /v1/documents/{id}`

**请求参数**:

```json
{
    "title": "更新后的标题",
    "originScope": "municipal",
    "originDepartment": "刑侦支队-六大队"
}
```

### 13.4 语义搜索（支持元数据过滤）

**接口路径**: `POST /v1/search/semantic`

**请求参数**:

```json
{
    "keyword": "盗窃罪的量刑标准",
    "kbId": 1,
    "originScope": "provincial",
    "originDepartment": "刑侦",
    "topK": 10
}
```

**originScope 可选值**: national, ministerial, provincial, municipal, county, unit

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "results": [
            {
                "documentId": 100,
                "title": "刑法盗窃罪量刑标准",
                "content": "盗窃公私财物，数额较大的，处三年以下有期徒刑...",
                "score": 0.92,
                "originScope": "provincial",
                "originDepartment": "刑侦支队-六大队"
            }
        ],
        "total": 5,
        "queryTimeMs": 150
    }
}
```

### 13.5 混合搜索（支持多条件过滤）

**接口路径**: `POST /v1/search/hybrid`

**请求参数**:

```json
{
    "keyword": "打架斗殴处理",
    "kbId": null,
    "originScope": "municipal",
    "originDepartment": "治安",
    "categoryId": null,
    "topK": 20,
    "threshold": 0.5
}
```

**响应示例**:

```json
{
    "code": 200,
    "data": {
        "keywordResults": [
            {
                "documentId": 101,
                "title": "打架斗殴处理指南",
                "score": 0.88,
                "source": "市公安局"
            }
        ],
        "vectorResults": [
            {
                "documentId": 102,
                "title": "治安管理处罚法相关解释",
                "score": 0.85,
                "originScope": "municipal",
                "originDepartment": "治安管理支队"
            }
        ],
        "total": 10,
        "queryTimeMs": 200
    }
}
```

---

## 十四、附录

### 9.1 支持的文档格式

| 格式 | MIME类型 | 最大文件大小 |
|------|----------|--------------|
| PDF | application/pdf | 100MB |
| Word | application/vnd.openxmlformats-officedocument.wordprocessingml.document | 50MB |
| Markdown | text/markdown | 10MB |
| 文本 | text/plain | 10MB |
| HTML | text/html | 10MB |

### 9.2 速率限制

| API类型 | 限制 |
|---------|------|
| 普通API | 100次/分钟 |
| 问答API | 50次/分钟 |
| 上传API | 20次/分钟 |
| MCP工具调用 | 100次/分钟 |

### 9.3 WebSocket 连接

对于需要实时推送的场景，支持WebSocket连接：

**连接路径**: `ws://{host}/api/v1/ws`

**消息格式**:

```json
{
    "type": "chat",
    "sessionId": "session-abc123",
    "message": "用户问题"
}
```

**接收消息**:

```json
{
    "type": "stream",
    "chunk": "回答内容片段",
    "done": false
}
```

### 9.4 版本信息

| 组件 | 版本 |
|------|------|
| API版本 | v1 |
| Spring AI | 1.1.2 |
| Spring AI Alibaba | 1.1.2.0 |
| Spring Boot | 3.5.7 |

---

**文档版本**: 2.1.0
**最后更新**: 2026年2月2日
