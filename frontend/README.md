# Police Knowledge Base System - Frontend

公安专网知识库系统前端

## 技术栈

### 核心框架
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.13 | 前端框架 |
| TypeScript | 5.7.3 | 类型安全 |
| Vite | 6.0.6 | 构建工具 |
| Vue Router | 4.5.0 | 路由管理 |
| Pinia | 3.0.2 | 状态管理 |

### UI组件
| 技术 | 版本 | 用途 |
|------|------|------|
| Element Plus | 2.9.3 | UI组件库 |
| Element Plus Icons | 2.3.1 | 图标库 |
| Sass | 1.85.1 | CSS预处理器 |

### 网络与工具
| 技术 | 版本 | 用途 |
|------|------|------|
| Axios | 1.8.2 | HTTP客户端 |
| Axios Retry | 4.5.0 | 请求重试 |
| Day.js | 1.11.13 | 日期处理 |
| Lodash-es | 4.17.21 | 工具库 |
| NProgress | 0.2.0 | 加载进度 |

### 文档渲染
| 技术 | 版本 | 用途 |
|------|------|------|
| Marked | 17.0.1 | Markdown解析 |
| Markdown-it | 14.1.0 | Markdown渲染 |
| Highlight.js | 11.11.1 | 代码高亮 |
| DOMPurify | 3.3.1 | HTML净化 |

### 开发工具
| 技术 | 用途 |
|------|------|
| unplugin-auto-import | 自动导入API |
| unplugin-vue-components | 自动注册组件 |
| Vue TSConfig | TypeScript配置 |
| ESLint | 代码检查 |
| Prettier | 代码格式化 |

---

## 项目结构

```
frontend/
├── public/                      # 静态资源
│   └── favicon.ico
│
├── src/
│   ├── api/                     # API接口定义
│   │   ├── auth/                # 认证相关
│   │   │   └── index.ts         # 登录/登出/用户信息
│   │   ├── user/               # 用户管理
│   │   │   └── index.ts
│   │   ├── kb/                 # 知识库API
│   │   │   └── index.ts
│   │   ├── doc/                # 文档API
│   │   │   └── index.ts
│   │   ├── search.ts            # 搜索API
│   │   ├── dict.ts             # 字典API
│   │   └── conversation.ts     # 对话API
│   │
│   ├── assets/                 # 资源文件
│   │   └── styles/             # 样式文件
│   │       ├── index.scss
│   │       └── variables.scss
│   │
│   ├── components/              # 公共组件（自动导入）
│   │   └── ...
│   │
│   ├── layout/                 # 布局组件
│   │   └── index.vue           # 主布局
│   │
│   ├── router/                 # 路由配置
│   │   └── index.ts            # 路由定义、守卫
│   │
│   ├── store/                  # Pinia状态管理
│   │   ├── index.ts            # Store配置
│   │   └── modules/
│   │       ├── user.ts         # 用户状态
│   │       ├── permission.ts   # 权限状态
│   │       └── chat.ts         # 对话状态
│   │
│   ├── types/                  # TypeScript类型定义
│   │   ├── api.ts              # API类型
│   │   └── global.d.ts         # 全局类型
│   │
│   ├── utils/                  # 工具函数
│   │   ├── request.ts          # Axios封装
│   │   └── markdown.ts         # Markdown渲染
│   │
│   ├── views/                  # 页面视图
│   │   ├── 404/                # 404页面
│   │   ├── chat/               # 智能问答
│   │   │   └── normal/
│   │   │       ├── index.vue   # 对话主界面
│   │   │       └── components/
│   │   │           ├── ModelSelector.vue      # 模型选择器
│   │   │           ├── FileUploader.vue      # 文件上传
│   │   │           ├── McpPanel.vue         # MCP服务面板
│   │   │           ├── PromptTemplateDialog.vue # 模板选择
│   │   │           ├── QuickCommands.vue     # 快捷指令
│   │   │           └── TemplateManager.vue   # 模板管理
│   │   ├── dashboard/           # 仪表盘
│   │   │   └── index.vue        # 首页统计
│   │   ├── dict/                # 字典管理
│   │   │   ├── index.vue        # 字典列表
│   │   │   └── components/
│   │   │       ├── DictDataDialog.vue      # 字典数据对话框
│   │   │       ├── DictTypeDialog.vue      # 字典类型对话框
│   │   │       └── ImportDialog.vue        # 导入对话框
│   │   ├── doc/                 # 文档管理
│   │   │   └── list/
│   │   │       ├── index.vue    # 文档列表
│   │   │       └── components/
│   │   │           ├── DocDialog.vue        # 文档编辑对话框
│   │   │           └── DocDetailDialog.vue # 文档详情对话框
│   │   ├── kb/                  # 知识库管理
│   │   │   └── list/
│   │   │       ├── index.vue    # 知识库列表
│   │   │       └── components/
│   │   │           └── KbDialog.vue         # 知识库对话框
│   │   ├── login/               # 登录页
│   │   │   └── index.vue
│   │   ├── search/              # 智能搜索
│   │   │   └── index.vue       # 搜索界面
│   │   ├── system/              # 系统管理
│   │   │   └── user/
│   │   │       ├── index.vue    # 用户列表
│   │   │       └── components/
│   │   │           └── UserDialog.vue       # 用户对话框
│   │   └── vector/              # 向量管理
│   │       └── stats.vue        # 向量统计
│   │
│   ├── App.vue                  # 根组件
│   ├── main.ts                  # 入口文件
│   ├── env.d.ts                 # 环境类型
│   ├── vite-env.d.ts            # Vite类型
│   ├── auto-imports.d.ts        # 自动导入声明
│   └── components.d.ts           # 组件声明
│
├── .env                          # 环境变量
├── .env.production               # 生产环境配置
├── .gitignore
├── index.html                    # HTML模板
├── package.json
├── package-lock.json
├── tsconfig.json                 # TypeScript配置
├── tsconfig.node.json
└── vite.config.ts              # Vite配置
```

