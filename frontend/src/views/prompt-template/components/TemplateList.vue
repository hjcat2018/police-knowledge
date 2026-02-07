<template>
  <div class="template-list">
    <el-empty v-if="templates.length === 0" description="暂无模板" />
    
    <el-row v-else :gutter="16">
      <el-col style="margin-bottom: 16px;" v-for="template in templates" :key="template.id" :span="8">
        <el-card class="template-card" :class="{ 'is-default': template.isDefault === 1 }">
          <div class="template-header">
            <div class="tags">
              <el-tag v-if="template.isSystem === 1" type="info" size="small">系统</el-tag>
              <el-tag v-if="template.isDefault === 1" type="success" size="small">默认</el-tag>
            </div>
            <h3>{{ template.name }}</h3>
          </div>
          
          <el-input
            type="textarea"
            :model-value="template.content"
            readonly
            class="template-content"
            :rows="4"
          />
          
          <p class="template-desc">{{ template.description || '暂无描述' }}</p>
          
          <div class="template-actions" v-if="editable !== false">
            <el-button link type="primary" @click="$emit('edit', template)">编辑</el-button>
            <el-button 
              v-if="template.isDefault !== 1" 
              link 
              type="primary" 
              @click="$emit('set-default', template.id)"
            >
              设为默认
            </el-button>
            <el-button link type="danger" @click="$emit('delete', template.id)">删除</el-button>
          </div>
          <div class="template-actions" v-else>
            <el-button link type="primary" @click="$emit('copy', template)">复制到我的模板</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import type { PromptTemplate } from '@/api/promptTemplate'

defineProps<{
  templates: PromptTemplate[]
  editable?: boolean
}>()

defineEmits<{
  (e: 'edit', template: PromptTemplate): void
  (e: 'delete', id: number): void
  (e: 'set-default', id: number): void
  (e: 'copy', template: PromptTemplate): void
}>()
</script>

<style lang="scss" scoped>
.template-list {
  min-height: 200px;
}

.template-card {
  height: 100%;
  // margin-bottom: 16px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.is-default {
    border-color: #67c23a;
  }

  .template-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .tags {
      display: flex;
      gap: 4px;
    }

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      flex: 1;
    }
  }

  .template-content {
    margin-bottom: 12px;

    :deep(.el-textarea__inner) {
      background: #f5f7fa;
      font-family: 'Consolas', monospace;
      font-size: 13px;
      color: #606266;
    }
  }

  .template-desc {
    font-size: 12px;
    color: #909399;
    margin: 0 0 12px 0;
    line-height: 1.5;
  }

  .template-actions {
    display: flex;
    gap: 8px;
    border-top: 1px solid #ebeef5;
    padding-top: 12px;
    margin-top: auto;
  }
}

html.dark {
  .template-card {
    .template-header h3 {
      color: #ffffff;
    }

    .template-content :deep(.el-textarea__inner) {
      background: #2a2a3e;
      color: #e0e0e0;
    }

    .template-desc {
      color: #a0a0a0;
    }

    .template-actions {
      border-top-color: #3d3d3d;
    }
  }
}
</style>
