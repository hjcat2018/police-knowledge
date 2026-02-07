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
        <el-radio-group
          v-model="chatStore.chatMode"
          size="default"
          style="margin-right: 16px">
          <el-radio-button value="normal">普通模式</el-radio-button>
          <el-radio-button value="professional">专业模式</el-radio-button>
        </el-radio-group>
        <el-cascader
          v-if="chatStore.chatMode === 'professional'"
          v-model="selectedKbId"
          placeholder="请选择知识库"
          clearable
          filterable
          style="width: 200px; margin-right: 16px"
          :options="kbList"
          :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }" />
        <div class="connection-status" :class="connectionStatus">
          <el-icon v-if="connectionStatus === 'connected'"
            ><CircleCheck
          /></el-icon>
          <el-icon v-else-if="connectionStatus === 'disconnected'"
            ><CircleCheck
          /></el-icon>
          <el-icon v-else><Loading /></el-icon>
          <span>{{ connectionStatusText }}</span>
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
          <el-empty description="开始提问吧" />
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
              正在思考...
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
  import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
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
    Warning,
    Close
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
    try {
      const res = (await listConversations({ page: 1, size: 100 })) as any
      let convList: Conversation[] = []
      if (res && typeof res === 'object' && 'records' in res) {
        convList = res.records || []
      } else if (Array.isArray(res)) {
        convList = res
      }
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
        title: '新对话'
      })) as unknown as Conversation
      chatStore.addConversation(newConv)
      chatStore.setCurrentConversationId(newConv.id)
      chatStore.setMessages(newConv.id, [])
      chatStore.setChatMode('normal')
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
    const kbId =
      chatStore.chatMode === 'professional' ? selectedKbId.value || 0 : 0
    const categoryId =
      chatStore.chatMode === 'professional' && selectedCategoryId.value ? selectedCategoryId.value : undefined
    let url = `${baseUrl}/v1/chat/stream/${convId}?question=${encodeURIComponent(question)}&kbId=${kbId}`
    if (categoryId !== undefined) {
      url += `&categoryId=${categoryId}`
    }

    console.log(
      '[Chat] 发送消息 - 模式:',
      chatStore.chatMode,
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
    loadConversations()
    loadKbList()
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
    background: #f5f7fa;
  }

  .chat-sidebar {
    width: 280px;
    background: #fff;
    border-right: 1px solid #e4e7ed;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;
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

      &:hover {
        background: #f5f7fa;
      }

      &.active {
        background: #ecf5ff;
        color: #409eff;
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
      background: #fff;
      border-bottom: 1px solid #e4e7ed;
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

        &.connected {
          color: #67c23a;
          background: #f0f9eb;
        }

        &.disconnected {
          color: #67c23a;
          background: #f0f9eb;
        }

        &.connecting {
          color: #e6a23c;
          background: #fdf6ec;
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
      }

      .message {
        display: flex;
        margin-bottom: 24px;

        &.user {
          flex-direction: row-reverse;

          .message-bubble {
            background: #409eff;
            color: #fff;
          }
        }

        &.assistant .message-bubble {
          background: #fff;
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
          color: #909399;
          display: flex;
          align-items: center;
          gap: 8px;

          .token-info {
            color: #909399;
            font-size: 11px;
          }
        }

        .message-bubble {
          padding: 12px 16px;
          border-radius: 12px;
          line-height: 1.6;
          font-size: 14px;

          :deep(p) {
            margin: 0 0 8px 0;
            &:last-child {
              margin-bottom: 0;
            }
          }

          :deep(h1) {
            font-size: 1.5em;
            font-weight: 600;
            margin: 16px 0 12px 0;
            color: #1a1a1a;
            border-bottom: 1px solid #e8e8e8;
            padding-bottom: 8px;
          }

          :deep(h2) {
            font-size: 1.3em;
            font-weight: 600;
            margin: 14px 0 10px 0;
            color: #2c2c2c;
          }

          :deep(h3) {
            font-size: 1.15em;
            font-weight: 600;
            margin: 12px 0 8px 0;
            color: #3c3c3c;
          }

          :deep(h4) {
            font-size: 1em;
            font-weight: 600;
            margin: 10px 0 6px 0;
            color: #4c4c4c;
          }

          :deep(strong),
          :deep(b) {
            font-weight: 600;
            // color: #1890ff;
            color: #1890ff;
            background: linear-gradient(
              to bottom,
              transparent 80%,
              rgba(24, 144, 255, 0.2) 10%
            );
          }

          :deep(em),
          :deep(i) {
            font-style: italic;
            color: #52c41a;
          }

          :deep(blockquote) {
            margin: 12px 0;
            padding: 8px 16px;
            border-left: 4px solid #1890ff;
            background: linear-gradient(135deg, #f6f8ff 0%, #f0f4ff 100%);
            color: #595959;
            border-radius: 0 8px 8px 0;

            p {
              margin: 0;
            }

            :deep(blockquote) {
              margin: 8px 0;
              border-left-color: #52c41a;
              background: linear-gradient(135deg, #f6fff6 0%, #f0fff0 100%);
            }
          }

          :deep(ul),
          :deep(ol) {
            margin: 8px 0;
            padding-left: 24px;

            li {
              margin: 4px 0;
              line-height: 1.6;
            }
          }

          :deep(ul) {
            list-style-type: disc;

            li::marker {
              color: #1890ff;
            }
          }

          :deep(ol) {
            list-style-type: decimal;

            li::marker {
              color: #52c41a;
              font-weight: 500;
            }
          }

          :deep(table) {
            width: 100%;
            margin: 12px 0;
            border-collapse: collapse;
            background: #fff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

            thead {
              background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
              color: #fff;

              th {
                padding: 12px 16px;
                text-align: left;
                font-weight: 600;
                white-space: nowrap;
              }
            }

            tbody {
              tr {
                border-bottom: 1px solid #f0f0f0;
                transition: background 0.2s;

                &:last-child {
                  border-bottom: none;
                }

                &:hover {
                  background: #f5f7fa;
                }
              }

              td {
                padding: 10px 16px;
                line-height: 1.5;
              }
            }
          }

          :deep(a) {
            color: #1890ff;
            text-decoration: none;
            border-bottom: 1px dashed #1890ff;
            transition: all 0.2s;

            &:hover {
              color: #40a9ff;
              border-bottom-style: solid;
            }
          }

          :deep(code) {
            background: linear-gradient(135deg, #f5f7fa 0%, #eef1f5 100%);
            padding: 2px 8px;
            border-radius: 4px;
            font-family: 'Fira Code', 'Consolas', monospace;
            font-size: 0.9em;
            color: #eb2f96;
            border: 1px solid #e8e8e8;
          }

          :deep(pre) {
            background: #1e293b;
            padding: 16px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 12px 0;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

            code {
              background: transparent;
              padding: 0;
              color: #606266;
              font-size: 13px;
              line-height: 1.5;
              border: none;
            }
          }

          :deep(.hljs-code-block) {
            margin: 0;
            padding: 10px;
            background: transparent;
          }

          :deep(hr) {
            border: none;
            height: 2px;
            background: linear-gradient(
              90deg,
              transparent 0%,
              #1890ff 50%,
              transparent 100%
            );
            margin: 16px 0;
          }

          :deep(.markdown-image-container) {
            margin: 12px 0;
            text-align: center;

            .markdown-image {
              max-width: 100%;
              height: auto;
              border-radius: 8px;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }

            .markdown-image-alt {
              display: block;
              margin-top: 8px;
              font-size: 12px;
              color: #8c8c8c;
              font-style: italic;
            }
          }

          :deep(.task-list) {
            list-style-type: none;
            padding-left: 0;

            li {
              display: flex;
              align-items: flex-start;
              margin: 4px 0;

              input[type='checkbox'] {
                margin-right: 8px;
                margin-top: 4px;
              }
            }
          }

          :deep(.footnotes) {
            margin-top: 24px;
            padding-top: 16px;
            border-top: 1px solid #e8e8e8;
            font-size: 12px;
            color: #8c8c8c;

            .footnote-item {
              margin: 4px 0;
            }
          }

          &.loading {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }

        .message-references {
          margin-top: 8px;
          padding: 12px;
          background: #f4f4f5;
          border-radius: 8px;
          font-size: 12px;

          .references-header {
            display: flex;
            align-items: center;
            gap: 4px;
            color: #909399;
            margin-bottom: 8px;
            font-weight: 500;
          }

          .reference-item {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            padding: 4px 0;
            color: #606266;
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
      background: #fff;
      border-top: 1px solid #e4e7ed;
      flex-shrink: 0;

      :deep(.el-input__wrapper) {
        padding: 8px 12px;
        min-height: 80px;

        .el-input__inner {
          min-height: 60px;
          line-height: 26px;
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
        background: #fef0f0;
        border-radius: 4px;
        color: #f56c6c;
        font-size: 12px;
      }
    }
  }
</style>
