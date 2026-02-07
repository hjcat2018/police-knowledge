# 智能问答系统拆分与增强方案

## 项目背景

现有智能问答页面包含"普通模式"和"专业模式"两种功能，需要拆分为两个独立页面，并借鉴 DeepResearch 示例增强功能。

---

## 一、现状分析

### 1.1 现有功能概览

| 功能模块 | 普通模式 | 专业模式 | 状态 |
|---------|---------|---------|------|
| 对话列表管理 | ✓ | ✓ | 已实现 |
| 模式切换 | ✓ | ✓ | 已实现 |
| 知识库选择 | ✗ | ✓ (级联) | 已实现 |
| SSE流式对话 | ✓ | ✓ | 已实现 |
| 参考文档展示 | ✗ | ✓ | 已实现 |
| 多模型选择 | ✗ | ✗ | 未实现 |
| 文件上传 | ✗ | ✗ | 未实现 |
| MCP服务选择 | ✗ | ✗ | 未实现 |

### 1.2 拆分需求

将现有 `/chat` 页面拆分为两个独立页面：
- `/chat/professional` - 专业模式（知识问答）
- `/chat/normal` - 普通模式（通用对话）

---

## 二、借鉴 DeepResearch 示例特性

> 参考: https://java2ai.com/agents/deepresearch/quick-start

### 2.1 可借鉴特性

| 特性 | 说明 | 借鉴程度 |
|------|------|---------|
| **任务规划** | TodoListInterceptor 分解复杂任务为步骤 | ⭐⭐⭐ 高 |
| **子智能体协作** | research-agent / critique-agent | ⭐⭐⭐ 高 |
| **上下文管理** | 自动压缩、大结果落盘 | ⭐⭐ 高 |
| **MCP工具集成** | Spring AI MCP Client | ⭐⭐⭐ 高 |
| **重试机制** | ToolRetryInterceptor | ⭐⭐⭐ 高 |
| **并行执行** | 独立子任务并行执行 | ⭐⭐⭐ 高 |

### 2.2 新增功能（借鉴后）

| 功能 | 说明 | 借鉴来源 |
|------|------|---------|
| 回答质量评估 | 评估多模型回答质量，标记最佳回答 | Critique-Agent |
| 任务分解 | 复杂问题分解为子问题 | TodoListInterceptor |
| 增强MCP配置 | API Key/URL/超时/认证方式配置 | MCP Client |
| 上下文自动管理 | 自动压缩、大结果落盘 | ContextEditingInterceptor |

---

## 三、最终确认需求

| # | 问题 | 确认答案 |
|---|------|---------|
| 1 | 对话历史是否完全独立 | ✅ 完全独立 |
| 2 | 支持哪些文件类型 | PDF / Word / Excel / TXT / Markdown |
| 3 | 最大文件大小限制 | 10MB |
| 4 | 最大同时请求模型数 | 6个 |
| 5 | MCP服务默认启用哪些 | 空配置 |
| 6 | 是否需要回答对比功能 | 后期 |
| 7 | 是否需要回答导出功能 | 后期 |
| 8 | 普通模式需要哪些角色权限 | 所有用户 |
| 9 | 文件解析失败如何处理 | 跳过文件，继续处理 |
| 10 | 单个模型失败如何处理 | 重试3次 |

---

## 四、架构设计

### 4.1 前端架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端架构                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  /chat/professional                    /chat/normal                          │
│  ┌─────────────────────┐              ┌─────────────────────────────────┐   │
│  │ 专业模式            │              │ 普通模式                         │   │
│  │ • 知识库问答        │              │ • 多模型并行回答（最多6个）      │   │
│  │ • RAG检索          │              │ • 文件上传（PDF/Word/Excel等）   │   │
│  │ • 参考文档         │              │ • 个人知识库选择                 │   │
│  │ • 独立对话历史     │              │ • MCP服务配置                    │   │
│  └─────────────────────┘              │ • 回答质量评估（新增）           │   │
│                                       │ • 任务分解（新增）                │   │
│                                       │ • 独立对话历史                   │   │
│                                       └─────────────────────────────────┘   │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        共用组件 (components/)                         │   │
│  │   ConversationList  │  ChatMessages  │  ChatInput                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 后端API架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              后端API                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  POST /api/v1/chat/normal              多模型并行对话（流式输出）            │
│  POST /api/v1/chat/analyze             文件分析对话                          │
│  GET  /api/v1/models                   模型列表（含配置参数）                │
│  GET  /api/v1/models/{id}/config       模型配置参数                          │
│  GET  /api/v1/mcp/services             MCP服务列表                           │
│  GET  /api/v1/mcp/services/{id}        MCP服务详情                           │
│  PUT  /api/v1/mcp/services/{id}/config 保存MCP配置                           │
│  POST /api/v1/files/upload             文件上传                              │
│  POST /api/v1/files/parse              文件解析（后端统一处理）              │
│  POST /api/v1/chat/evaluate            回答质量评估                          │
│  POST /api/v1/chat/decompose           任务分解                              │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 五、目录结构

### 5.1 前端目录结构

```
frontend/src/views/chat/
├── professional/                    # 专业模式
│   └── index.vue                    # 专业模式页面（从原chat/index.vue迁移）
│
├── normal/                          # 普通模式
│   └── index.vue                    # 普通模式页面
│   ├── components/
│   │   ├── ModelSelector.vue        # 模型选择器
│   │   ├── FileUploader.vue         # 文件上传器
│   │   ├── KnowledgeSelector.vue    # 知识库选择器
│   │   ├── McpPanel.vue             # MCP配置面板
│   │   ├── ModelAnswerCard.vue      # 模型回答卡片
│   │   └── TaskPlanPanel.vue        # 任务分解面板（新增）
│   └── composables/
│       ├── useMultiModel.ts         # 多模型并行请求
│       ├── useContextManager.ts     # 上下文管理
│       └── useQualityEvaluation.ts  # 质量评估
│
├── components/                      # 共享组件
│   ├── ConversationList.vue         # 对话列表
│   ├── ChatMessages.vue             # 消息展示
│   └── ChatInput.vue                # 输入组件
│
types/
└── chat.ts                          # 类型定义
```

### 5.2 后端目录结构

