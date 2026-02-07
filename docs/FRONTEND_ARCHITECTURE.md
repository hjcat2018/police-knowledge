# 前端架构设计文档

> **文档版本**: 2.0.0  
> **最后更新**: 2025年1月20日  
> **技术栈**: Vue 3.5+ | TypeScript 5.x | Vite 6.x | Element Plus 2.x | Pinia 2.x

---

## 一、技术选型

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 6.x | 构建工具 |
| Element Plus | 2.x | UI组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP客户端 |
| ECharts | 5.x | 图表统计 |
| @vueuse/core | 11.x | 工具函数 |
| sass | 1.77.x | CSS预处理器 |

---

## 二、项目结构

```
frontend/
├── public/                          # 静态资源
│   ├── favicon.ico
│   └── robots.txt
├── src/
│   ├── api/                         # API接口定义
│   │   ├── index.ts                 # API入口
│   │   ├── auth.ts                  # 认证相关
│   │   │   ├── login.ts             # 登录
│   │   │   ├── logout.ts            # 登出
│   │   │   ├── info.ts              # 用户信息
│   │   │   └── refresh.ts           # Token刷新
│   │   ├── knowledge.ts             # 知识库API
│   │   │   ├── list.ts              # 知识库列表
│   │   │   ├── create.ts            # 创建知识库
│   │   │   ├── update.ts            # 更新知识库
│   │   │   └── delete.ts            # 删除知识库
│   │   ├── document.ts              # 文档API
│   │   │   ├── upload.ts            # 上传文档
│   │   │   ├── list.ts              # 文档列表
│   │   │   ├── detail.ts            # 文档详情
│   │   │   ├── delete.ts            # 删除文档
│   │   │   └── preview.ts           # 文档预览
│   │   ├── chat.ts                  # 问答API
│   │   │   ├── send.ts              # 发送消息
│   │   │   ├── stream.ts            # 流式对话
│   │   │   ├── history.ts           # 会话历史
│   │   │   └── feedback.ts          # 问答反馈
│   │   ├── search.ts                # 搜索API
│   │   │   ├── search.ts            # 混合搜索
│   │   │   ├── hot.ts               # 热门关键词
│   │   │   └── suggest.ts           # 搜索建议
│   │   ├── admin.ts                 # 管理API
│   │   │   ├── audit.ts             # 审核管理
│   │   │   ├── statistics.ts        # 统计分析
│   │   │   └── users.ts             # 用户管理
│   │   └── monitor.ts               # 监控API
│   │       ├── sync.ts              # 同步状态
│   │       ├── health.ts            # 健康检查
│   │       └── metrics.ts           # 监控指标
│   │
│   ├── assets/                      # 资源文件
│   │   ├── styles/
│   │   │   ├── _variables.scss      # 样式变量
│   │   │   ├── _mixins.scss         # 样式混入
│   │   │   ├── _functions.scss      # 样式函数
│   │   │   └── global.scss          # 全局样式
│   │   └── images/                  # 图片资源
│   │       ├── logo.svg
│   │       └── icons/
│   │
│   ├── components/                  # 公共组件
│   │   ├── common/                  # 通用组件
│   │   │   ├── Pagination/
│   │   │   │   ├── index.vue        # 分页组件
│   │   │   │   └── index.ts
│   │   │   ├── SearchBar/
│   │   │   │   ├── index.vue        # 搜索栏
│   │   │   │   └── index.ts
│   │   │   ├── UploadDialog/
│   │   │   │   ├── index.vue        # 上传弹窗
│   │   │   │   └── index.ts
│   │   │   ├── FileTypeIcon/
│   │   │   │   ├── index.vue        # 文件类型图标
│   │   │   │   └── index.ts
│   │   │   └── EmptyState/
│   │   │       ├── index.vue        # 空状态
│   │   │       └── index.ts
│   │   ├── chat/                    # 聊天组件
│   │   │   ├── ChatInput/
│   │   │   │   ├── index.vue        # 聊天输入框
│   │   │   │   └── index.ts
│   │   │   ├── ChatMessage/
│   │   │   │   ├── index.vue        # 消息气泡
│   │   │   │   └── index.ts
│   │   │   ├── ChatWindow/
│   │   │   │   ├── index.vue        # 聊天窗口
│   │   │   │   └── index.ts
│   │   │   ├── ChatHistory/
│   │   │   │   ├── index.vue        # 历史会话
│   │   │   │   └── index.ts
│   │   │   └── SourceCard/
│   │   │       ├── index.vue        # 来源卡片
│   │   │       └── index.ts
│   │   ├── document/                # 文档组件
│   │   │   ├── DocumentCard/
│   │   │   │   ├── index.vue        # 文档卡片
│   │   │   │   └── index.ts
│   │   │   ├── DocumentPreview/
│   │   │   │   ├── index.vue        # 文档预览
│   │   │   │   └── index.ts
│   │   │   ├── DocumentList/
│   │   │   │   ├── index.vue        # 文档列表
│   │   │   │   └── index.ts
│   │   │   └── CategoryTree/
│   │   │       ├── index.vue        # 分类树
│   │   │       └── index.ts
│   │   ├── statistics/              # 统计组件
│   │   │   ├── HotKeywords/
│   │   │   │   ├── index.vue        # 热门关键词
│   │   │   │   └── index.ts
│   │   │   ├── UserChart/
│   │   │   │   ├── index.vue        # 用户图表
│   │   │   │   └── index.ts
│   │   │   └── QueryTrend/
│   │   │       ├── index.vue        # 查询趋势
│   │   │       └── index.ts
│   │   └── layout/                  # 布局组件
│   │       ├── Header/
│   │       │   ├── index.vue        # 顶部导航
│   │       │   └── index.ts
│   │       ├── Sidebar/
│   │       │   ├── index.vue        # 侧边菜单
│   │       │   ├── index.ts
│   │       │   └── menu.ts          # 菜单配置
│   │       ├── Breadcrumb/
│   │       │   ├── index.vue        # 面包屑
│   │       │   └── index.ts
│   │       └── TagsView/
│   │           ├── index.vue        # 标签页
│   │           └── index.ts
│   │
│   ├── composables/                 # 组合式函数
│   │   ├── useAuth.ts               # 认证逻辑
│   │   ├── useChat.ts               # 聊天逻辑
│   │   ├── useSearch.ts             # 搜索逻辑
│   │   ├── useWebSocket.ts          # WebSocket封装
│   │   ├── useUpload.ts             # 上传逻辑
│   │   ├── usePermission.ts         # 权限逻辑
│   │   └── useForm.ts               # 表单逻辑
│   │
│   ├── hooks/                       # 自定义Hooks
│   │   ├── useDebounce.ts           # 防抖
│   │   ├── useThrottle.ts           # 节流
│   │   ├── useLocalStorage.ts       # 本地存储
│   │   └── useClipboard.ts          # 剪贴板
│   │
│   ├── layout/                      # 布局组件
│   │   ├── index.vue                # 主布局
│   │   ├── Main.vue                 # 主内容区
│   │   └── Layout.vue               # 布局容器
│   │
│   ├── router/                      # 路由配置
│   │   ├── index.ts                 # 路由入口
│   │   ├── routes.ts                # 路由定义
│   │   ├── guard.ts                 # 路由守卫
│   │   └── asyncRoutes.ts           # 动态路由
│   │
│   ├── store/                       # 状态管理
│   │   ├── index.ts                 # Store入口
│   │   ├── modules/
│   │   │   ├── user.ts              # 用户状态
│   │   │   │   ├── state.ts         # 状态
│   │   │   │   ├── getters.ts       # 计算属性
│   │   │   │   └── actions.ts       # 方法
│   │   │   ├── knowledge.ts         # 知识库状态
│   │   │   │   ├── state.ts
│   │   │   │   └── actions.ts
│   │   │   ├── chat.ts              # 聊天状态
│   │   │   │   ├── state.ts
│   │   │   │   └── actions.ts
│   │   │   ├── search.ts            # 搜索状态
│   │   │   │   ├── state.ts
│   │   │   │   └── actions.ts
│   │   │   ├── app.ts               # 应用状态
│   │   │   │   ├── state.ts
│   │   │   │   └── actions.ts
│   │   │   └── tags.ts              # 标签页状态
│   │   │       ├── state.ts
│   │   │       └── actions.ts
│   │   └── plugins/
│   │       ├── persist.ts           # 持久化插件
│   │       └── logger.ts            # 日志插件
│   │
│   ├── types/                       # TypeScript类型定义
│   │   ├── api.d.ts                 # API响应类型
│   │   ├── user.d.ts                # 用户类型
│   │   ├── knowledge.d.ts           # 知识库类型
│   │   ├── document.d.ts            # 文档类型
│   │   ├── chat.d.ts                # 聊天类型
│   │   ├── search.d.ts              # 搜索类型
│   │   ├── admin.d.ts               # 管理类型
│   │   └── common.d.ts              # 公共类型
│   │
│   ├── utils/                       # 工具函数
│   │   ├── request.ts               # Axios封装
│   │   ├── sa-token.ts              # Sa-Token封装
│   │   ├── validate.ts              # 表单验证
│   │   ├── format.ts                # 格式化工具
│   │   ├── constants.ts             # 常量定义
│   │   ├── regex.ts                 # 正则表达式
│   │   ├── browser.ts               # 浏览器工具
│   │   └── error.ts                 # 错误处理
│   │
│   ├── views/                       # 页面组件
│   │   ├── login/
│   │   │   ├── index.vue            # 登录页面
│   │   │   └── LoginForm.vue        # 登录表单
│   │   ├── dashboard/
│   │   │   └── index.vue            # 首页仪表盘
│   │   ├── search/
│   │   │   ├── index.vue            # 知识搜索页
│   │   │   ├── SearchHeader.vue     # 搜索头部
│   │   │   ├── SearchResults.vue    # 搜索结果
│   │   │   ├── SearchFilters.vue    # 搜索筛选
│   │   │   └── SearchEmpty.vue      # 空结果页
│   │   ├── chat/
│   │   │   ├── index.vue            # 智能问答页
│   │   │   ├── ChatPanel.vue        # 聊天面板
│   │   │   ├── ChatWelcome.vue      # 欢迎页
│   │   │   └── QuickQuestions.vue   # 快捷问题
│   │   ├── knowledge/
│   │   │   ├── list.vue             # 知识列表
│   │   │   ├── detail.vue           # 知识详情
│   │   │   ├── editor.vue           # 知识编辑
│   │   │   └── CreateKnowledge.vue  # 创建知识库
│   │   ├── admin/
│   │   │   ├── audit.vue            # 审核管理
│   │   │   ├── auditDetail.vue      # 审核详情
│   │   │   ├── statistics.vue       # 统计分析
│   │   │   ├── statisticsChart.vue  # 统计图表
│   │   │   └── syncMonitor.vue      # 同步监控
│   │   ├── system/
│   │   │   ├── users.vue            # 用户管理
│   │   │   ├── userForm.vue         # 用户表单
│   │   │   ├── roles.vue            # 角色管理
│   │   │   ├── roleForm.vue         # 角色表单
│   │   │   ├── settings.vue         # 系统设置
│   │   │   └── password.vue         # 修改密码
│   │   ├── error/
│   │   │   ├── 403.vue              # 403页面
│   │   │   ├── 404.vue              # 404页面
│   │   │   └── 500.vue              # 500页面
│   │   └── redirect/
│   │       └── index.vue            # 重定向页
│   │
│   ├── App.vue                      # 根组件
│   ├── main.ts                      # 入口文件
│   └── shims-vue.d.ts               # Vue类型声明
│
├── .env                             # 环境变量
├── .env.development                 # 开发环境
├── .env.staging                     # 预发布环境
├── .env.production                  # 生产环境
├── .eslintrc.js                     # ESLint配置
├── .prettierrc                      # Prettier配置
├── index.html                       # HTML模板
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

---

## 三、核心组件设计

### 3.1 路由配置

```typescript
// src/router/routes.ts
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'dashboard' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/search/index.vue'),
        meta: { title: '知识搜索', icon: 'search' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '智能问答', icon: 'chat' }
      },
      {
        path: 'knowledge',
        name: 'KnowledgeList',
        component: () => import('@/views/knowledge/list.vue'),
        meta: { title: '知识管理', icon: 'folder', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'knowledge/:id',
        name: 'KnowledgeDetail',
        component: () => import('@/views/knowledge/detail.vue'),
        meta: { title: '知识详情', hidden: true }
      },
      {
        path: 'knowledge/edit/:id?',
        name: 'KnowledgeEditor',
        component: () => import('@/views/knowledge/editor.vue'),
        meta: { title: '知识编辑', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/admin/audit.vue'),
        meta: { title: '审核管理', icon: 'audit', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/admin/statistics.vue'),
        meta: { title: '统计分析', icon: 'chart', roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/system/users.vue'),
        meta: { title: '用户管理', icon: 'user', roles: ['ADMIN'] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/system/settings.vue'),
        meta: { title: '系统设置', icon: 'setting', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { requiresAuth: false }
  }
]

export default routes
```

### 3.2 状态管理

```typescript
// src/store/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserInfo } from '@/types/user'
import { getUserInfo, logout as logoutApi } from '@/api/auth'
import { removeToken, getToken } from '@/utils/sa-token'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // State
  const user = ref<UserInfo | null>(null)
  const token = ref<string>(getToken() || '')
  const roles = ref<string[]>([])
  const permissions = ref<string[]>([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isManager = computed(() => roles.value.includes('MANAGER') || isAdmin.value)
  const userName = computed(() => user.value?.realName || user.value?.username || '')

  // Actions
  async function login(username: string, password: string) {
    const { data } = await loginApi({ username, password })
    token.value = data.accessToken
    user.value = data.user
    roles.value = [data.user.role.code]
    return data
  }

  async function getUserInfo() {
    if (!token.value) return null
    
    try {
      const { data } = await getUserInfo()
      user.value = data
      roles.value = [data.role.code]
      return data
    } catch {
      logout()
      return null
    }
  }

  function logout() {
    logoutApi().finally(() => {
      resetState()
      router.push('/login')
    })
  }

  function resetState() {
    user.value = null
    token.value = ''
    roles.value = []
    permissions.value = []
    removeToken()
  }

  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission)
  }

  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  return {
    user,
    token,
    roles,
    permissions,
    isLoggedIn,
    isAdmin,
    isManager,
    userName,
    login,
    getUserInfo,
    logout,
    hasPermission,
    hasRole
  }
})
```

### 3.3 API封装

```typescript
// src/utils/request.ts
import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'
import { getToken, removeToken } from './sa-token'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, data, message } = response.data
    
    if (code === 200) {
      return data
    }
    
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error) => {
    const { response } = error
    
    if (response) {
      switch (response.status) {
        case 401:
          removeToken()
          router.push('/login')
          ElNotification.error({
            title: '登录过期',
            message: '您的登录已过期，请重新登录'
          })
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误，请稍后重试')
          break
        default:
          ElMessage.error(response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

export default request
```

### 3.4 Sa-Token封装

```typescript
// src/utils/sa-token.ts

// 常量定义
export const SA_TOKEN_KEY = 'police_kb_token'
export const SA_TOKEN_HEADER = 'Authorization'
export const SA_TOKEN_PREFIX = 'Bearer'

// 获取Token
export function getToken(): string {
  return localStorage.getItem(SA_TOKEN_KEY) || ''
// 设置Token
export function setToken(token: string): void {
  localStorage.setItem(SA_TOKEN_KEY, token)
}

// 移除Token
export function removeToken(): void {
  localStorage.removeItem(SA_TOKEN_KEY)
}

// Token是否有效
export function isTokenValid(): boolean {
  const token = getToken()
  if (!token) return false
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 > Date.now()
  } catch {
    return false
  }
}

// 获取请求头
export function getAuthHeaders(): Record<string, string> {
  return {
    [SA_TOKEN_HEADER]: `${SA_TOKEN_PREFIX} ${getToken()}`
  }
}

// 获取用户ID
export function getUserId(): number | null {
  const token = getToken()
  if (!token) return null
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.userId || null
  } catch {
    return null
  }
}
```

### 3.5 聊天组件

```typescript
// src/views/chat/index.vue
<template>
  <div class="chat-container">
    <!-- 左侧会话列表 -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>对话历史</h3>
        <el-button type="primary" size="small" @click="createNewChat">
          新建对话
        </el-button>
      </div>
      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="['session-item', { active: currentSessionId === session.id }]"
          @click="selectSession(session.id)"
        >
          <span class="session-title">{{ session.title }}</span>
          <span class="session-time">{{ formatTime(session.updatedAt) }}</span>
        </div>
      </div>
    </div>
    
    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <template v-if="messages.length > 0">
          <ChatMessage
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
          />
        </template>
        <template v-else>
          <ChatWelcome @quickAsk="onQuickAsk" />
        </template>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-area">
        <ChatInput
          v-model="inputMessage"
          :loading="streaming"
          @send="sendMessage"
          @stop="stopStream"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { useChatStore } from '@/store/modules/chat'
import ChatMessage from '@/components/chat/ChatMessage/index.vue'
import ChatInput from '@/components/chat/ChatInput/index.vue'
import ChatWelcome from '@/components/chat/ChatWelcome/index.vue'
import { getChatSessions, createChatSession } from '@/api/chat'

const chatStore = useChatStore()

const inputMessage = ref('')
const messageListRef = ref<HTMLElement>()
const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<string | null>(null)

const messages = computed(() => chatStore.messages)
const streaming = computed(() => chatStore.streaming)

onMounted(() => {
  loadSessions()
})

async function loadSessions() {
  const { data } = await getChatSessions()
  sessions.value = data
}

function selectSession(id: string) {
  currentSessionId.value = id
  chatStore.initSession(id)
  loadMessages(id)
}

async function createNewChat() {
  const { data } = await createChatSession()
  currentSessionId.value = data.id
  chatStore.initSession(data.id)
}

function sendMessage() {
  if (!inputMessage.value.trim() || streaming.value) return
  
  chatStore.streamMessage(inputMessage.value)
  inputMessage.value = ''
  
  nextTick(() => {
    scrollToBottom()
  })
}

function stopStream() {
  chatStore.stopStreaming()
}

function onQuickAsk(question: string) {
  inputMessage.value = question
  sendMessage()
}

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

function formatTime(time: string): string {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: 100%;
  
  .chat-sidebar {
    width: 280px;
    border-right: 1px solid #e4e7ed;
    background: #f5f7fa;
    
    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
      }
    }
    
    .session-list {
      overflow-y: auto;
      height: calc(100% - 60px);
      
      .session-item {
        padding: 12px 16px;
        cursor: pointer;
        border-bottom: 1px solid #ebeef5;
        transition: background 0.3s;
        
        &:hover,
        &.active {
          background: #ecf5ff;
        }
        
        .session-title {
          display: block;
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .session-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
    }
    
    .input-area {
      padding: 16px 20px;
      border-top: 1px solid #e4e7ed;
    }
  }
}
</style>
```

---

## 四、页面设计

### 4.1 首页仪表盘

```
┌─────────────────────────────────────────────────────────────────┐
│  公安知识库系统                                    🔔(3) 张三 ✘ │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │  欢迎回来，张三     │  │  快捷搜索            │              │
│  │  治安大队 | 民警   │  │  [搜索法律法规...]  │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  热门搜索                              查看全部 >      │   │
│  │  🔥 打架斗殴    🔥 盗窃    🔥 赌博    🔥 诈骗          │   │
│  │  🔥 交通违法    🔥 户籍    🔥 治安    🔥 毒品           │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────┐  ┌─────────────────────┐              │
│  │  近期问答统计       │  │  新入库知识         │              │
│  │  ┌───────────────┐  │  ┌─────────────────┐ │              │
│  │  │  今日问答 25   │  │  │ 治安管理处罚法   │ │              │
│  │  │  本周 156     │  │  │ 户籍办理指南     │ │              │
│  │  │  本月 523     │  │  │ 刑事案件程序     │ │              │
│  │  └───────────────┘  │  └─────────────────┘ │              │
│  └─────────────────────┘  └─────────────────────┘              │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  操作导航                                                 │   │
│  │  [📚 知识搜索]  [💬 智能问答]  [📄 知识管理]  [📊 统计]  │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 智能问答页面

```
┌─────────────────────────────────────────────────────────────────┐
│  智能问答                                              清除历史 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  🤖 您好，我是公安知识助手有什么可以帮您？              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  👤 打架斗殴一般怎么处理？                              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  🤖 根据《治安管理处罚法》第43条规定：                  │   │
│  │     殴打他人的，或者故意伤害他人身体的，                │   │
│  │     处五日以上十日以下拘留，并处二百元以上              │   │
│  │     五百元以下罚款；情节较轻的，处五日以下              │   │
│  │     拘留或者五百元以下罚款。                            │   │
│  │                                                         │   │
│  │  📄 参考来源：                                          │   │
│  │     • 治安管理处罚法（公安部令第128号）                 │   │
│  │     • 治安案件办理程序规定                              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  👤 那轻微伤呢？                                        │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  [输入问题...]                         [发送] [附件]    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 4.3 知识搜索页面

```
┌─────────────────────────────────────────────────────────────────┐
│  知识搜索                                              高级搜索 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  [打架斗殴     🔍]                     搜索类型：        │   │
│  │                                           ○ 全部         │   │
│  │                                           ○ 法律法规     │   │
│  │                                           ○ 规章制度     │   │
│  │                                           ○ 培训资料     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  筛选条件：                                               │   │
│  │  警种： [治安    ▼]  业务： [治安管理 ▼]  来源：[全部 ▼]│   │
│  │  时间： [近一年 ▼]  排序： [相关度 ▼]                    │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  共找到 156 条结果，耗时 0.023 秒                          导出   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  📄 治安管理处罚法（2023修订）                          │   │
│  │     来源：公安部 | 发布：2023-01-01 | 访问：3,256       │   │
│  │     摘要：为了维护社会治安秩序，保障公共安全...         │   │
│  │     [相关度: 0.95] [📎 收藏] [👁 预览] [📄 原文]       │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│                          [1] [2] [3] ... [16]  >               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 五、环境配置

### 5.1 环境变量

```bash
# .env.development
VITE_API_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080
VITE_APP_TITLE=公安知识库系统
VITE_APP_SHORT_TITLE=知识库

# .env.production
VITE_API_URL=https://kb.police.moj.gov.cn/api/v1
VITE_WS_URL=wss://kb.police.moj.gov.cn
VITE_APP_TITLE=公安知识库系统
VITE_APP_SHORT_TITLE=知识库
```

### 5.2 Vite配置

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@/assets/styles/variables.scss" as *;`
      }
    }
  }
})
```

---

**文档版本**: 2.0.0  
**最后更新**: 2025年1月20日
