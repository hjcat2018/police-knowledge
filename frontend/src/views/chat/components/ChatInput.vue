<template>
  <div class="chat-input" :class="{ disabled: disabled }">
    <div v-if="attachedFiles.length > 0" class="attached-files">
      <el-divider v-if="showDivider" content-position="left">
        <span>已上传文件 ({{ attachedFiles.length }})</span>
      </el-divider>
      <div class="file-tags">
        <el-tag
          v-for="(file, idx) in attachedFiles"
          :key="idx"
          closable
          @close="$emit('remove-file', idx)"
          class="file-tag">
          <el-icon><Document /></el-icon>
          {{ file.name }}
        </el-tag>
      </div>
    </div>

    <el-input
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      :type="inputType"
      :placeholder="placeholder"
      :disabled="disabled"
      @keydown.enter.exact.prevent="handleEnter"
      :rows="rows"
      :resize="resize ? 'vertical' : 'none'" />

    <div class="input-actions">
      <div class="left-actions">
        <slot name="actions-left">
          <el-button size="small" circle :disabled="disabled" @click="$emit('show-template')">
            <el-icon><Document /></el-icon>
          </el-button>
          <el-button size="small" circle :disabled="disabled" @click="$emit('show-commands')">
            <el-icon><Lightning /></el-icon>
          </el-button>
        </slot>
      </div>
      <slot name="actions-right">
        <el-button
          type="primary"
          :loading="loading"
          :disabled="disabled || !canSend"
          @click="handleSend">
          <el-icon v-if="!loading"><Promotion /></el-icon>
          {{ sendButtonText || '发送' }}
        </el-button>
      </slot>
    </div>

    <div v-if="errorMessage" class="error-banner">
      <el-icon><Warning /></el-icon>
      <span>{{ errorMessage }}</span>
      <el-button
        v-if="canRetry"
        size="small"
        text
        @click="$emit('retry')">
        重试
      </el-button>
      <el-button size="small" text @click="$emit('clear-error')">
        <el-icon><Close /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { Document, Lightning, Promotion, Warning, Close } from '@element-plus/icons-vue'

  interface AttachedFile {
    name: string
    size: string
    type: string
  }

  interface Props {
    modelValue: string
    disabled?: boolean
    loading?: boolean
    placeholder?: string
    rows?: number
    resize?: boolean
    showDivider?: boolean
    sendButtonText?: string
    errorMessage?: string
    canRetry?: boolean
    attachedFiles?: AttachedFile[]
    inputType?: 'text' | 'textarea'
  }

  const props = withDefaults(defineProps<Props>(), {
    disabled: false,
    loading: false,
    placeholder: '输入问题，按 Enter 发送...',
    rows: 3,
    resize: true,
    showDivider: true,
    sendButtonText: '',
    errorMessage: '',
    canRetry: false,
    attachedFiles: () => [],
    inputType: 'textarea'
  })

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void
    (e: 'send'): void
    (e: 'show-template'): void
    (e: 'show-commands'): void
    (e: 'remove-file', index: number): void
    (e: 'retry'): void
    (e: 'clear-error'): void
  }>()

  const canSend = computed(() => props.modelValue.trim().length > 0)

  const handleEnter = () => {
    if (props.inputType === 'textarea') {
      emit('send')
    }
  }

  const handleSend = () => {
    if (canSend.value && !props.disabled && !props.loading) {
      emit('send')
    }
  }
</script>

<style lang="scss" scoped>
  .chat-input {
    padding: 16px 24px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    flex-shrink: 0;

    &.disabled {
      opacity: 0.6;
      pointer-events: none;
    }

    .attached-files {
      margin-bottom: 12px;

      :deep(.el-divider__text) {
        font-size: 12px;
        color: #909399;
      }

      .file-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        padding: 8px 0;

        .file-tag {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    :deep(.el-textarea__inner),
    :deep(.el-input__inner) {
      padding: 8px 12px;
    }

    :deep(.el-textarea__inner) {
      min-height: 80px !important;
      line-height: 26px;
    }

    .input-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;

      .left-actions {
        display: flex;
        gap: 8px;
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
</style>