```
backend/src/main/java/com/police/kb/
├── controller/
│   ├── ChatController.java          # 聊天API（新增normal/analyze）
│   ├── McpController.java           # MCP服务API
│   └── FileController.java          # 文件解析API
│
├── service/
│   ├── ChatService.java
│   ├── ChatServiceImpl.java
│   ├── McpService.java              # MCP服务接口
│   ├── McpServiceImpl.java          # MCP服务实现
│   ├── QualityEvaluationService.java # 质量评估服务（新增）
│   └── TaskDecompositionService.java # 任务分解服务（新增）
│
├── domain/
│   ├── dto/
│   │   ├── NormalChatRequest.java   # 普通模式请求DTO
│   │   ├── MultiModelResponse.java  # 多模型响应DTO
│   │   ├── ModelResult.java         # 单模型结果
│   │   ├── AnswerEvaluation.java    # 回答评估DTO（新增）
│   │   └── TaskPlan.java            # 任务分解DTO（新增）
│   │
│   └── entity/
│       ├── McpServiceEntity.java    # MCP服务实体
│       ├── McpConfigEntity.java     # MCP配置实体
│       └── ConversationEvaluation.java # 对话评估实体（新增）
│
└── config/
    └── McpClientConfig.java         # MCP客户端配置（新增）
```

---

## 六、组件设计

### 6.1 ModelSelector（模型选择器）

**功能**: 多选模型，最多6个

```vue
<template>
  <el-select
    v-model="selectedModels"
    multiple
    collapse-tags
    collapse-tags-tooltip
    :max-collapse-tags="2"
    placeholder="选择模型（最多6个）"
    style="width: 280px"
  >
    <el-option
      v-for="model in availableModels"
      :key="model.id"
      :label="model.name"
      :value="model.id"
      :disabled="!selectedModels.includes(model.id) && selectedModels.length >= 6"
    >
      <span>{{ model.name }}</span>
      <el-tag size="small" type="info" style="margin-left: 8px">{{ model.provider }}</el-tag>
    </el-option>
  </el-select>
</template>
```

### 6.2 ModelAnswerCard（模型回答卡片）

**功能**: 展示单个模型的回答，支持流式输出和质量评估

```vue
<template>
  <el-card class="model-answer-card" :class="{ loading: streaming }">
    <template #header>
      <div class="card-header">
        <el-tag :type="isBestAnswer ? 'success' : 'info'" effect="dark">
          <el-icon><component :is="isBestAnswer ? 'Star' : 'Check'" /></el-icon>
          {{ modelName }}
        </el-tag>
        <div class="meta">
          <el-tooltip v-if="tokens > 0" :content="`消耗 ${tokens} tokens`">
            <span class="tokens">{{ formatTokens(tokens) }}</span>
          </el-tooltip>
          <el-tooltip v-if="latency > 0" :content="`响应 ${latency}ms`">
            <span class="latency">{{ formatLatency(latency) }}</span>
          </el-tooltip>
          <el-tag v-if="isBestAnswer" type="success" size="small">最佳</el-tag>
        </div>
      </div>
    </template>
    
    <div class="answer-content" v-html="renderedContent" />
    
    <template #footer v-if="streaming">
      <el-progress :percentage="progress" :stroke-width="4" :status="retryCount > 0 ? 'warning' : undefined">
        <span>{{ streaming ? '流式输出中...' : '加载完成' }}</span>
      </el-progress>
    </template>
  </el-card>
</template>
```

### 6.3 FileUploader（文件上传器）

**功能**: 支持5种文件类型，单个最大10MB，最多10个文件

```vue
<template>
  <el-upload
    ref="uploadRef"
    :auto-upload="false"
    :accept="acceptedTypes"
    :limit="maxFiles"
    :max-size="maxFileSize * 1024 * 1024"
    :on-change="handleFileChange"
    :on-exceed="handleExceed"
    :on-remove="handleRemove"
    multiple
  >
    <el-button>+ 上传文件</el-button>
    <template #tip>
      <div class="file-list" v-if="fileList.length > 0">
        <el-tag v-for="file in fileList" :key="file.uid" closable type="primary">
          <el-icon><Document /></el-icon>
          {{ file.name }} ({{ formatSize(file.size) }})
        </el-tag>
      </div>
      <div class="tip">
        支持 {{ acceptedTypes }}，单个最大 {{ maxFileSize }}MB，最多 {{ maxFiles }} 个
      </div>
    </template>
  </el-upload>
</template>
```

### 6.4 McpPanel（MCP配置面板）

**功能**: 配置MCP服务（API Key/URL/超时/认证方式）

```vue
<template>
  <el-drawer v-model="visible" title="MCP服务配置" size="450px">
    <el-form v-for="service in services" :key="service.id" class="mcp-service-form">
      <el-divider content-position="left">
        {{ service.name }}
        <el-tag v-if="service.enabled" type="success" size="small" style="margin-left: 8px">已启用</el-tag>
      </el-divider>
      
      <el-form-item label="启用">
        <el-switch v-model="service.enabled" />
      </el-form-item>
      
      <template v-if="service.enabled">
        <el-form-item label="API URL" required>
          <el-input v-model="service.config.url" placeholder="https://api.example.com/mcp" />
        </el-form-item>
        
        <el-form-item label="认证方式">
          <el-select v-model="service.config.authType" style="width: 100%">
            <el-option label="API Key" value="api_key" />
            <el-option label="Bearer Token" value="bearer" />
            <el-option label="OAuth 2.0" value="oauth2" />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="service.config.authType !== 'none'" label="凭证" required>
          <el-input v-model="service.config.credentials" type="password" show-password placeholder="请输入凭证" />
        </el-form-item>
        
        <el-form-item label="超时时间">
          <el-input-number v-model="service.config.timeout" :min="5" :max="300" :step="5" />
          <span style="margin-left: 8px">秒</span>
        </el-form-item>
        
        <el-form-item label="请求方法">
          <el-radio-group v-model="service.config.method">
            <el-radio-button label="POST">POST</el-radio-button>
            <el-radio-button label="GET">GET</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </template>
    </el-form>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save" :loading="saving">保存配置</el-button>
    </template>
  </el-drawer>
</template>
```

### 6.5 TaskPlanPanel（任务分解面板）

**功能**: 展示复杂问题的任务分解进度

