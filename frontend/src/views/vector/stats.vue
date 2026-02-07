<template>
  <div class="vector-stats-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>向量统计</h3>
          <div>
            <el-button type="primary" @click="refreshStats" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="vectorizeAll" :loading="vectorizing">
              <el-icon><Upload /></el-icon>
              全部向量化
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="文档总数" :value="stats.totalDocuments || 0" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="向量总数" :value="stats.totalVectors || 0" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="已向量化文档" :value="stats.vectorizedDocuments || 0" />
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="recent-card">
      <template #header>
        <span>文档列表</span>
        <el-button type="primary" link @click="refreshDocs">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </template>
      
      <el-table :data="documents" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="statusName" label="状态" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="vectorizeOne(row.id)">向量化</el-button>
            <el-button type="success" link @click="viewDocument(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <DocDetailDialog
      v-model:visible="detailVisible"
      :doc-id="currentDocId"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Upload } from '@element-plus/icons-vue'
import { getDocumentList, Document } from '@/api/doc'
import { useUserStore } from '@/store/modules/user'
import DocDetailDialog from '@/views/doc/list/components/DocDetailDialog.vue'

interface Stats {
  totalDocuments: number
  totalVectors: number
  vectorizedDocuments: number
}

const loading = ref(false)
const vectorizing = ref(false)
const documents = ref<Document[]>([])
const stats = reactive<Stats>({
  totalDocuments: 0,
  totalVectors: 0,
  vectorizedDocuments: 0
})
const detailVisible = ref(false)
const currentDocId = ref(0)

const refreshStats = async () => {
  const userStore = useUserStore()
  loading.value = true
  try {
    const response = await fetch('/api/v1/vector/stats', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    const data = await response.json()
    if (data.code === 200) {
      stats.totalDocuments = data.data.totalDocuments || 0
      stats.totalVectors = data.data.totalVectors || 0
      stats.vectorizedDocuments = data.data.vectorizedDocuments || 0
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const refreshDocs = async () => {
  const userStore = useUserStore()
  loading.value = true
  try {
    const response = await fetch('/api/v1/vector/stats', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    const data = await response.json()
    if (data.code === 200) {
      const docRes = await getDocumentList({ pageNum: 1, pageSize: 50 })
      documents.value = docRes.records
    }
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  } finally {
    loading.value = false
  }
}

const vectorizeOne = async (id: number) => {
  const userStore = useUserStore()
  try {
    await fetch(`/api/v1/vector/vectorize/${id}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    ElMessage.success('向量化完成')
    refreshStats()
  } catch (error) {
    ElMessage.error('向量化失败')
  }
}

const vectorizeAll = async () => {
  const userStore = useUserStore()
  vectorizing.value = true
  try {
    const response = await fetch('/api/v1/vector/vectorize/all', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    })
    const data = await response.json()
    if (data.code === 200) {
      ElMessage.success(`成功: ${data.data.successCount}, 失败: ${data.data.failCount}`)
      refreshStats()
      refreshDocs()
    } else {
      ElMessage.error(data.message || '向量化失败')
    }
  } catch (error) {
    ElMessage.error('向量化失败')
  } finally {
    vectorizing.value = false
  }
}

const viewDocument = (id: number) => {
  currentDocId.value = id
  detailVisible.value = true
}

onMounted(() => {
  refreshStats()
  refreshDocs()
})
</script>

<style lang="scss" scoped>
.vector-stats-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h3 {
    margin: 0;
    font-size: 18px;
  }
}

.recent-card {
  margin-top: 20px;
}
</style>
