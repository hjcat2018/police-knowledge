<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <SkeletonCard :loaded="!loading" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #1890ff, #096dd9);">
            <el-icon size="28"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ docCount }}</div>
            <div class="stat-label">文档总数</div>
          </div>
        </SkeletonCard>
      </el-col>

      <el-col :span="6">
        <SkeletonCard :loaded="!loading" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a, #389e0d);">
            <el-icon size="28"><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ kbCount }}</div>
            <div class="stat-label">知识库</div>
          </div>
        </SkeletonCard>
      </el-col>

      <el-col :span="6">
        <SkeletonCard :loaded="!loading" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #faad14, #d48806);">
            <el-icon size="28"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ chatCount }}</div>
            <div class="stat-label">智能问答</div>
          </div>
        </SkeletonCard>
      </el-col>

      <el-col :span="6">
        <SkeletonCard :loaded="!loading" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #eb2f96, #c41d7f);">
            <el-icon size="28"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ userCount }}</div>
            <div class="stat-label">在线用户</div>
          </div>
        </SkeletonCard>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近更新文档</span>
              <el-button text type="primary">查看更多</el-button>
            </div>
          </template>
          <SkeletonTable :loaded="!loading" :rows="5" :columns="3" v-if="loading">
            <el-table :data="recentDocs" style="width: 100%">
              <el-table-column prop="title" label="文档标题" />
              <el-table-column prop="kbName" label="所属知识库" width="180" />
              <el-table-column prop="updatedTime" label="更新时间" width="180" />
            </el-table>
          </SkeletonTable>
          <el-table v-else :data="recentDocs" style="width: 100%">
            <el-table-column prop="title" label="文档标题" />
            <el-table-column prop="kbName" label="所属知识库" width="180" />
            <el-table-column prop="updatedTime" label="更新时间" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-links">
            <div class="quick-link-item">
              <el-icon size="24"><DocumentAdd /></el-icon>
              <span>新建文档</span>
            </div>
            <div class="quick-link-item">
              <el-icon size="24"><Search /></el-icon>
              <span>文档搜索</span>
            </div>
            <div class="quick-link-item">
              <el-icon size="24"><ChatDotRound /></el-icon>
              <span>智能问答</span>
            </div>
            <div class="quick-link-item">
              <el-icon size="24"><Collection /></el-icon>
              <span>知识库管理</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Document, Collection, ChatDotRound, User, DocumentAdd, Search } from '@element-plus/icons-vue'
import SkeletonCard from '@/components/common/SkeletonCard.vue'
import SkeletonTable from '@/components/common/SkeletonTable.vue'

const loading = ref(true)
const docCount = ref('1,234')
const kbCount = ref('56')
const chatCount = ref('3,456')
const userCount = ref('128')

const recentDocs = ref([
  { title: '治安管理处罚法修订解读', kbName: '法律法规', updatedTime: '2024-01-20 10:30' },
  { title: '社区警务工作规范', kbName: '业务规范', updatedTime: '2024-01-19 16:45' },
  { title: '交通事故处理流程指南', kbName: '操作指南', updatedTime: '2024-01-19 14:20' },
  { title: '常见证件办理问题解答', kbName: '常见问题', updatedTime: '2024-01-18 11:15' }
])

onMounted(() => {
  setTimeout(() => {
    loading.value = false
  }, 1500)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  height: 100%;
  min-height: 100px;
}

.stat-card {
  :deep(.el-card__body) {
    display: flex;
    align-items: center;
    padding: 20px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
    flex-shrink: 0;
  }

  .stat-content {
    flex: 1;
    min-width: 0;

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: var(--text-primary);
      line-height: 1.2;
    }

    .stat-label {
      font-size: 14px;
      color: var(--text-secondary);
      margin-top: 4px;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .quick-link-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    background: var(--bg-tertiary);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: var(--border-color);
      transform: translateY(-2px);
    }

    .el-icon {
      color: var(--primary-color);
      margin-bottom: 8px;
    }

    span {
      font-size: 14px;
      color: var(--text-primary);
    }
  }
}
</style>