```vue
<template>
  <el-collapse v-model="activeNames">
    <el-collapse-item title="📋 任务分解" name="task-plan">
      <el-steps :active="currentStep" finish-status="success" direction="vertical">
        <el-step
          v-for="(task, index) in taskPlan"
          :key="index"
          :title="task.title"
          :description="task.description"
          :status="getTaskStatus(task.status)"
        />
      </el-steps>
    </el-collapse-item>
  </el-collapse>
</template>
```

### 6.6 PromptTemplateDialog（提示词模板对话框 - 本次新增）

**功能**: 编辑和管理提示词模板，支持变量替换

```vue
<template>
  <el-dialog v-model="visible" title="提示词模板" width="700px">
    <el-form :model="template" label-width="100px">
      <el-form-item label="模板名称" required>
        <el-input v-model="template.name" placeholder="请输入模板名称" />
      </el-form-item>
      
      <el-form-item label="模板内容" required>
        <el-input
          v-model="template.content"
          type="textarea"
          :rows="10"
          placeholder="请输入提示词模板，支持变量替换：&#10;{{question}} - 用户问题&#10;{{kb_name}} - 知识库名称&#10;{{user_name}} - 用户名称&#10;{{current_time}} - 当前时间"
        />
      </el-form-item>
      
      <el-form-item label="可用变量">
        <el-tag
          v-for="var in availableVariables"
          :key="var"
          class="variable-tag"
          @click="insertVariable(var)"
        >
          {{var}}
        </el-tag>
      </el-form-item>
      
      <el-form-item label="模板描述">
        <el-input v-model="template.description" type="textarea" :rows="2" />
      </el-form-item>
      
      <el-form-item label="设为默认">
        <el-switch v-model="template.isDefault" />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>
```

### 6.7 TemplateManager（模板管理组件 - 本次新增）

**功能**: 模板列表管理，支持切换、编辑、删除

```vue
<template>
  <div class="template-manager">
    <div class="template-list">
      <el-radio-group v-model="selectedTemplateId" @change="onTemplateChange">
        <el-radio-button
          v-for="template in templates"
          :key="template.id"
          :label="template.id"
        >
          {{ template.name }}
          <el-tag v-if="template.isDefault" type="success" size="small">默认</el-tag>
        </el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="template-actions">
      <el-button @click="createTemplate">新建模板</el-button>
      <el-button @click="editTemplate" :disabled="!selectedTemplate">编辑</el-button>
      <el-button @click="deleteTemplate" :disabled="!selectedTemplate || selectedTemplate.isSystem">删除</el-button>
    </div>
  </div>
</template>
```

### 6.8 QuickCommands（快捷指令组件 - 本次新增）

**功能**: 预设常用指令，一键发送

```vue
<template>
  <div class="quick-commands">
    <div class="command-header">
      <span>快捷指令</span>
      <el-button text size="small" @click="showMore = !showMore">
        {{ showMore ? '收起' : '更多' }}
      </el-button>
    </div>
    
    <div class="command-list">
      <el-tooltip
        v-for="cmd in visibleCommands"
        :key="cmd.id"
        :content="cmd.description"
        placement="top"
      >
        <el-button
          class="command-btn"
          :style="{ '--cmd-color': cmd.color }"
          @click="executeCommand(cmd)"
        >
          <el-icon :size="16"><component :is="cmd.icon || 'ArrowRight'" /></el-icon>
          <span>{{ cmd.name }}</span>
        </el-button>
      </el-tooltip>
    </div>
    
    <div v-if="selectedText" class="text-selected-indicator">
      <el-icon><Edit /></el-icon>
      已选择文本，将用于快捷指令
    </div>
  </div>
</template>

<style lang="scss" scoped>
.quick-commands {
  .command-btn {
    --cmd-color: #409EFF;
    border-color: var(--cmd-color);
    color: var(--cmd-color);
    
    &:hover {
      background: var(--cmd-color);
      color: #fff;
    }
  }
}
</style>
```

---

## 七、核心业务逻辑

### 7.1 多模型并行请求（含重试）

```typescript
interface ModelRequest {
  model: string
  question: string
  files: FileContent[]
  kbId?: number
  mcpServices: string[]
  systemPrompt?: string
}

interface ModelResponse {
  model: string
  success: boolean
  answer?: string
  tokens?: number
  latency?: number
  error?: string
}

/**
 * 多模型并行请求（含重试机制）
 */
const sendToMultipleModels = async (request: ModelRequest): Promise<ModelResponse[]> => {
  const models = selectedModels.value
  
  // 并行发起请求
  const promises = models.map(modelId => 
    sendToSingleModel({
      ...request,
      model: modelId
    })
  )
  
  const results = await Promise.all(promises)
  return results
}

/**
 * 单模型请求（含重试3次）
 */
const sendToSingleModel = async (request: ModelRequest): Promise<ModelResponse> => {
  const maxRetries = 3
  let attempt = 0
  
  while (attempt < maxRetries) {
    try {
      const response = await fetch('/api/v1/chat/normal', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...request,
          options: { stream: true }
        })
      })
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      
      return await handleStreamingResponse(response, request.model)
      
    } catch (error) {
      attempt++
      if (attempt >= maxRetries) {
        return { model: request.model, success: false, error: `重试${maxRetries}次后失败: ${error.message}` }
      }
      await delay(1000 * attempt)  // 指数退避
    }
  }
  
  return { model: request.model, success: false, error: '未知错误' }
}
```

### 7.2 回答质量评估

```typescript
interface AnswerEvaluation {
  model: string
  score: number          // 0-100
  strengths: string[]    // 优势
  weaknesses: string[]   // 不足
  suggestions: string[]  // 建议
  bestAnswer: boolean    // 是否最佳
}

const EVALUATION_DIMENSIONS = {
  accuracy: '准确性',        // 回答是否准确
  completeness: '完整性',    // 是否完整回答问题
  clarity: '清晰度',         // 表达是否清晰
  relevance: '相关性',       // 是否紧扣问题
  citation: '引用规范性'     // 引用是否规范
}

/**
 * 评估多模型回答质量
 */
const evaluateAnswers = async (
  question: string,
  results: ModelResponse[]
): Promise<AnswerEvaluation[]> => {
  const response = await fetch('/api/v1/chat/evaluate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question, results })
  })
  
  return response.json()
}
```