---

## 核心功能模块

### 1. 智能问答（/chat/normal）

#### 功能描述
- **多模型并行响应**：支持同时调用多个AI模型进行响应
- **文件上传**：支持PDF、Word、Excel、TXT、Markdown文件上传
- **MCP服务集成**：支持模型上下文协议服务
- **对话模板**：内置常用提示词模板
- **快捷指令**：一键触发常用操作
- **流式响应**：实时显示AI生成内容

#### 支持的AI模型（8个）
| 模型ID | 名称 | 提供商 | 描述 |
|--------|------|--------|------|
| deepseek-reasoner | DeepSeek Reasoner | DeepSeek | 深度求索思考对话模型 |
| deepseek-chat | DeepSeek Chat | DeepSeek | 擅长多轮交互与内容生成 |
| claude-3-opus | Claude 3 Opus | Anthropic | 长上下文理解能力强 |
| claude-3-sonnet | Claude 3 Sonnet | Anthropic | 平衡性能与成本 |
| ERNIE-4.5 | 文心一言 4.5 | 百度 | 中文理解能力强 |
| ERNIE-4 | 文心一言 4 | 百度 | 快速中文响应 |
| Spark | 讯飞星火 | 科大讯飞 | 多模态交互能力 |
| GLM-4 | GLM-4 | 智谱AI | 中英双语优化 |

#### 文件上传功能
| 特性 | 说明 |
|------|------|
| 支持格式 | PDF, Word(.doc/.docx), Excel(.xls/.xlsx), TXT, Markdown |
| 最大文件大小 | 100MB |
| 最大文件数 | 5个/条消息 |
| 上传端点 | `/v1/documents/parse-and-upload` |

#### SSE流式通信
```typescript
// POST请求发送消息
POST /api/v1/chat/normal/{conversationId}
Content-Type: application/json

{
  "question": "用户问题",
  "models": "deepseek-chat,deepseek-reasoner",
  "files": [
    {"name": "文件名.docx", "content": "解析后的文本内容"}
  ]
}

// SSE事件类型
event: chunk     // 内容块
event: error     // 错误事件
event: done     // 单模型完成
event: allDone  // 所有模型完成
event: heartbeat // 心跳保活
```

