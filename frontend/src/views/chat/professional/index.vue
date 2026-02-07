<template>
  <div class="chat-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <el-button type="primary" @click="createNewChat" style="width: 100%">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
      </div>
      <div class="conversation-list">
        <div
          v-for="conv in chatStore.conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: chatStore.currentConversationId === conv.id }"
          @click="selectConversation(conv.id)">
          <el-icon><ChatLineRound /></el-icon>
          <span class="title">{{ conv.title }}</span>
          <el-dropdown
            trigger="click"
            @command="(cmd: string) => handleCommand(cmd, conv.id)">
            <el-icon class="more-icon"><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="delete">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="chat-main">
      <div class="chat-header" v-if="chatStore.currentConversationId">
        <el-tag type="primary" size="large" effect="dark" style="display: inline-flex; align-items: center; gap: 4px;">
          <el-icon style="display: inline-flex; margin-right: 2px;"><Reading /></el-icon>
          专业模式
        </el-tag>
        <el-cascader
          v-model="selectedKbId"
          placeholder="请选择知识库"
          clearable
          filterable
          style="width: 200px; margin-left: 16px"
          :options="kbList"
          :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }" />
        <div class="connection-status" :class="connectionStatus">
          <el-icon v-if="connectionStatus === 'connected'"
            ><CircleCheck
          /></el-icon>
          <el-icon v-else-if="connectionStatus === 'disconnected'"
            ><CircleClose
          /></el-icon>
          <el-icon v-else><Loading /></el-icon>
          <span>{{ connectionStatusText }}</span>
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
          <div class="empty-content">
            <el-icon size="64" color="#409eff"><Reading /></el-icon>
            <h3>专业问答模式</h3>
            <p>基于知识库进行精准问答</p>
            <div class="quick-tips">
              <el-tag type="info" size="large" effect="plain" round>选择知识库后开始提问</el-tag>
              <el-tag type="info" size="large" effect="plain" round>支持多级分类筛选</el-tag>
              <el-tag type="info" size="large" effect="plain" round>提供答案来源引用</el-tag>
            </div>
          </div>
        </div>
        <div
          v-for="msg in chatStore.currentMessages"
          :key="msg.id"
          class="message"
          :class="msg.role">
          <div class="message-avatar">
            <el-avatar
              :size="36"
              :icon="msg.role === 'user' ? 'User' : 'ChatDotRound'" />
          </div>
          <div class="message-content">
            <div class="message-meta">
              <span class="message-time">{{
                formatTime(msg.createdTime)
              }}</span>
              <span v-if="msg.tokenCount" class="token-info">
                {{ msg.tokenCount }} tokens
              </span>
            </div>
            <div
              class="message-bubble"
              v-html="formatContent(msg.content)"></div>
            <div
              v-if="
                msg.role === 'assistant' &&
                msg.references &&
                parseReferences(msg.references).length > 0
              "
              class="message-references">
              <div class="references-header">
                <el-icon><Document /></el-icon>
                <span
                  >参考文档 ({{ parseReferences(msg.references).length }})</span
                >
              </div>
              <div
                v-for="(ref, index) in parseReferences(msg.references)"
                :key="index"
                class="reference-item">
                <el-tag type="info" size="small" effect="plain" round>{{
                  index + 1
                }}</el-tag>
                <span class="ref-title">{{ ref.title }}</span>
                <el-tag type="success" size="small"
                  >{{ (ref.score * 100).toFixed(0) }}%</el-tag
                >
              </div>
            </div>
          </div>
        </div>
        <div v-if="loading" class="message assistant loading">
          <div class="message-avatar">
            <el-avatar :size="36" icon="ChatDotRound" />
          </div>
          <div class="message-content">
            <div class="message-bubble loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              正在检索知识库并生成回答...
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="输入问题，按 Enter 发送..."
          :disabled="!chatStore.currentConversationId || loading"
          @keyup.enter="sendMessage">
          <template #append>
            <el-button
              @click="sendMessage"
              :loading="loading"
              :disabled="!chatStore.currentConversationId">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </template>
        </el-input>
        <div v-if="errorMessage" class="error-banner">
          <el-icon><Warning /></el-icon>
          <span>{{ errorMessage }}</span>
          <el-button
            size="small"
            text
            @click="retryLastMessage"
            v-if="canRetry">
            重试
          </el-button>
          <el-button size="small" text @click="clearError">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted, onUnmounted, onActivated, nextTick } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    Plus,
    ChatLineRound,
    MoreFilled,
    Promotion,
    Document,
    Loading,
    CircleCheck,
    CircleClose,
    Warning,
    Close,
    Reading
  } from '@element-plus/icons-vue'
  import {
    createConversation,
    listConversations,
    getMessages,
    deleteConversation,
    ChatMessage,
    Conversation
  } from '@/api/conversation'
  import { getKbCategoryList, type SysDict } from '@/api/dict'
  import { useChatStore } from '@/store/modules/chat'
  import DOMPurify from 'dompurify'
  import { renderMarkdown } from '@/utils/markdown'

  const route = useRoute()
  const chatStore = useChatStore() as any

  const messagesContainer = ref<HTMLElement>()
  const loading = ref(false)
  const inputMessage = ref('')
  const kbList = ref<any[]>([])
  const selectedKbId = ref<number | null>(null)
  const selectedCategoryId = ref<number | null>(null)

  const connectionStatus = ref<'connected' | 'disconnected' | 'connecting'>(
    'disconnected'
  )
  const lastHeartbeatTime = ref<number>(0)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 3
  const reconnectDelay = 2000
  const heartbeatTimeout = 35000
  let eventSource: EventSource | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let heartbeatTimeoutTimer: ReturnType<typeof setTimeout> | null = null

  const errorMessage = ref('')
  const canRetry = ref(false)
  const pendingQuestion = ref('')

  const connectionStatusText = computed(() => {
    switch (connectionStatus.value) {
      case 'connected':
        return 'AI工作中'
      case 'disconnected':
        return '就绪'
      case 'connecting':
        return '连接中...'
      default:
        return '就绪'
    }
  })

  const loadConversations = async () => {
    const loadMode = 'professional'
    chatStore.setCurrentLoadMode(loadMode)
    
    try {
      const res = (await listConversations({ page: 1, size: 100, mode: loadMode })) as any
      let convList: Conversation[] = []
      if (res && typeof res === 'object' && 'records' in res) {
        convList = res.records || []
      } else if (Array.isArray(res)) {
        convList = res
      }
      if (chatStore.currentLoadMode !== loadMode) return
      chatStore.setConversations(convList)

      const lastId = chatStore.getLastVisitConversationId()
      if (lastId && !chatStore.currentConversationId) {
        selectConversation(lastId)
      }
    } catch (error) {
      console.error('加载对话列表失败', error)
    }
  }

  const transformKbTree = (list: SysDict[]): any[] => {
    return list.map(item => ({
      id: item.id,
      name: item.detail,
      children: item.children && item.children.length > 0 ? transformKbTree(item.children) : undefined
    }))
  }

  const loadKbList = async () => {
    try {
      const kbListData = await getKbCategoryList(1)
      kbList.value = transformKbTree(kbListData)
    } catch (error) {
      console.error('加载知识库列表失败', error)
    }
  }

  const createNewChat = async () => {
    try {
      const newConv = (await createConversation({
        title: '新对话',
        mode: 'professional'
      })) as unknown as Conversation
      chatStore.addConversation(newConv)
      chatStore.setCurrentConversationId(newConv.id)
      chatStore.setMessages(newConv.id, [])
      chatStore.setChatMode('professional')
      selectedKbId.value = null
      selectedCategoryId.value = null
      closeEventSource()
    } catch (error) {
      ElMessage.error('创建对话失败')
    }
  }

  const selectConversation = async (id: number) => {
    chatStore.setCurrentConversationId(id)
    closeEventSource()
    try {
      const res = (await getMessages(id)) as unknown as ChatMessage[]
      chatStore.setMessages(id, res || [])
      scrollToBottom()
    } catch (error) {
      console.error('加载消息失败', error)
    }
  }

  const closeEventSource = () => {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
    if (heartbeatTimeoutTimer) {
      clearTimeout(heartbeatTimeoutTimer)
      heartbeatTimeoutTimer = null
    }
    connectionStatus.value = 'disconnected'
  }

  const startHeartbeatTimer = () => {
    heartbeatTimer = setInterval(() => {
      const now = Date.now()
      if (now - lastHeartbeatTime.value > heartbeatTimeout) {
        console.warn('[Chat] 心跳超时，尝试重连')
        handleReconnect()
      }
    }, 10000)
  }

  const handleReconnect = () => {
    if (reconnectAttempts.value >= maxReconnectAttempts) {
      console.error('[Chat] 重连次数已达上限')
      showError('连接已断开，请刷新页面重试', false)
      connectionStatus.value = 'disconnected'
      return
    }

    reconnectAttempts.value++
    connectionStatus.value = 'connecting'
    console.log(
      `[Chat] 尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})`
    )

    setTimeout(() => {
      if (pendingQuestion.value && chatStore.currentConversationId) {
        sendMessageContent(pendingQuestion.value)
      }
    }, reconnectDelay * reconnectAttempts.value)
  }

  const showError = (message: string, retry: boolean) => {
    errorMessage.value = message
    canRetry.value = retry
  }

  const clearError = () => {
    errorMessage.value = ''
    canRetry.value = false
  }

  const retryLastMessage = () => {
    if (pendingQuestion.value) {
      clearError()
      sendMessageContent(pendingQuestion.value)
    }
  }

  const sendMessage = () => {
    if (
      !inputMessage.value.trim() ||
      !chatStore.currentConversationId ||
      loading.value
    )
      return

    const question = inputMessage.value.trim()
    inputMessage.value = ''
    sendMessageContent(question)
  }

  const sendMessageContent = async (question: string) => {
    if (!chatStore.currentConversationId || loading.value) return

    pendingQuestion.value = question
    clearError()

    const convId = chatStore.currentConversationId
    const userMessage: ChatMessage = {
      conversationId: convId,
      role: 'user',
      content: question
    }
    chatStore.addMessage(convId, userMessage)
    scrollToBottom()

    loading.value = true
    connectionStatus.value = 'connecting'
    reconnectAttempts.value = 0

    const assistantMessage: ChatMessage = {
      conversationId: convId,
      role: 'assistant',
      content: ''
    }
    chatStore.addMessage(convId, assistantMessage)
    scrollToBottom()

    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    const kbId = selectedKbId.value || 0
    const categoryId = selectedCategoryId.value || undefined
    let url = `${baseUrl}/v1/chat/stream/${convId}?question=${encodeURIComponent(question)}&kbId=${kbId}`
    if (categoryId !== undefined) {
      url += `&categoryId=${categoryId}`
    }

    console.log(
      '[Chat] 发送消息 - 模式:',
      'professional',
      ', kbId:',
      kbId,
      ', categoryId:',
      categoryId,
      ', 知识库列表:',
      kbList.value.length
    )
    console.log('[Chat] 连接到SSE:', url)

    try {
      eventSource = new EventSource(url)

      eventSource.onopen = () => {
        console.log('[Chat] SSE连接已建立')
        connectionStatus.value = 'connected'
        lastHeartbeatTime.value = Date.now()
        startHeartbeatTimer()
      }

      eventSource.onmessage = (event: MessageEvent) => {
        console.log('[Chat] 收到消息:', event.data)
      }

      eventSource.addEventListener('heartbeat', () => {
        console.log('[Chat] 收到心跳')
        lastHeartbeatTime.value = Date.now()
      })

      eventSource.addEventListener('chunk', (event: MessageEvent) => {
        console.log('[Chat] 收到内容块:', event.data)
        try {
          const data = JSON.parse(event.data)
          if (data.content) {
            chatStore.appendAssistantContent(convId, data.content)
            scrollToBottom()
          }
        } catch (e) {
          console.error('[Chat] 解析内容块错误:', e)
        }
      })

      eventSource.addEventListener('done', (event: MessageEvent) => {
        console.log('[Chat] 流式响应完成:', event.data)
        closeEventSource()
        loading.value = false
        pendingQuestion.value = ''

        try {
          const data = JSON.parse(event.data)
          if (data.references || data.totalTokens || data.createdTime) {
            chatStore.updateMessageReferences(
              convId,
              data.references || '[]',
              data.totalTokens,
              data.createdTime
            )
          }
        } catch (e) {
          console.error('[Chat] 解析完成事件错误:', e)
        }
        scrollToBottom()
      })

      eventSource.onerror = (event: Event) => {
        console.error('[Chat] SSE错误:', event)
        closeEventSource()
        loading.value = false

        if (pendingQuestion.value) {
          handleReconnect()
        } else {
          showError('生成回答失败，请稍后重试', true)
        }
      }

      setTimeout(() => {
        if (eventSource && eventSource.readyState !== EventSource.CLOSED) {
          console.warn('[Chat] 请求超时，关闭连接')
          closeEventSource()
          loading.value = false
          showError('响应超时，请检查网络连接', true)
        }
      }, 180000)
    } catch (error) {
      console.error('[Chat] 发送消息错误:', error)
      loading.value = false
      connectionStatus.value = 'disconnected'
      showError('发送消息失败，请稍后重试', true)
    }
  }

  const handleCommand = async (command: string, id: number) => {
    if (command === 'delete') {
      try {
        await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
          type: 'warning'
        })
        await deleteConversation(id)
        chatStore.removeConversation(id)
        if (chatStore.currentConversationId === id) {
          chatStore.clearCurrentConversation()
          closeEventSource()
        }
        ElMessage.success('删除成功')
      } catch {}
    }
  }

  const scrollToBottom = () => {
    nextTick(() => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    })
  }

  const formatContent = (content: string) => {
    if (!content) return ''
    try {
      return renderMarkdown(content)
    } catch (e) {
      console.error('[Chat] Markdown解析错误:', e)
      return DOMPurify.sanitize(content.replace(/\n/g, '<br>'))
    }
  }

  const formatTime = (time: string | undefined) => {
    if (!time) return ''
    try {
      const date = new Date(time)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    } catch {
      return ''
    }
  }

  interface Reference {
    documentId: number
    title: string
    content: string
    score: number
  }

  const parseReferences = (json: string): Reference[] => {
    if (!json || json.trim() === '') return []
    try {
      let cleanedJson = json
      cleanedJson = cleanedJson.replace(/\n/g, '\\n')
      cleanedJson = cleanedJson.replace(/\r/g, '\\r')
      cleanedJson = cleanedJson.replace(/\t/g, '\\t')
      const parsed = JSON.parse(cleanedJson)
      return Array.isArray(parsed) ? parsed : []
    } catch (e: any) {
      console.error('JSON解析错误:', e.message)
      return [
        {
          documentId: 0,
          title: '解析失败',
          content: '错误: ' + e.message,
          score: 0
        } as Reference
      ]
    }
  }

  onMounted(() => {
    loadKbList()
  })

  onActivated(() => {
    chatStore.setChatMode('professional')
    loadConversations()
    if (route.query.id) {
      selectConversation(Number(route.query.id))
    }
  })

  onUnmounted(() => {
    closeEventSource()
  })
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: 100%;
  background: var(--bg-secondary);
}