### 7.3 任务分解

```typescript
interface TaskPlan {
  mainQuestion: string
  subQuestions: SubQuestion[]
  estimatedSteps: number
}

interface SubQuestion {
  id: number
  question: string
  description: string
  status: 'pending' | 'in_progress' | 'completed' | 'failed'
}

/**
 * 分解复杂问题为子问题
 */
const decomposeQuestion = async (question: string): Promise<TaskPlan> => {
  const response = await fetch('/api/v1/chat/decompose', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question })
  })
  
  return response.json()
}
```

### 7.4 SSE流式处理

```typescript
interface SSEChunk {
  type: 'chunk' | 'done' | 'error'
  model: string
  content?: string
  tokens?: number
  latency?: number
  error?: string
}

const handleStreamingResponse = async (
  response: Response,
  model: string
): Promise<ModelResponse> => {
  const reader = response.body?.getReader()
  const decoder = new TextDecoder()
  let answer = ''
  let tokens = 0
  let latency = 0
  
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    
    const chunk = decoder.decode(value)
    const lines = chunk.split('\n')
    
    for (const line of lines) {
      if (line.startsWith('data: ')) {
        try {
          const data: SSEChunk = JSON.parse(line.slice(6))
          
          if (data.type === 'chunk') {
            answer += data.content
            updateModelAnswer(model, answer, false)
          } else if (data.type === 'done') {
            tokens = data.tokens || 0
            latency = data.latency || 0
            return { model, success: true, answer, tokens, latency }
          } else if (data.type === 'error') {
            throw new Error(data.error)
          }
        } catch (e) {
          // 忽略解析错误
        }
      }
    }
  }
  
  return { model, success: true, answer, tokens, latency }
}
```

### 7.5 上下文自动管理

```typescript
/**
 * 上下文管理器（借鉴ContextEditingInterceptor）
 */
class ContextManager {
  private readonly COMPRESSION_THRESHOLD = 10000  // 10K tokens
  private readonly SUMMARIZATION_THRESHOLD = 120000 // 120K tokens
  private readonly LARGE_RESULT_THRESHOLD = 50000 // 50K chars
  
  /**
   * 检查是否需要压缩上下文
   */
  checkCompressionNeeded(context: ConversationContext): boolean {
    return context.tokens > this.COMPRESSION_THRESHOLD
  }
  
  /**
   * 自动压缩上下文
   */
  async compressContext(context: ConversationContext): Promise<ConversationContext> {
    const summary = await this.summarizeEarlyMessages(context.messages)
    return context.withCompressedHistory(summary)
  }
  
  /**
   * 大结果自动落盘（借鉴LargeResultEvictionInterceptor）
   */
  async dumpLargeResult(result: string): Promise<string> {
    if (result.length > this.LARGE_RESULT_THRESHOLD) {
      const fileId = `result_${Date.now()}.txt`
      await this.writeFile(fileId, result)
      return `[文件已保存: ${fileId}]`
    }
    return result
  }
}
```

### 7.6 提示词模板服务（本次新增）

```java
/**
 * 提示词模板服务
 */
@Service
public class PromptTemplateService {

    /**
     * 获取模板列表
     */
    public List<PromptTemplate> listTemplates(boolean includeDeleted) {
        // 查询模板列表
    }

    /**
     * 获取模板详情
     */
    public PromptTemplate getTemplate(Long id) {
        // 查询模板详情
    }

    /**
     * 创建模板
     */
    public PromptTemplate createTemplate(PromptTemplate template) {
        // 解析变量
        template.setVariables(parseVariables(template.getContent()));
        // 保存
    }

    /**
     * 更新模板
     */
    public PromptTemplate updateTemplate(Long id, PromptTemplate template) {
        template.setId(id);
        template.setVariables(parseVariables(template.getContent()));
        // 更新
    }

    /**
     * 删除模板
     */
    public void deleteTemplate(Long id) {
        // 软删除
    }

    /**
     * 设为默认模板
     */
    public void setDefaultTemplate(Long id) {
        // 取消其他默认，设为当前默认
    }

    /**
     * 渲染模板（变量替换）
     */
    public String renderTemplate(PromptTemplate template, Map<String, Object> variables) {
        String content = template.getContent();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            content = content.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return content;
    }

    /**
     * 解析模板中的变量
     */
    private List<String> parseVariables(String content) {
        // 匹配 {{variable}} 格式
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        // 返回变量列表
    }
}
```

### 7.7 快捷指令服务（本次新增）

```java
/**
 * 快捷指令服务
 */
@Service
public class QuickCommandService {

    /**
     * 获取指令列表
     */
    public List<QuickCommand> listCommands(boolean includeSystem) {
        // 查询指令列表
    }

    /**
     * 执行指令
     */
    public String executeCommand(Long commandId, Map<String, Object> context) {
        QuickCommand command = getCommand(commandId);
        String result = command.getCommand();
        
        // 变量替换
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        
        // 增加使用次数
        incrementUsageCount(commandId);
        
        return result;
    }

    /**
     * 创建指令
     */
    public QuickCommand createCommand(QuickCommand command) {
        // 保存
    }

    /**
     * 更新指令
     */
    public QuickCommand updateCommand(Long id, QuickCommand command) {
        command.setId(id);
        // 更新
    }

    /**
     * 删除指令
     */
    public void deleteCommand(Long id) {
        // 软删除
    }
}
```

---

## 八、UI布局设计

