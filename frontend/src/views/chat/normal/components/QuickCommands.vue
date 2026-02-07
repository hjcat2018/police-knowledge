<template>
  <div class="quick-commands">
    <div class="panel-header">
      <el-icon><Lightning /></el-icon>
      <span>快捷指令</span>
    </div>
    <div class="command-list">
      <div
        v-for="command in commands"
        :key="command.id"
        class="command-item"
        @click="executeCommand(command)">
        <div class="command-icon">
          <el-icon :size="20"><component :is="getIcon(command.icon)" /></el-icon>
        </div>
        <div class="command-info">
          <div class="command-name">{{ command.command }}</div>
          <div class="command-desc">{{ command.description }}</div>
        </div>
        <div class="command-action">
          <el-icon><Right /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Lightning, Right, InfoFilled, Document, Warning, Search, DataAnalysis } from '@element-plus/icons-vue'

  interface QuickCommand {
    id: number
    command: string
    description: string
    icon: string
  }

  const props = defineProps<{
    commands: QuickCommand[]
  }>()

  void props

  const emit = defineEmits<{
    (e: 'execute', command: string): void
  }>()

  const getIcon = (iconName: string) => {
    const icons: Record<string, any> = {
      Info: InfoFilled,
      Document,
      Translate: Warning,
      Search,
      DataAnalysis
    }
    return icons[iconName] || InfoFilled
  }

  const executeCommand = (command: QuickCommand) => {
    emit('execute', command.command)
  }
</script>

<style lang="scss" scoped>
  .quick-commands {
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

    .command-list {
      .command-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
        margin-bottom: 8px;
        background: #f5f7fa;

        &:hover {
          background: #ecf5ff;

          .command-action {
            color: #409eff;
          }
        }

        .command-icon {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #fff;
          border-radius: 8px;
          margin-right: 12px;
          color: #409eff;
        }

        .command-info {
          flex: 1;

          .command-name {
            font-weight: 500;
            margin-bottom: 4px;
          }

          .command-desc {
            font-size: 12px;
            color: #909399;
          }
        }

        .command-action {
          color: #909399;
          transition: color 0.2s;
        }
      }
    }
  }
</style>
