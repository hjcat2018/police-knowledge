<template>
  <el-drawer
    v-model="dialogVisible"
    title="文档详情"
    size="50%"
  >
    <div v-if="docData" class="doc-detail">
      <h1 class="doc-title">{{ docData.title }}</h1>
      <div class="doc-meta">
        <span>作者：{{ docData.author || '未知' }}</span>
        <span>来源：{{ docData.source || '原创' }}</span>
        <span v-if="docData.sourceTime">来源时间：{{ docData.sourceTime }}</span>
        <span>浏览：{{ docData.viewCount }} 次</span>
        <span>发布时间：{{ docData.createdTime }}</span>
      </div>
      <div v-if="docData.docUrl" class="doc-url">
        <span>原文链接：</span>
        <el-link type="primary" :href="docData.docUrl" target="_blank">
          {{ docData.docUrl }}
        </el-link>
      </div>
      <el-divider />
      <div class="doc-content">
        {{ docData.content || '暂无内容' }}
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getDocumentById } from '@/api/doc'
import { Document } from '@/api/doc'

const props = defineProps<{
  visible: boolean
  docId: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
}>()

const docData = ref<Document | null>(null)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

watch(() => props.visible, async (val) => {
  if (val && props.docId > 0) {
    try {
      docData.value = await getDocumentById(props.docId)
    } catch (error) {
      console.error('获取文档详情失败', error)
    }
  }
})
</script>

<style lang="scss" scoped>
.doc-detail {
  padding: 20px;
}

.doc-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.doc-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
  margin-bottom: 20px;
}

.doc-content {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