### 8.1 普通模式页面布局

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 顶部工具栏                                                              │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐  │
│ │ 模型选择 │ │ 知识库   │ │ MCP服务  │ │ 文件上传     │ │ 任务分解 │  │
│ │ [多选▼]  │ │ [选择▼]  │ │ [配置]   │ │ [+ 上传]     │ │ [展开▼]  │  │
│ └──────────┘ └──────────┘ └──────────┘ └──────────────┘ └──────────┘  │
├─────────────────────────────────────────────────────────────────────────┤
│ 消息区域                                                               │
│                                                                     │
│ 用户问题: 打架的处罚是什么？对比各地执行差异                          │
│                                                                     │
│ ┌─────────────────────┐  ┌─────────────────────┐  ┌────────────────┐ │
│ │ 🟢 通义千问 Plus    │  │ 🟢 GPT-4            │  │ 🟢 文心一言    │ │
│ │ ⭐⭐⭐⭐ 回答质量    │  │ ⭐⭐⭐ 回答质量      │  │ ⭐⭐⭐⭐ 回答质量│ │
│ │ [最佳回答]          │  │                     │  │                │ │
│ │ ───────────────    │  │ ───────────────     │  │ ────────────   │ │
│ │ 根据《治安管理...》 │  │ 根据相关法律...     │  │ 根据《治安...》│ │
│ └─────────────────────┘  └─────────────────────┘  └────────────────┘ │
│                                                                     │
│ ┌─────────────────────┐  ┌─────────────────────┐                    │
│ │ 🟢 Claude 3         │  │ 🟢 通义千问 Max     │                    │
│ │ ⭐⭐⭐⭐ 回答质量    │  │ ⭐⭐⭐ 回答质量      │                    │
│ │ ───────────────     │  │ ───────────────     │                    │
│ │ 根据《刑法》...     │  │ 打架行为涉及...     │                    │
│ └─────────────────────┘  └─────────────────────┘                    │
│                                                                     │
├─────────────────────────────────────────────────────────────────────────┤
│ 输入区域                                                              │
│ [文本输入框...] [+ 附件] [发送]                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

### 8.2 响应式Grid布局

```scss
.model-answers-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  
  // 大屏幕一行3个 (>= 1400px)
  @media (min-width: 1400px) {
    grid-template-columns: repeat(3, 1fr);
  }
  
  // 中等屏幕一行2个 (900-1399px)
  @media (min-width: 900px) and (max-width: 1399px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  // 小屏幕一行1个 (< 900px)
  @media (max-width: 899px) {
    grid-template-columns: 1fr;
  }
}
```

---

## 九、后端API设计

### 9.1 多模型并行对话API

```java
@PostMapping("/chat/normal")
public Result<MultiModelChatResponse> chatNormal(@RequestBody @Valid NormalChatRequest request) {
    // 验证模型数量（最多6个）
    if (request.getModels().size() > 6) {
        return Result.error("最多支持6个模型同时回答");
    }
    
    // 并行调用多个模型（含重试3次）
    List<CompletableFuture<ModelChatResult>> futures = request.getModels().stream()
        .map(modelId -> CompletableFuture.supplyAsync(() -> 
            callModelWithRetry(modelId, request, 3)
        ))
        .collect(Collectors.toList());
    
    // 等待所有模型返回
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // 收集结果
    List<ModelChatResult> results = futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    
    // 构建响应
    MultiModelChatResponse response = new MultiModelChatResponse();
    response.setConversationId(request.getConversationId());
    response.setQuestion(request.getQuestion());
    response.setResults(results);
    response.setTimestamp(LocalDateTime.now());
    
    return Result.success(response);
}

/**
 * 单模型调用（含重试）
 */
private ModelChatResult callModelWithRetry(String modelId, NormalChatRequest request, int maxRetries) {
    int attempt = 0;
    while (attempt < maxRetries) {
        try {
            return callModel(modelId, request);
        } catch (Exception e) {
            attempt++;
            if (attempt >= maxRetries) {
                ModelChatResult result = new ModelChatResult();
                result.setModel(modelId);
                result.setSuccess(false);
                result.setError("重试" + maxRetries + "次后失败: " + e.getMessage());
                return result;
            }
            try {
                Thread.sleep(1000 * attempt);  // 指数退避
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }
    }
    throw new RuntimeException("不可能到达这里");
}
```

### 9.2 文件解析API

```java
@PostMapping("/files/parse")
public Result<FileContent> parseFile(@RequestParam("file") MultipartFile file) {
    // 校验文件大小（最大10MB）
    if (file.getSize() > 10 * 1024 * 1024) {
        return Result.error("文件大小不能超过10MB");
    }
    
    // 校验文件类型
    String fileName = file.getOriginalFilename();
    String ext = getFileExtension(fileName).toLowerCase();
    Set<String> supportedTypes = Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt", "md");
    if (!supportedTypes.contains(ext)) {
        return Result.error("不支持的文件类型，仅支持: pdf, doc, docx, xls, xlsx, txt, md");
    }
    
    try {
        // 解析文件内容（统一后端处理）
        String content = fileParseService.parse(fileName, file.getBytes());
        
        FileContent result = new FileContent();
        result.setName(fileName);
        result.setContent(content);
        result.setSize(file.getSize());
        
        return Result.success(result);
        
    } catch (Exception e) {
        log.error("文件解析失败: {}", fileName, e);
        // 跳过文件，继续处理
        return Result.success(null);
    }
}
```

### 9.3 回答质量评估API

```java
@PostMapping("/chat/evaluate")
public Result<List<AnswerEvaluation>> evaluateAnswers(@RequestBody EvaluationRequest request) {
    // 调用评估服务
    List<AnswerEvaluation> evaluations = qualityEvaluationService.evaluate(
        request.getQuestion(),
        request.getResults()
    );
    
    // 标记最佳回答
    AnswerEvaluation best = evaluations.stream()
        .max(Comparator.comparingInt(AnswerEvaluation::getScore))
        .orElse(null);
    if (best != null) {
        best.setBestAnswer(true);
    }
    
    return Result.success(evaluations);
}
```

### 9.4 任务分解API

```java
@PostMapping("/chat/decompose")
public Result<TaskPlan> decomposeQuestion(@RequestBody DecomposeRequest request) {
    // 调用LLM分解问题
    TaskPlan taskPlan = taskDecompositionService.decompose(request.getQuestion());
    
    return Result.success(taskPlan);
}
```

### 9.5 提示词模板API（本次新增）