#### 界面组件
```
┌─────────────────────────────────────────────────────────────────┐
│ [≡] 普通模式                                                    │
│ 模型选择 [DeepSeek Chat ▼] [+]                    [MCP] [文件] │
├─────────────────────────────────────────────────────────────────┤
│ ┌───────────────────┐ ┌───────────────────────────────────────┐ │
│ │                   │ │                                       │ │
│ │  • 新对话1        │ │  [用户] 请总结这份文档                │ │
│ │  • 新对话2        │ │                                       │ │
│ │  • 新对话3        │ │  ┌─────────────────────────────────┐ │ │
│ │                   │ │  │ DeepSeek Chat                    │ │ │
│ │                   │ │  │ 这份文档是一份面向公安系统的AI...  │ │ │
│ │                   │ │  └─────────────────────────────────┘ │ │
│ └───────────────────┘ └───────────────────────────────────────┘ │
│                                                                 │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 已上传文件 (1): [文档.pdf ×]                               │ │
│ │ [发送消息...]                                    [↑] [📄] [⚡]│ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

#### 组件说明
| 组件 | 功能 |
|------|------|
| ModelSelector | 多模型选择器，最多选择6个 |
| FileUploader | 文件上传面板，支持拖拽 |
| McpPanel | MCP服务管理面板 |
| PromptTemplateDialog | 提示词模板选择对话框 |
| QuickCommands | 快捷指令面板 |
| TemplateManager | 模板CRUD管理 |

---

### 2. 智能搜索（/search）

#### 功能描述
- 关键词搜索
- 知识库筛选
- 语义搜索（向量检索）
- 混合搜索（语义+关键词）

#### API端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/v1/search/semantic` | 语义搜索 |
| POST | `/v1/search/hybrid` | 混合搜索 |

---

### 3. 文档管理（/doc）

#### 功能描述
- 文档列表展示（分页）
- 知识库级联筛选
- 文档CRUD操作
- 文件上传
- 热门/最近文档

#### API端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/documents` | 文档列表（分页） |
| GET | `/v1/documents/:id` | 获取文档详情 |
| POST | `/v1/documents` | 创建文档 |
| PUT | `/v1/documents/:id` | 更新文档 |
| DELETE | `/v1/documents/:id` | 删除文档 |
| POST | `/v1/documents/parse-and-upload` | 解析并上传文件 |
| POST | `/v1/documents/create-with-file` | 创建文档（带文件） |
| GET | `/v1/documents/hot` | 热门文档 |
| GET | `/v1/documents/recent` | 最近文档 |

---

### 4. 知识库管理（/kb）

#### 功能描述
- 知识库树形展示
- 层级CRUD
- 文档统计
- 状态管理

#### API端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/kb` | 知识库列表（分页） |
| GET | `/v1/kb/list` | 所有知识库 |
| GET | `/v1/kb/:id` | 获取知识库 |
| POST | `/v1/kb` | 创建知识库 |
| PUT | `/v1/kb/:id` | 更新知识库 |
| DELETE | `/v1/kb/:id` | 删除知识库 |
| PUT | `/v1/kb/:id/status` | 更新状态 |

---

### 5. 用户管理（/system/user）

#### 功能描述
- 用户列表（分页）
- 用户CRUD
- 角色分配
- 状态管理
- 密码重置

#### API端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/users` | 用户列表 |
| GET | `/v1/users/:id` | 获取用户 |
| POST | `/v1/users` | 创建用户 |
| PUT | `/v1/users/:id` | 更新用户 |
| DELETE | `/v1/users/:id` | 删除用户 |
| PUT | `/v1/users/:id/status` | 更新状态 |
| PUT | `/v1/users/:id/reset-password` | 重置密码 |

---

### 6. 字典管理（/system/dict）

#### 功能描述
- 字典类型管理
- 字典数据管理
- 层级结构
- 批量导入导出

#### API端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/v1/dict/types` | 字典类型列表 |
| GET | `/v1/dict` | 字典分页 |
| GET | `/v1/dict/tree` | 字典树 |
| POST | `/v1/dict` | 创建字典数据 |
| PUT | `/v1/dict/:id` | 更新字典数据 |
| DELETE | `/v1/dict/:id` | 删除字典数据 |
| POST | `/v1/dict/import` | 导入字典 |
| GET | `/v1/dict/export` | 导出字典 |
| GET | `/v1/dict/kb-category/tree` | 知识库分类树 |

---

## 状态管理

