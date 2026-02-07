<template>
  <el-card class="summary-card">
    <template #header>
      <div class="card-header">
        <span>文档摘要</span>
        <div class="actions">
        <el-button
          v-if="summaryStatus === 'pending'"
          type="primary"
            size="small"
            :loading="generating"
            @click="handleGenerate">
            生成摘要
          </el-button>
          <el-button
            v-if="summaryStatus === 'completed'"
            size="small"
            @click="handleRegenerate">
            重新生成
          </el-button>
        </div>
      </div>
    </template>

    <div class="summary-content" v-if="summary">
      <el-input
        type="textarea"
        v-model="summary"
        :rows="6"
        resize="none"
        @blur="handleSaveSummary" />
      <div class="summary-meta">
        <el-tag type="info" size="small">
          {{ chunkCount }} 个分块
        </el-tag>
        <el-tag type="success" size="small">
          约 {{ Math.round((summaryLength || summary.length) / 2) }} 字
        </el-tag>
      </div>
    </div>

    <div class="generating" v-else-if="generating">
      <el-progress
        :percentage="progressPercent"
        :status="progressPercent === 100 ? 'success' : ''" />
      <p class="progress-text">
        正在生成摘要 ({{ currentChunk }}/{{ totalChunks }} 块)
      </p>
    </div>

    <div class="empty" v-else>
      <el-empty description="暂无摘要" :image-size="60">
        <el-button type="primary" @click="handleGenerate">
          点击生成摘要
        </el-button>
      </el-empty>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSummaryStatus, regenerateSummary } from '@/api/summary'
import { summaryWS } from '@/utils/summary-websocket'

const props = defineProps<{
  docId: number
}>()

const summary = ref('')
const summaryLength = ref(0)
const generating = ref(false)
const currentChunk = ref(0)
const totalChunks = ref(0)
const chunkCount = ref(0)

const summaryStatus = computed(() => {
  if (generating.value) return 'processing'
  if (summary.value) return 'completed'
  return 'pending'
})

const progressPercent = computed(() => {
  if (totalChunks.value === 0) return 0
  return Math.round((currentChunk.value / totalChunks.value) * 100)
})

  const loadSummaryStatus = async () => {
    try {
      const res = await getSummaryStatus(props.docId)
      if (res.data) {
        const data = res.data as any
        summary.value = data.summary || ''
        summaryLength.value = data.length || 0
        chunkCount.value = data.chunks || 0
      }
    } catch (e) {
      console.error('加载摘要状态失败:', e)
    }
  }

const handleGenerate = () => {
  generating.value = true
  currentChunk.value = 0
  totalChunks.value = 10

  summaryWS
    .connect(props.docId)
    .onProgress((current, total) => {
      currentChunk.value = current
      totalChunks.value = total
    })
    .onComplete((resultSummary) => {
      summary.value = resultSummary
      summaryLength.value = resultSummary.length
      generating.value = false
      ElMessage.success('摘要生成完成')
    })
    .onError((error) => {
      generating.value = false
      ElMessage.error(error)
    })

  regenerateSummary(props.docId)
}

const handleRegenerate = () => {
  summary.value = ''
  handleGenerate()
}

const handleSaveSummary = () => {
  console.log('摘要已保存')
}

onMounted(() => {
  loadSummaryStatus()
})

onUnmounted(() => {
  summaryWS.close()
})
</script>

<style scoped>
.summary-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-content .el-textarea {
  margin-bottom: 12px;
}

.summary-meta {
  display: flex;
  gap: 8px;
}

.generating {
  text-align: center;
  padding: 20px 0;
}

.progress-text {
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}
</style>
