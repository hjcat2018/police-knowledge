import { defineStore } from 'pinia'
import type { Conversation, ChatMessage, ChatFile } from '@/api/conversation'

export interface ChatState {
  conversations: Conversation[]
  currentConversationId: number | null
  messages: Record<number, ChatMessage[]>
  chatMode: 'normal' | 'professional'
  selectedKbId: number | null
  lastVisitConversationId: number | null
  attachedFiles: ChatFile[]
  professionalConversations: Conversation[]
  normalConversations: Conversation[]
  currentLoadMode: string | null
}

export const useChatStore = defineStore('chat', {
  state: (): ChatState => ({
    conversations: [],
    currentConversationId: null,
    messages: {},
    chatMode: 'normal',
    selectedKbId: null,
    lastVisitConversationId: null,
    attachedFiles: [],
    professionalConversations: [],
    normalConversations: [],
    currentLoadMode: null as string | null
  }),

  getters: {
    currentMessages: (state): ChatMessage[] => {
      if (!state.currentConversationId) return []
      return state.messages[state.currentConversationId] || []
    },

    currentConversation: (state): Conversation | undefined => {
      if (!state.currentConversationId) return undefined
      return state.conversations.find(c => c.id === state.currentConversationId)
    },

    hasActiveConversation: (state): boolean => {
      return state.currentConversationId !== null
    },

    conversationCount: (state): number => {
      return state.conversations.length
    }
  },

  actions: {
    setCurrentLoadMode(mode: string | null) {
      this.currentLoadMode = mode
    },

    setConversations(convs: Conversation[]) {
      const expectedMode = this.currentLoadMode
      if (expectedMode === 'professional') {
        this.professionalConversations = convs
        this.conversations = convs
      } else if (expectedMode === 'normal') {
        this.normalConversations = convs
        this.conversations = convs
      } else {
        this.conversations = convs
      }
      this.currentLoadMode = null
    },

    addConversation(conv: Conversation) {
      if (this.chatMode === 'professional') {
        this.professionalConversations.unshift(conv)
      } else {
        this.normalConversations.unshift(conv)
      }
      this.conversations.unshift(conv)
    },

    updateConversation(id: number, updates: Partial<Conversation>) {
      const index = this.conversations.findIndex(c => c.id === id)
      if (index !== -1) {
        this.conversations[index] = { ...this.conversations[index], ...updates }
      }
      if (this.chatMode === 'professional') {
        const pIndex = this.professionalConversations.findIndex(c => c.id === id)
        if (pIndex !== -1) {
          this.professionalConversations[pIndex] = { ...this.professionalConversations[pIndex], ...updates }
        }
      } else {
        const nIndex = this.normalConversations.findIndex(c => c.id === id)
        if (nIndex !== -1) {
          this.normalConversations[nIndex] = { ...this.normalConversations[nIndex], ...updates }
        }
      }
    },

    removeConversation(id: number) {
      this.conversations = this.conversations.filter(c => c.id !== id)
      this.professionalConversations = this.professionalConversations.filter(c => c.id !== id)
      this.normalConversations = this.normalConversations.filter(c => c.id !== id)
      if (this.currentConversationId === id) {
        this.currentConversationId = null
        delete this.messages[id]
      }
      delete this.messages[id]
    },

    setCurrentConversationId(id: number | null) {
      this.currentConversationId = id
      if (id) {
        if (this.chatMode === 'professional') {
          this.lastVisitConversationId = id
        } else {
          this.lastVisitConversationId = id
        }
      }
    },

    setMessages(conversationId: number, msgs: ChatMessage[]) {
      this.messages[conversationId] = msgs
    },

    addMessage(conversationId: number, msg: ChatMessage): number {
      if (!this.messages[conversationId]) {
        this.messages[conversationId] = []
      }
      // 生成临时消息ID（使用时间戳+随机数）
      const tempId = Date.now() + Math.floor(Math.random() * 1000)
      ;(msg as any).id = tempId
      this.messages[conversationId].push(msg)
      return tempId
    },

    appendAssistantContent(conversationId: number, content: string) {
      const messages = this.messages[conversationId]
      if (messages && messages.length > 0) {
        const lastMsg = messages[messages.length - 1]
        if (lastMsg.role === 'assistant') {
          lastMsg.content += content
        }
      }
    },

    appendAssistantContentByMsgId(conversationId: number, msgId: number, content: string) {
      const messages = this.messages[conversationId]
      if (!messages) return
      const index = messages.findIndex(m => (m as any).id === msgId || m.id === msgId)
      if (index !== -1 && messages[index].role === 'assistant') {
        messages[index].content += content
      }
    },

    updateMessageReferences(conversationId: number, references: string, tokenCount?: number, createdTime?: string) {
      const messages = this.messages[conversationId]
      if (messages && messages.length > 0) {
        const lastMsg = messages[messages.length - 1]
        if (lastMsg.role === 'assistant') {
          lastMsg.references = references
          if (tokenCount !== undefined) {
            lastMsg.tokenCount = tokenCount
          }
          if (createdTime) {
            lastMsg.createdTime = createdTime
          }
        }
      }
    },

    updateMessageReferencesByMsgId(conversationId: number, msgId: number, tokenCount?: number, createdTime?: string) {
      const messages = this.messages[conversationId]
      if (!messages) return
      const targetMsg = messages.find(m => (m as any).id === msgId || m.id === msgId)
      if (targetMsg && targetMsg.role === 'assistant') {
        targetMsg.references = '[]'
        if (tokenCount !== undefined) {
          targetMsg.tokenCount = tokenCount
        }
        if (createdTime) {
          targetMsg.createdTime = createdTime
        }
      }
    },

    setChatMode(mode: 'normal' | 'professional') {
      this.chatMode = mode
      this.currentConversationId = null
      this.messages = {}
      if (mode === 'professional') {
        this.conversations = this.professionalConversations
      } else {
        this.conversations = this.normalConversations
      }
    },

    setSelectedKbId(kbId: number | null) {
      this.selectedKbId = kbId
    },

    clearCurrentConversation() {
      if (this.currentConversationId) {
        delete this.messages[this.currentConversationId]
      }
      this.currentConversationId = null
    },

    clearAllMessages() {
      this.messages = {}
    },

    clearChatHistory() {
      this.conversations = []
      this.professionalConversations = []
      this.normalConversations = []
      this.currentConversationId = null
      this.messages = {}
      this.lastVisitConversationId = null
    },

    getLastVisitConversationId(): number | null {
      return this.lastVisitConversationId
    },

    addAttachedFile(file: ChatFile) {
      this.attachedFiles.push(file)
    },

    removeAttachedFile(index: number) {
      if (index >= 0 && index < this.attachedFiles.length) {
        this.attachedFiles.splice(index, 1)
      }
    },

    clearAttachedFiles() {
      this.attachedFiles = []
    }
  },

  persist: {
    key: 'police-kb-chat',
    storage: localStorage
  }
})