### Chat Store（对话状态）
```typescript
// State
interface ChatState {
  conversations: Conversation[]           // 所有对话
  currentConversationId: number | null  // 当前对话ID
  messages: Record<number, ChatMessage[]> // 消息缓存
  chatMode: 'normal' | 'professional' // 聊天模式
  selectedKbId: number | null          // 选中的知识库ID
  lastVisitConversationId: number | null // 上次访问的对话
  attachedFiles: ChatFile[]            // 附件文件
  professionalConversations: Conversation[] // 专业模式对话
  normalConversations: Conversation[]     // 普通模式对话
  currentLoadMode: string | null      // 当前加载模式
}

// Actions
setCurrentLoadMode(mode)               // 设置加载模式
setConversations(convs)                // 加载对话列表
addConversation(conv)                  // 添加对话
removeConversation(id)                 // 删除对话
setCurrentConversationId(id)           // 切换对话
setMessages(id, msgs)                 // 加载消息
addMessage(conversationId, msg)       // 添加消息
appendAssistantContent()               // 流式追加内容
updateMessageReferences()              // 更新引用信息
setChatMode(mode)                     // 设置聊天模式
clearAttachedFiles()                  // 清空附件
addAttachedFile() / removeAttachedFile() // 管理附件
```

### User Store（用户状态）
```typescript
// State
interface UserState {
  token: string                        // JWT令牌
  userInfo: UserInfo | null           // 用户信息
  roles: string[]                    // 角色列表
  permissions: Set<string>           // 权限集合
}

// Actions
loginAction()                         // 登录
logoutAction()                       // 登出
hasRole(role)                        // 角色检查
hasPermission(permission)            // 权限检查
```

### Permission Store（权限状态）
```typescript
// State
interface PermissionState {
  routes: RouteRecordRaw[]           // 所有路由
  accessedRoutes: RouteRecordRaw[]   // 有权限的路由
}

// Actions
generateRoutes(roles)                 // 根据角色生成路由
```

---

## 认证与权限

### 登录流程
```
登录页 → 输入账号密码 → POST /api/v1/auth/login → 获取Token → 保存至Pinia
                                                                    ↓
                                                          自动跳转至首页
```

### 权限控制
```typescript
// 路由元信息
{
  path: 'system/dict',
  meta: { title: '字典管理', roles: ['ADMIN'] }
}

// 权限判断
const hasPermission = (permission: string) => {
  return userStore.permissions.has(permission)
}
```

### Token管理
- 存储位置: localStorage (`Admin-Token`)
- 请求头: `Authorization: Bearer <token>`
- 过期处理: 401自动跳转登录页

---

## API接口封装

### 请求配置（request.ts）
```typescript
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(config => {
  const token = userStore.token
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
service.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      // 跳转登录
      router.push('/login')
    }
    return Promise.reject(error)
  }
)
```

---

## 路由配置

### 路由表
| 路径 | 组件 | 名称 | 角色 |
|------|------|------|------|
| /login | Login | 登录 | 无 |
| /dashboard | Dashboard | 仪表盘 | 所有 |
| /kb/list | KbList | 知识库列表 | ADMIN/MANAGER |
| /kb/docs | DocList | 文档管理 | ADMIN/MANAGER |
| /search | Search | 智能搜索 | 所有 |
| /chat/normal | Chat | 智能问答(普通) | 所有 |
| /chat/professional | Chat | 智能问答(专业) | 所有 |
| /vector/stats | VectorStats | 向量统计 | 所有 |
| /system/user | User | 用户管理 | ADMIN/MANAGER |
| /system/dict | Dict | 字典管理 | ADMIN |

---

## 环境配置

### 开发环境
```bash
npm run dev
# 访问: http://localhost:3000
# API代理: /api → http://localhost:8080
```

### 环境变量
```bash
# .env
VITE_API_BASE_URL=/api
```

### 构建命令
```bash
# 开发构建
npm run dev

# 生产构建
npm run build

# 预览构建结果
npm run preview

# 代码检查
npm run lint

# 代码格式化
npm run format
```

---

## 部署方案

### 方式一：静态部署（Nginx）
```nginx
server {
    listen 80;
    server_name kb.example.com;
    root /var/www/police-kb/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 方式二：Docker部署
```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## 常见问题

### Q1: 页面空白
**解决**: 检查控制台错误，确认API服务是否启动

### Q2: 登录后无反应
**解决**: 检查Token是否正确存储，清除缓存后重试

