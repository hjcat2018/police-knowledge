<template>
  <div class="chat-messages" ref="containerRef">
    <slot name="empty">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="开始提问吧" />
      </div>
    </slot>

    <div
      v-for="msg in messages"
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
          <span class="message-time">{{ formatTime(msg.createdTime) }}</span>
          <span v-if="msg.modelName" class="model-name">
            <el-tag size="small" effect="plain">{{ msg.modelName }}</el-tag>
          </span>
          <span v-if="msg.tokenCount" class="token-info">
            {{ msg.tokenCount }} tokens
          </span>
        </div>
        <div
          class="message-bubble"
          v-html="formatContent(msg.content)"></div>
        <div v-if="msg.files && msg.files.length > 0" class="message-files">
          <div v-for="(file, idx) in msg.files" :key="idx" class="file-item">
            <el-icon><Document /></el-icon>
            <span>{{ file.name }}</span>
          </div>
        </div>
        <div
          v-if="
            msg.role === 'assistant' &&
            msg.references &&
            parseReferences(msg.references).length > 0
          "
          class="message-references">
          <div class="references-header">
            <el-icon><Document /></el-icon>
            <span>参考文档 ({{ parseReferences(msg.references).length }})</span>
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

    <slot name="loading">
      <div v-if="loading" class="message assistant loading">
        <div class="message-avatar">
          <el-avatar :size="36" icon="ChatDotRound" />
        </div>
        <div class="message-content">
          <div class="message-bubble loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <slot name="loading-text">正在思考...</slot>
          </div>
        </div>
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, nextTick, watch } from 'vue'
  import { Document, Loading } from '@element-plus/icons-vue'
  import type { ChatMessage } from '@/api/conversation'
  import DOMPurify from 'dompurify'
  import { renderMarkdown } from '@/utils/markdown'

  interface Props {
    messages: ChatMessage[]
    loading?: boolean
    autoScroll?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false,
    autoScroll: true
  })

  const containerRef = ref<HTMLElement>()

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

  const formatContent = (content: string) => {
    if (!content) return ''
    try {
      return renderMarkdown(content)
    } catch (e) {
      return DOMPurify.sanitize(content.replace(/\n/g, '<br>'))
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
    } catch {
      return []
    }
  }

  const scrollToBottom = () => {
    if (!props.autoScroll || !containerRef.value) return
    nextTick(() => {
      containerRef.value!.scrollTop = containerRef.value!.scrollHeight
    })
  }

  watch(() => props.messages.length, () => {
    scrollToBottom()
  })

  watch(() => props.loading, (newVal) => {
    if (!newVal) {
      nextTick(() => scrollToBottom())
    }
  })

  onMounted(() => {
    scrollToBottom()
  })

  defineExpose({ scrollToBottom })
</script>

<style lang="scss" scoped>
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

        :deep(h1),
        :deep(h2),
        :deep(h3),
        :deep(h4) {
          font-weight: 600;
          margin: 12px 0 8px 0;
        }

        :deep(ul),
        :deep(ol) {
          margin: 8px 0;
          padding-left: 24px;
        }

        :deep(code) {
          background: #f5f7fa;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: monospace;
        }

        :deep(pre) {
          background: #1e293b;
          padding: 12px;
          border-radius: 8px;
          overflow-x: auto;
          color: #fff;

          code {
            background: transparent;
            padding: 0;
            color: inherit;
          }
        }

        :deep(a) {
          color: #409eff;
        }

        :deep(blockquote) {
          margin: 8px 0;
          padding: 8px 16px;
          border-left: 4px solid #409eff;
          background: #f5f7fa;
        }

        &.loading {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .message-files {
        margin-top: 8px;

        .file-item {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          padding: 4px 8px;
          background: #f5f7fa;
          border-radius: 4px;
          font-size: 12px;
          margin-right: 8px;
          margin-bottom: 4px;
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
</style>
