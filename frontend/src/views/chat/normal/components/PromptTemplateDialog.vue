<template>
  <div class="prompt-template-dialog">
    <div class="panel-header">
      <el-icon><Document /></el-icon>
      <span>提示词模板</span>
      <el-button size="small" type="success" text @click="$emit('manage')">
        <el-icon><Setting /></el-icon>
        管理
      </el-button>
    </div>
    <div class="template-list">
      <div
        v-for="template in templates"
        :key="template.id"
        class="template-item"
        @click="$emit('select', template)">
        <div class="template-header">
          <el-icon><Document /></el-icon>
          <span class="template-name">{{ template.name }}</span>
          <el-tag :type="getTypeColor(template.type)" size="small" effect="plain">
            {{ getTypeLabel(template.type) }}
          </el-tag>
        </div>
        <div class="template-desc">{{ template.description }}</div>
        <div class="template-preview">
          <el-input
            type="textarea"
            :value="template.content"
            readonly
            :rows="3"
            size="small" />
        </div>
        <div class="template-action">
          <el-button size="small" type="primary" plain>
            <el-icon><Pointer /></el-icon>
            使用模板
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Document, Setting, Pointer } from '@element-plus/icons-vue'

  interface PromptTemplate {
    id: number
    name: string
    content: string
    description: string
    type: string
  }

  const props = defineProps<{
    templates: PromptTemplate[]
  }>()

  void props

  defineEmits<{
    (e: 'select', template: PromptTemplate): void
    (e: 'manage'): void
  }>()

  const getTypeColor = (type: string): 'success' | 'warning' | 'danger' | 'info' => {
    const colors: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
      text: 'info',
      translate: 'success',
      case: 'warning',
      regulation: 'danger'
    }
    return colors[type] || 'info'
  }

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      text: '文本',
      translate: '翻译',
      case: '案例',
      regulation: '法规'
    }
    return labels[type] || '通用'
  }
</script>

<style lang="scss" scoped>
  .prompt-template-dialog {
    .panel-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding-bottom: 12px;
      border-bottom: 1px solid #e4e7ed;
      margin-bottom: 12px;
      font-weight: 500;
      color: #303133;
    }

    .template-list {
      max-height: 500px;
      overflow-y: auto;

      .template-item {
        padding: 12px;
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #409eff;
          background: #ecf5ff;
        }

        .template-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .template-name {
            flex: 1;
            font-weight: 500;
          }
        }

        .template-desc {
          font-size: 12px;
          color: #909399;
          margin-bottom: 8px;
        }

        .template-preview {
          margin-bottom: 8px;

          :deep(.el-textarea__inner) {
            background: #f5f7fa;
            border: none;
            resize: none;
          }
        }

        .template-action {
          text-align: right;
        }
      }
    }
  }
</style>