### Q3: 接口401报错
**解决**: Token过期，重新登录

### Q4: 文件上传失败
**解决**: 检查文件大小是否超过100MB，格式是否支持

### Q5: SSE连接断开
**解决**: 检查网络连接，后端服务是否正常运行

---

## 参考资料

- [Vue 3文档](https://vuejs.org/)
- [Element Plus文档](https://element-plus.org/)
- [Pinia文档](https://pinia.vuejs.org/)
- [Vue Router文档](https://router.vuejs.org/)
- [Vite文档](https://vitejs.dev/)
- [TypeScript文档](https://www.typescriptlang.org/)
- [Axios文档](https://axios-http.com/)

---

## License

MIT License

---

## 2026-02-06 MCP服务管理与提示模板功能

### 一、MCP服务管理

#### 1.1 功能概述

MCP（Model Context Protocol）服务管理模块提供MCP服务的CRUD功能，支持通过HTTP或进程启动模式集成外部MCP服务。

#### 1.2 新增文件

| 文件路径 | 说明 |
| -------- | ---- |
| `api/mcpService.ts` | MCP服务API客户端 |
| `views/mcp-service/index.vue` | MCP服务管理页面 |

#### 1.3 API函数

```typescript
// api/mcpService.ts

export interface McpService {
  id: number
  name: string
  code: string
  description: string
  enabled: number
  configUrl: string
  configAuthType: string
  configTimeout: number
  configMethod: string
  command: string
  args: string
  env: string
  sort: number
  createdTime: string
}

export interface CreateMcpServiceRequest {
  name: string
  code: string
  description?: string
  enabled?: number
  configUrl?: string
  configAuthType?: string
  configCredentials?: string
  configTimeout?: number
  configMethod?: string
  sort?: number
  command?: string
  args?: string
  env?: string
}

export function getMcpServices()
export function getMcpService(id: number)
export function createMcpService(data: CreateMcpServiceRequest)
export function updateMcpService(id: number, data: CreateMcpServiceRequest)
export function deleteMcpService(id: number)
export function toggleMcpService(id: number)
```

#### 1.4 界面功能

**MCP服务管理页面** (`/system/mcp`)：
- 服务列表展示（表格）
- 添加/编辑MCP服务对话框
- 启用/禁用状态切换
- 删除服务确认
- 支持HTTP模式和进程启动模式

**表单字段**：
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| 服务名称 | 必填 | string |
| 服务编码 | 必填 | string（唯一） |
| 描述 | 选填 | string |
| API URL | HTTP模式 | string |
| 认证方式 | 选填 | api_key/bearer/oauth2 |
| 认证凭证 | 选填 | password类型 |
| 请求方法 | 选填 | GET/POST |
| 超时时间 | 选填 | number（秒） |
| 排序 | 选填 | number |
| 状态 | 选填 | switch |
| 启动命令 | 进程模式 | string |
| 命令参数 | 进程模式 | string |
| 环境变量 | 进程模式 | string |

#### 1.5 路由配置

```typescript
// router/index.ts
{
  path: '/system',
  meta: { title: '系统管理', icon: 'Setting' },
  children: [
    {
      path: 'mcp',
      name: 'McpService',
      component: () => import('@/views/mcp-service/index.vue'),
      meta: { title: 'MCP服务管理', icon: 'Connection' }
    }
  ]
}
```

---

### 二、提示模板管理

#### 2.1 功能概述

提示模板管理模块支持创建、管理和使用提示词模板，支持三种模板类型：
- **我的模板**：用户自己创建的私有模板
- **系统模板**：系统预置的模板（只读）
- **共享模板**：共享给其他用户的模板

#### 2.2 新增文件

| 文件路径 | 说明 |
| -------- | ---- |
| `api/promptTemplate.ts` | 提示模板API客户端 |
| `views/prompt-template/index.vue` | 提示模板管理页面 |
| `views/prompt-template/components/TemplateFormDialog.vue` | 模板表单对话框 |
| `views/prompt-template/components/TemplateList.vue` | 模板列表组件 |

#### 2.3 API函数

```typescript
// api/promptTemplate.ts

export interface PromptTemplate {
  id: number
  name: string
  content: string
  variables: string
  description: string
  isDefault: number
  isSystem: number
  sort: number
  createdBy?: number
  createdTime?: string
  updatedBy?: number
  updatedTime?: string
}

export enum TemplateType {
  SYSTEM = 'system',
  MY = 'my',
  SHARED = 'shared'
}

export interface CreateTemplateRequest {
  name: string
  content: string
  variables?: string
  description?: string
  sort?: number
}

export function getTemplates(type: TemplateType = TemplateType.MY)
export function getTemplate(id: number)
export function createTemplate(data: CreateTemplateRequest, isSystem?: 0 | 1 | 2)
export function createSharedTemplate(data: CreateTemplateRequest)
export function updateTemplate(id: number, data: CreateTemplateRequest)
export function deleteTemplate(id: number)
export function setDefault(id: number)
```

#### 2.4 界面功能

**提示模板管理页面** (`/system/prompt-template`)：
- 三个标签页切换：我的模板 / 系统模板 / 共享模板
- 创建模板（支持"共享给其他用户"选项）
- 编辑/删除模板（系统模板只读）
- 复制模板到个人模板
- 设为默认模板
- 模板变量自动识别和显示

**模板表单对话框**：
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| 模板名称 | 必填 | string（1-100字符） |
| 模板内容 | 必填 | textarea，支持{{变量名}} |
| 模板描述 | 选填 | textarea |
| 排序 | 选填 | number（0-999） |
| 共享给其他用户 | 选填 | checkbox（仅创建时显示） |

**可用变量标签**：
系统自动识别模板内容中的 `{{变量名}}` 格式并显示为标签，支持预定义变量：
- `{{content}}` - 文档内容或用户输入
- `{{question}}` - 用户问题
- `{{language}}` - 语言
- `{{username}}` - 用户名
- `{{date}}` - 当前日期

#### 2.5 isSystem字段说明

| 值 | 类型 | 说明 |
| -- | ---- | ---- |
| 0 | 我的模板 | 当前用户创建的私有模板 |
| 1 | 系统模板 | 系统预置的模板（只读） |
| 2 | 共享模板 | 共享给其他用户的模板 |

#### 2.6 路由配置

```typescript
// router/index.ts
{
  path: '/system',
  meta: { title: '系统管理', icon: 'Setting' },
  children: [
    {
      path: 'prompt-template',
      name: 'PromptTemplate',
      component: () => import('@/views/prompt-template/index.vue'),
      meta: { title: '提示模板管理', icon: 'DocumentCopy' }
    }
  ]
}
```

#### 2.7 使用示例

```vue
<template>
  <el-button @click="showCreateDialog">新建模板</el-button>
  
  <TemplateFormDialog
    v-model:visible="dialogVisible"
    :template="currentTemplate"
    :mode="dialogMode"
    @submit="loadTemplates"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TemplateFormDialog from './components/TemplateFormDialog.vue'
import { getTemplates, createTemplate, TemplateType } from '@/api/promptTemplate'

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentTemplate = ref(null)

const showCreateDialog = () => {
  currentTemplate.value = null
  dialogMode.value = 'create'
  dialogVisible.value = true
}
</script>
```

---

### 三、菜单结构

```
系统管理
├── 用户管理              /system/user
├── 字典管理              /system/dict
├── MCP服务管理           /system/mcp          (Connection图标)
└── 提示模板管理          /system/prompt-template  (DocumentCopy图标)
```

---

### 四、数据库表结构

```sql
-- MCP服务表
CREATE TABLE `mcp_service` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `code` varchar(50) NOT NULL,
  `description` varchar(500),
  `enabled` tinyint(4) DEFAULT 1,
  `config_url` varchar(500),
  `config_auth_type` varchar(20) DEFAULT 'api_key',
  `config_credentials` varchar(500),
  `config_timeout` int(11) DEFAULT 60,
  `config_method` varchar(10) DEFAULT 'POST',
  `command` varchar(500),
  `args` text,
  `env` text,
  `sort` int(11) DEFAULT 0,
  `created_by` bigint(20),
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
);

-- 提示模板表
CREATE TABLE `prompt_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `variables` json,
  `description` varchar(500),
  `is_default` tinyint(4) DEFAULT 0,
  `is_system` tinyint(4) DEFAULT 0,
  `sort` int(11) DEFAULT 0,
  `created_by` bigint(20),
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`id`)
);
```