```java
/**
 * 提示词模板控制器
 */
@RestController
@RequestMapping("/api/v1/prompt-templates")
public class PromptTemplateController {

    @GetMapping
    public Result<List<PromptTemplate>> list(
            @RequestParam(required = false) Boolean includeDeleted) {
        return Result.success(promptTemplateService.listTemplates(includeDeleted));
    }

    @GetMapping("/{id}")
    public Result<PromptTemplate> get(@PathVariable Long id) {
        return Result.success(promptTemplateService.getTemplate(id));
    }

    @PostMapping
    public Result<PromptTemplate> create(@RequestBody PromptTemplate template) {
        return Result.success(promptTemplateService.createTemplate(template));
    }

    @PutMapping("/{id}")
    public Result<PromptTemplate> update(@PathVariable Long id, @RequestBody PromptTemplate template) {
        return Result.success(promptTemplateService.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        promptTemplateService.deleteTemplate(id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        promptTemplateService.setDefaultTemplate(id);
        return Result.success();
    }

    @PostMapping("/render")
    public Result<String> render(@RequestBody RenderRequest request) {
        PromptTemplate template = promptTemplateService.getTemplate(request.getTemplateId());
        String rendered = promptTemplateService.renderTemplate(template, request.getVariables());
        return Result.success(rendered);
    }
}
```

### 9.6 快捷指令API（本次新增）

```java
/**
 * 快捷指令控制器
 */
@RestController
@RequestMapping("/api/v1/quick-commands")
public class QuickCommandController {

    @GetMapping
    public Result<List<QuickCommand>> list(
            @RequestParam(required = false) Boolean includeSystem) {
        return Result.success(quickCommandService.listCommands(includeSystem));
    }

    @GetMapping("/{id}")
    public Result<QuickCommand> get(@PathVariable Long id) {
        return Result.success(quickCommandService.getCommand(id));
    }

    @PostMapping
    public Result<QuickCommand> create(@RequestBody QuickCommand command) {
        return Result.success(quickCommandService.createCommand(command));
    }

    @PutMapping("/{id}")
    public Result<QuickCommand> update(@PathVariable Long id, @RequestBody QuickCommand command) {
        return Result.success(quickCommandService.updateCommand(id, command));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        quickCommandService.deleteCommand(id);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    public Result<String> execute(@PathVariable Long id, @RequestBody Map<String, Object> context) {
        String result = quickCommandService.executeCommand(id, context);
        return Result.success(result);
    }

    @PostMapping("/{id}/usage")
    public Result<Void> incrementUsage(@PathVariable Long id) {
        quickCommandService.incrementUsageCount(id);
        return Result.success();
    }
}
```

---

## 十、数据库设计

> **建表规范**: 字符集 `utf8mb4`，排序规则 `utf8mb4_general_ci`

### 10.1 提示词模板表

```sql
-- 提示词模板表
CREATE TABLE IF NOT EXISTS `prompt_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `variables` JSON COMMENT '变量列表',
    `description` VARCHAR(500) COMMENT '模板描述',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认模板: 0-否 1-是',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统模板: 0-否 1-是（系统模板不可删除）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_by` BIGINT COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_is_default` (`is_default`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提示词模板表';
```

### 10.2 对话设置表

```sql
-- 对话设置表
CREATE TABLE IF NOT EXISTS `conversation_settings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `template_id` BIGINT COMMENT '使用的模板ID',
    `custom_prompt` TEXT COMMENT '自定义提示词（覆盖模板）',
    `temperature` DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '温度参数: 0-1之间',
    `max_tokens` INT NOT NULL DEFAULT 2000 COMMENT '最大生成tokens',
    `top_p` DECIMAL(3,2) NOT NULL DEFAULT 0.95 COMMENT 'topP参数: 0-1之间',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对话设置表';
```

### 10.3 MCP服务表

```sql
-- MCP服务表
CREATE TABLE IF NOT EXISTS `mcp_service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
    `code` VARCHAR(50) NOT NULL COMMENT '服务编码',
    `description` VARCHAR(500) COMMENT '服务描述',
    `enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用: 0-禁用 1-启用',
    `config_url` VARCHAR(500) COMMENT 'API URL',
    `config_auth_type` VARCHAR(20) NOT NULL DEFAULT 'api_key' COMMENT '认证方式: api_key-API Key bearer-Bearer Token oauth2-OAuth2',
    `config_credentials` VARCHAR(500) COMMENT '认证凭证',
    `config_timeout` INT NOT NULL DEFAULT 60 COMMENT '超时时间（秒）',
    `config_method` VARCHAR(10) NOT NULL DEFAULT 'POST' COMMENT '请求方法: GET/POST',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` BIGINT COMMENT '创建人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` BIGINT COMMENT '更新人',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MCP服务表';
```

### 10.4 MCP服务调用日志表

```sql
-- MCP服务调用日志表
CREATE TABLE IF NOT EXISTS `mcp_service_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `service_id` BIGINT NOT NULL COMMENT '服务ID',
    `request_data` TEXT COMMENT '请求数据',
    `response_data` TEXT COMMENT '响应数据',
    `latency` INT COMMENT '响应延迟（毫秒）',
    `status` VARCHAR(20) COMMENT '状态: success-成功 failed-失败',
    `error_message` TEXT COMMENT '错误信息',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='MCP服务调用日志表';
```

### 10.5 对话评估结果表