.chat-sidebar {
  width: 280px;
  background: var(--card-bg);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;

  .sidebar-header {
    padding: 16px;
    border-bottom: 1px solid var(--border-color);
  }

  .conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }

  .conversation-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 4px;
    color: var(--text-primary);

    &:hover {
      background: var(--bg-tertiary);
    }

    &.active {
      background: #409eff;
      color: #fff;
    }

    .title {
      flex: 1;
      margin-left: 8px;
      font-size: 14px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .more-icon {
      opacity: 0;
      transition: opacity 0.2s;
    }

    &:hover .more-icon {
      opacity: 1;
    }
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .chat-header {
    padding: 16px 24px;
    background: var(--header-bg);
    border-bottom: 1px solid var(--border-color);
    font-size: 16px;
    font-weight: 500;
    flex-shrink: 0;
    display: flex;
    align-items: center;

    .connection-status {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 4px;
      margin-left: auto;

      &.connected {
        color: #67c23a;
        background: rgba(103, 194, 58, 0.1);
      }

      &.disconnected {
        color: var(--text-secondary);
        background: var(--bg-tertiary);
      }

      &.connecting {
        color: #e6a23c;
        background: rgba(230, 162, 60, 0.1);
      }
    }
  }

  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    min-height: 0;

    .empty-state {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .empty-content {
        text-align: center;
        color: var(--text-regular);

        h3 {
          margin: 16px 0 8px;
          font-size: 20px;
          color: var(--text-primary);
        }

        p {
          margin: 0 0 16px;
          font-size: 14px;
          color: var(--text-secondary);
        }

        .quick-tips {
          display: flex;
          gap: 8px;
          justify-content: center;
          flex-wrap: wrap;
        }
      }
    }

    .message {
      display: flex;
      margin-bottom: 24px;

      &.user {
        flex-direction: row-reverse;

        .message-bubble {
          background: #409eff;
          color: #fff;
          border-radius: 12px;
          border-bottom-right-radius: 4px;
        }
      }

      &.assistant .message-bubble {
        background: var(--card-bg);
        border-radius: 12px;
        border: 1px solid var(--border-color);
      }

      .message-avatar {
        flex-shrink: 0;
      }

      .message-content {
        max-width: 70%;
        margin: 0 12px;
      }

      .message-meta {
        margin-bottom: 4px;
        font-size: 12px;
        color: var(--text-secondary);
        display: flex;
        align-items: center;
        gap: 8px;

        .token-info {
          color: var(--text-secondary);
          font-size: 11px;
        }
      }

      .message-bubble {
        padding: 12px 16px;
        border-radius: 12px;
        line-height: 1.6;
        font-size: 14px;

        &.loading {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        :deep(ul),
        :deep(ol) {
          padding-left: 20px;
          margin: 10px 0;
        }

        :deep(li) {
          margin: 6px 0;
          line-height: 1.6;
          color: var(--text-regular);
          list-style-type: disc;
        }

        :deep(h1) {
          font-size: 1.5em;
          margin: 20px 0 12px;
          font-weight: 600;
          color: var(--text-primary);
          border-bottom: 1px solid var(--border-color);
          padding-bottom: 8px;
        }

        :deep(h2) {
          font-size: 1.25em;
          margin: 18px 0 10px;
          font-weight: 600;
          color: var(--text-primary);
        }

        :deep(h3) {
          font-size: 1.1em;
          margin: 16px 0 8px;
          font-weight: 600;
          color: var(--text-primary);
        }

        :deep(h4) {
          font-size: 1em;
          margin: 14px 0 6px;
          font-weight: 600;
          color: var(--text-primary);
        }

        :deep(p) {
          margin: 10px 0;
          line-height: 1.8;
          // color: var(--text-regular);
        }

        :deep(blockquote) {
          margin: 12px 0;
          padding: 10px 14px;
          border-left: 4px solid var(--primary-color);
          background: rgba(24, 144, 255, 0.1);
          color: var(--text-regular);
          border-radius: 0 4px 4px 0;
        }

        :deep(pre) {
          margin: 12px 0;
          padding: 14px;
          background: var(--bg-tertiary);
          border-radius: 6px;
          overflow-x: auto;
        }

        :deep(pre code) {
          font-family: 'Fira Code', 'Consolas', monospace;
          font-size: 13px;
          color: var(--text-primary);
          background: none;
          padding: 0;
        }

        :deep(code) {
          font-family: 'Fira Code', 'Consolas', monospace;
          font-size: 13px;
          background: var(--bg-tertiary);
          padding: 2px 6px;
          border-radius: 4px;
          color: #f56c6c;
          margin: 0 2px;
        }

        :deep(a) {
          color: var(--primary-color);
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }

        :deep(table) {
          width: 100%;
          margin: 12px 0;
          border-collapse: collapse;
        }

        :deep(th),
        :deep(td) {
          padding: 10px 14px;
          border: 1px solid var(--border-color);
          text-align: left;
        }

        :deep(th) {
          background: var(--bg-tertiary);
          font-weight: 600;
          color: var(--text-primary);
        }

        :deep(tr:nth-child(even)) {
          background: var(--bg-secondary);
        }

        :deep(hr) {
          margin: 20px 0;
          border: none;
          border-top: 1px solid var(--border-color);
        }

        :deep(strong) {
          font-weight: 600;
          color: var(--text-primary);
        }

        :deep(em) {
          font-style: italic;
        }

        :deep(img) {
          max-width: 100%;
          height: auto;
          margin: 10px 0;
          border-radius: 6px;
        }

        :deep(p) {
          margin: 0 0 8px 0;
          &:last-child {
            margin-bottom: 0;
          }
        }
      }

      .message-references {
        margin-top: 8px;
        padding: 12px;
        background: var(--bg-tertiary);
        border-radius: 8px;
        font-size: 12px;

        .references-header {
          display: flex;
          align-items: center;
          gap: 4px;
          color: var(--text-secondary);
          margin-bottom: 8px;
          font-weight: 500;
        }

        .reference-item {
          display: flex;
          justify-content: flex-start;
          align-items: center;
          padding: 4px 0;
          color: var(--text-regular);
          gap: 8px;

          .ref-title {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }

  .chat-input {
    padding: 16px 24px;
    background: var(--header-bg);
    border-top: 1px solid var(--border-color);
    flex-shrink: 0;

    :deep(.el-input__wrapper) {
      padding: 8px 12px;
      min-height: 80px;
      background: var(--bg-tertiary);

      .el-input__inner {
        min-height: 60px;
        line-height: 26px;
        color: var(--text-primary);
      }
    }

    :deep(.el-input-group__append) {
      padding: 0;

      .el-button {
        margin: 0;
        border-radius: 0 4px 4px 0;
        padding: 12px 16px;
      }
    }

    .error-banner {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
      padding: 8px 12px;
      background: rgba(245, 108, 108, 0.1);
      border-radius: 4px;
      color: #f56c6c;
      font-size: 12px;
    }
  }
}
</style>
