<template>
  <div class="conversation-list">
    <div class="sidebar-header">
      <slot name="header">
        <el-button type="primary" @click="$emit('create')" style="width: 100%">
          <el-icon><Plus /></el-icon>
          {{ createButtonText || '新建对话' }}
        </el-button>
      </slot>
    </div>
    <div class="conversation-items">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="conversation-item"
        :class="{ active: currentConversationId === conv.id }"
        @click="$emit('select', conv.id)">
        <el-icon><ChatLineRound /></el-icon>
        <span class="title">{{ conv.title }}</span>
        <el-dropdown
          trigger="click"
          @command="(cmd: string) => $emit('command', cmd, conv.id)">
          <el-icon class="more-icon"><MoreFilled /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="delete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <el-empty v-if="conversations.length === 0" description="暂无对话" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Plus, ChatLineRound, MoreFilled } from '@element-plus/icons-vue'
  import type { Conversation } from '@/api/conversation'

  interface Props {
    conversations: Conversation[]
    currentConversationId: number | null
    createButtonText?: string
  }

  defineProps<Props>()

  defineEmits<{
    (e: 'create'): void
    (e: 'select', id: number): void
    (e: 'command', command: string, id: number): void
  }>()
</script>

<style lang="scss" scoped>
  .conversation-list {
    display: flex;
    flex-direction: column;
    height: 100%;

    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;
    }

    .conversation-items {
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

        .more-icon {
          opacity: 1;
        }
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
        color: #909399;

        &:hover {
          color: #409eff;
        }
      }
    }
  }
</style>