```sql
-- 对话评估结果表
CREATE TABLE IF NOT EXISTS `conversation_evaluation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评估ID',
    `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
    `message_id` BIGINT COMMENT '消息ID',
    `model` VARCHAR(50) NOT NULL COMMENT '模型标识',
    `score` INT COMMENT '评分: 0-100',
    `evaluation` JSON COMMENT '详细评估结果',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_model` (`model`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='对话评估结果表';
```

### 10.6 快捷指令表（本次新增）

```sql
-- 快捷指令表
CREATE TABLE IF NOT EXISTS `quick_command` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指令ID',
    `name` VARCHAR(100) NOT NULL COMMENT '指令名称',
    `command` VARCHAR(500) NOT NULL COMMENT '指令内容（支持变量替换）',
    `description` VARCHAR(500) COMMENT '指令描述',
    `icon` VARCHAR(100) COMMENT '图标',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '颜色',
    `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统指令: 0-否 1-是（系统指令不可删除）',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `created_by` BIGINT COMMENT '创建人',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` BIGINT COMMENT '更新人',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_sort` (`sort`),
    KEY `idx_usage_count` (`usage_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='快捷指令表';
```

### 10.7 初始化数据

```sql
-- 初始化默认提示词模板
INSERT INTO `prompt_template` (`name`, `content`, `variables`, `description`, `is_default`, `is_system`, `sort`, `created_by`) VALUES
(
    '默认模板',
    '你是公安专网知识库的智能助手，专注于法律法规和业务规范的精准解答。请根据参考文档，准确、完整地回答用户问题。

## 用户问题
{{question}}

## 回答要求
1. **准确性**: 严格基于参考文档内容回答，不要添加文档中没有的信息
2. **完整性**: 如果涉及多条法规或多项规定，请逐一列出
3. **清晰性**: 使用清晰的标题和小标题组织内容，重要条款用**加粗**强调
4. **引用**: 在回答末尾明确标注引用来源
5. **诚实**: 如果参考文档中没有明确答案，请明确告知',
    '["{{question}}", "{{kb_name}}", "{{user_name}}", "{{current_time}}"]',
    '公安知识库默认提示词模板',
    1,
    1,
    0,
    1
);
```

---

## 十一、文件类型支持

| 文件类型 | 扩展名 | 解析工具 | 优先级 |
|---------|-------|---------|--------|
| PDF | .pdf | Apache PDFBox | 高 |
| Word | .doc/.docx | Apache POI (mammoth) | 高 |
| Excel | .xls/.xlsx | Apache POI | 高 |
| Markdown | .md | 普通文本 + Markdown解析 | 中 |
| TXT | .txt | 普通文本 | 中 |

---

## 十二、最终工时汇总

| Phase | 任务 | 工时 |
|-------|------|------|
| **Phase 1** | 项目结构调整 | 2h |
| | 创建目录结构 | 0.5h |
| | 更新路由配置 | 0.5h |
| | Store状态更新 | 1h |
| **Phase 2** | 专业模式迁移 | 1h |
| | 重构现有chat/index.vue | 1h |
| **Phase 3** | 共享组件开发 | 4h |
| | ConversationList | 0.5h |
| | ChatMessages | 1h |
| | ChatInput | 0.5h |
| | Markdown渲染器 | 1h |
| | 工具函数抽取 | 1h |
| **Phase 4** | 普通模式页面 | **14h** |
| | 页面骨架 | 1h |
| | ModelSelector | 0.5h |
| | FileUploader | 1h |
| | KnowledgeSelector | 0.5h |
| | McpPanel | 2h |
| | ModelAnswerCard | 2.5h |
| | TaskPlanPanel | 1h |
| | 多模型并行请求 | 1.5h |
| | PromptTemplateDialog | 1.5h |  # 新增：提示词模板对话框 |
| | TemplateManager | 1h |     # 新增：模板管理 |
| | QuickCommands | 1.5h |    # 新增：快捷指令 |
| **Phase 5** | 后端API开发 | **10h** |
| | 多模型对话API | 2h |
| | 质量评估API | 1.5h |
| | 任务分解API | 1h |
| | MCP服务管理API | 2h |
| | 文件解析API | 1.5h |
| | 提示词模板API | 1h |  # 新增
| | 快捷指令API | 1h |     # 新增 |
| **Phase 6** | 类型定义更新 | 1.5h |
| | 新增DTO类型 | 1h |
| | 类型导出 | 0.5h |
| **Phase 7** | 测试和调试 | **4h** |
| | 单元测试 | 1.5h |
| | 集成测试 | 1.5h |
| | Bug修复 | 1h |
| **合计** | | **36.5h** |

---

## 十三、交付物清单

### 13.1 前端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `src/views/chat/professional/index.vue` | 新建 | 专业模式页面 |
| `src/views/chat/normal/index.vue` | 新建 | 普通模式页面 |
| `src/views/chat/normal/components/ModelSelector.vue` | 新建 | 模型选择器 |
| `src/views/chat/normal/components/FileUploader.vue` | 新建 | 文件上传器 |
| `src/views/chat/normal/components/KnowledgeSelector.vue` | 新建 | 知识库选择器 |
| `src/views/chat/normal/components/McpPanel.vue` | 新建 | MCP配置面板 |
| `src/views/chat/normal/components/ModelAnswerCard.vue` | 新建 | 模型回答卡片 |
| `src/views/chat/normal/components/TaskPlanPanel.vue` | 新建 | 任务分解面板 |
| `src/views/chat/normal/components/PromptTemplateDialog.vue` | 新建 | 提示词模板对话框（本次新增） |
| `src/views/chat/normal/components/TemplateManager.vue` | 新建 | 模板管理组件（本次新增） |
| `src/views/chat/normal/components/QuickCommands.vue` | 新建 | 快捷指令组件（本次新增） |
| `src/views/chat/normal/composables/useMultiModel.ts` | 新建 | 多模型并行请求 |
| `src/views/chat/normal/composables/useContextManager.ts` | 新建 | 上下文管理 |
| `src/views/chat/normal/composables/useQualityEvaluation.ts` | 新建 | 质量评估 |
| `src/views/chat/components/ConversationList.vue` | 新建 | 对话列表 |
| `src/views/chat/components/ChatMessages.vue` | 新建 | 消息展示 |
| `src/views/chat/components/ChatInput.vue` | 新建 | 输入组件 |
| `src/types/chat.ts` | 新建 | 类型定义 |
| `src/router/index.ts` | 修改 | 路由配置 |
| `src/store/modules/chat.ts` | 修改 | Store状态 |

### 13.2 后端文件

| 文件路径 | 类型 | 说明 |
|---------|------|------|
| `ChatController.java` | 修改 | 新增normal/analyze/evaluate/decompose接口 |
| `McpController.java` | 新建 | MCP服务API |
| `FileController.java` | 新建 | 文件解析API |
| `PromptTemplateController.java` | 新建 | 提示词模板API（本次新增） |
| `QuickCommandController.java` | 新建 | 快捷指令API（本次新增） |
| `McpService.java` | 新建 | MCP服务接口 |
| `McpServiceImpl.java` | 新建 | MCP服务实现 |
| `QualityEvaluationService.java` | 新建 | 质量评估服务 |
| `TaskDecompositionService.java` | 新建 | 任务分解服务 |
| `PromptTemplateService.java` | 新建 | 提示词模板服务（本次新增） |
| `QuickCommandService.java` | 新建 | 快捷指令服务（本次新增） |
| `NormalChatRequest.java` | 新建 | 普通模式请求DTO |
| `MultiModelChatResponse.java` | 新建 | 多模型响应DTO |
| `ModelResult.java` | 新建 | 单模型结果 |
| `AnswerEvaluation.java` | 新建 | 回答评估DTO |
| `TaskPlan.java` | 新建 | 任务分解DTO |
| `McpServiceEntity.java` | 新建 | MCP服务实体 |
| `McpConfigEntity.java` | 新建 | MCP配置实体 |
| `McpClientConfig.java` | 新建 | MCP客户端配置 |
| `sql/mcp_service_init.sql` | 新建 | MCP服务初始化SQL |

---

## 十四、风险和应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 后端API未就绪 | 普通模式无法完整测试 | 先实现前端界面，API Mock |
| SSE连接过多 | 浏览器限制 | 限制最多6个模型 |
| 文件解析失败 | 影响用户体验 | 跳过失败文件，显示警告 |
| 模型重试次数过多 | 响应时间过长 | 设置最大重试次数和超时 |
| 质量评估不准确 | 误导用户 | 作为参考，非绝对标准 |

---

## 十五、功能对比矩阵

| 功能 | 专业模式 | 普通模式 | 借鉴来源 |
|------|---------|---------|---------|
| 知识库问答 | ✅ | ✅ | - |
| 多模型并行 | ❌ | ✅ (最多6个) | - |
| 文件上传解析 | ❌ | ✅ (5种类型) | - |
| MCP服务配置 | ❌ | ✅ | DeepResearch |
| 回答质量评估 | ❌ | ✅ | Critique-Agent |
| 任务分解 | ❌ | ✅ | TodoListInterceptor |
| 上下文自动压缩 | ✅ | ✅ | ContextEditingInterceptor |
| 大结果自动落盘 | ✅ | ✅ | LargeResultEvictionInterceptor |
| 重试机制 | ✅ | ✅ (3次) | ToolRetryInterceptor |
| 流式输出 | ✅ | ✅ | - |

---

## 十六、确认清单

在开始实施前，请确认以下问题：

| # | 问题 | 确认答案 |
|---|------|---------|
| 1 | 对话历史是否完全独立 | ✅ 完全独立 |
| 2 | 支持哪些文件类型 | PDF/Word/Excel/TXT/Markdown |
| 3 | 最大文件大小限制 | 10MB |
| 4 | 最大同时请求模型数 | 6个 |
| 5 | MCP服务默认启用哪些 | 空配置 |
| 6 | 是否需要回答对比功能 | 后期 |
| 7 | 是否需要回答导出功能 | 后期 |
| 8 | 普通模式需要哪些角色权限 | 所有用户 |
| 9 | 文件解析失败如何处理 | 跳过文件，继续处理 |
| 10 | 单个模型失败如何处理 | 重试3次 |

---

## 十七、后续优化方向（后期）

1. **回答对比功能** - 高亮差异，生成对比表格
2. **回答导出功能** - 导出为Markdown/PDF/Word
3. **提示词模板** - 自定义系统提示词
4. **快捷指令** - 预设常用指令
5. **对话分享** - 生成分享链接
6. **数据分析** - 使用DeepResearch深度分析

---

## 项目建表规范

| 规范 | 示例 |
|------|------|
| 表名 | 小写+下划线 `prompt_template` |
| 字段名 | 反引号包裹 `` `id` `` |
| 字段注释 | COMMENT '注释内容' |
| 表注释 | COMMENT='提示词模板表' |
| 主键 | `` `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY `` |
| 时间 | `` `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' `` |
| 更新时间 | `` `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' `` |
| 删除标记 | `` `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除' `` |
| 索引 | KEY `idx_xxx` (`xxx`) |
| 字符集 | ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci |

---

## 十三、SQL文件清单

```
sql/
├── 001_sys_dict_backup.sql
├── 002_sys_dict_optimize.sql
├── 003_sys_dict_init.sql
├── 004_document_scope_fields.sql
├── 005_document_vectors_fields.sql
├── 006_document_kb_path.sql           # 级联查询迁移
├── 007_prompt_template.sql            # 新增：提示词模板和对话设置
├── 008_mcp_service.sql                # 新增：MCP服务相关
└── 009_conversation_evaluation.sql    # 新增：对话评估
```

---

## 十八、参考文档

- DeepResearch示例: https://java2ai.com/agents/deepresearch/quick-start
- Spring AI Alibaba: https://github.com/alibaba/spring-ai-alibaba
- MCP协议: https://modelcontextprotocol.io/
- OceanBase SeekDB: https://www.oceanbase.ai/docs/

---

## 十九、最终确认清单

### 19.1 基础配置确认

| # | 项目 | 确认值 |
|---|------|-------|
| 1 | 字符集 | utf8mb4 |
| 2 | 排序规则 | utf8mb4_general_ci |
| 3 | 对话历史 | 完全独立 |
| 4 | 文件类型 | PDF/Word/Excel/TXT/Markdown |
| 5 | 最大文件大小 | 10MB |
| 6 | 最大模型数 | 6个 |
| 7 | MCP服务默认 | 空配置 |
| 8 | 文件解析失败 | 跳过文件，继续处理 |
| 9 | 模型失败重试 | 3次 |
| 10 | 用户权限 | 所有用户 |

### 19.2 新增功能确认

| 功能 | 状态 | 说明 |
|------|------|------|
| 多模型并行 | ✅ | 最多6个，并行请求 |
| 文件上传 | ✅ | 5种类型，后端解析 |
| MCP配置 | ✅ | API Key/URL/超时/认证 |
| 回答质量评估 | ✅ | 多维度评分 |
| 任务分解 | ✅ | 子问题拆分 |
| 提示词模板 | ✅ | 对话级自定义（本次实施） |
| 快捷指令 | ✅ | 预设常用指令（本次实施） |
| 流式输出 | ✅ | SSE |
| 重试机制 | ✅ | 3次 |

### 19.3 实施准备

| 项目 | 状态 |
|------|------|
| 开发计划文档 | ✅ 已完成 |
| SQL建表脚本 | ✅ 已完成 |
| 前端目录结构 | 待实施 |
| 后端API设计 | 待实施 |
| 测试用例 | 待设计 |

---

**文档版本**: 2.0
**创建日期**: 2026-02-03
**最后更新**: 2026-02-03
**状态**: 待实施确认
