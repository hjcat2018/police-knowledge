<template>
  <el-dialog v-model="dialogVisible" title="导入字典数据" width="500px" :close-on-click-modal="false" @close="handleClose">
    <div v-if="!importResult">
      <el-upload ref="uploadRef" class="upload-area" :auto-upload="false" :on-change="handleFileChange" :on-remove="handleFileRemove" :limit="1" accept=".xlsx,.xls">
        <el-button type="primary"><el-icon><Upload /></el-icon>选择Excel文件</el-button>
        <template #tip>
          <div class="el-upload__tip">支持 .xlsx, .xls 格式</div>
        </template>
      </el-upload>
      <div class="template-download">
        <el-button type="info" link @click="downloadTemplate"><el-icon><Download /></el-icon>下载导入模板</el-button>
      </div>
    </div>
    <el-result v-else :icon="importResult.errors.length > 0 ? 'warning' : 'success'" :title="importResult.successCount + '条数据导入成功'">
      <template #sub-title>
        <span v-if="importResult.skipCount > 0">跳过 {{ importResult.skipCount }} 条错误数据</span>
      </template>
      <template #extra>
        <el-button v-if="importResult.errors.length > 0" @click="showErrors = true">查看错误详情</el-button>
        <el-button type="primary" @click="handleClose">完成</el-button>
      </template>
    </el-result>
    <el-dialog v-model="showErrors" title="导入错误详情" width="600px" append-to-body>
      <el-table :data="importResult?.errors || []" max-height="400">
        <el-table-column prop="error" label="错误信息" />
      </el-table>
    </el-dialog>
    <template #footer v-if="!importResult">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="uploading" :disabled="!uploadFile" @click="handleUpload">{{ uploading ? '导入中...' : '开始导入' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { UploadFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Upload, Download } from '@element-plus/icons-vue'
import { importDict, getImportTemplate, type ImportResult } from '@/api/dict'

const props = defineProps<{
  visible: boolean
  kind?: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const uploadRef = ref()
const uploadFile = ref<UploadFile | null>(null)
const uploading = ref(false)
const importResult = ref<ImportResult | null>(null)
const showErrors = ref(false)

const handleFileChange = (file: UploadFile) => { uploadFile.value = file }
const handleFileRemove = () => { uploadFile.value = null }

const downloadTemplate = async () => {
  try {
    const template = await getImportTemplate()
    const headerNames = ['字典类型', '编码', '名称', '父级编码', '层级', '排序']
    const csvContent = [headerNames.join(','), ...template.map((row: any) => [row.kind, row.code, row.detail, row.parentCode || '', row.level, row.sort].join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = 'dict_import_template.csv'
    link.click()
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}

const handleUpload = async () => {
  if (!uploadFile.value?.raw) { ElMessage.warning('请选择文件'); return }
  if (!props.kind) { ElMessage.warning('请先选择字典类型'); return }
  uploading.value = true
  try {
    importResult.value = await importDict(props.kind, uploadFile.value.raw)
    if (importResult.value.successCount > 0) emit('success')
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    uploading.value = false
  }
}

const handleClose = () => {
  uploadFile.value = null
  importResult.value = null
  showErrors.value = false
  uploadRef.value?.clearFiles()
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
.upload-area {
  width: 100%;
  text-align: center;
  padding: 40px 0;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
}
.template-download {
  margin-top: 16px;
  text-align: center;
}
</style>
