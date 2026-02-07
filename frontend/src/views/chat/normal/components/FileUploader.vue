<template>
  <div class="file-uploader">
    <div class="panel-header">
      <el-icon><Upload /></el-icon>
      <span>文件上传</span>
      <el-tag type="info" size="small">最大 100MB</el-tag>
      <div class="mode-switch">
        <el-switch
          v-model="useSummaryMode"
          active-text="摘要模式"
          inactive-text="完整模式"
          size="small" />
      </div>
    </div>
    <el-upload
      ref="uploadRef"
      class="upload-area"
      drag
      :auto-upload="false"
      :limit="maxFiles"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      multiple
      accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.md">
      <el-icon size="40" color="#909399"><UploadFilled /></el-icon>
      <div class="upload-text">
        <span>拖拽文件到此处，或 <em>点击上传</em></span>
        <span class="upload-hint">支持 PDF、Word、Excel、TXT、Markdown</span>
      </div>
    </el-upload>
    <div class="file-list" v-if="files.length > 0">
      <el-divider content-position="left">
        <span>已选择文件 ({{ files.length }})</span>
        <el-tag v-if="useSummaryMode" type="warning" size="small" style="margin-left: 8px">
          摘要模式
        </el-tag>
      </el-divider>
      <div class="file-item" v-for="(file, index) in files" :key="file.uid || index">
        <div class="file-icon">
          <el-icon :size="20"><Document /></el-icon>
        </div>
        <div class="file-info">
          <div class="file-name">{{ file.name }}</div>
          <div class="file-size">{{ file.size }}</div>
        </div>
        <div class="file-status" v-if="useSummaryMode">
          <el-tag
            v-if="file.summaryGenerated"
            type="success"
            size="small">
            已摘要
          </el-tag>
          <el-tag
            v-else-if="file.processing"
            type="warning"
            size="small"
            :loading="file.processing">
            摘要中...
          </el-tag>
          <el-tag
            v-else
            type="info"
            size="small"
            class="clickable"
            @click="generateSummaryForFile(file)">
            未摘要
          </el-tag>
        </div>
        <div class="upload-status" v-else-if="file.uploading">
          <el-icon class="loading"><Loading /></el-icon>
          <span>上传中...</span>
        </div>
        <div class="upload-status success" v-else-if="file.uploaded">
          <el-icon><CircleCheck /></el-icon>
          <span>已上传</span>
        </div>
        <div class="file-actions">
          <el-button size="small" type="danger" text circle @click="removeFile(index)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import { Upload, UploadFilled, Document, Delete, Loading, CircleCheck } from '@element-plus/icons-vue'
  import { uploadFile } from '@/api/conversation'
  import { generateSummarySync } from '@/api/summary'
  import type { UploadFile } from 'element-plus'

  interface UploadFileData {
    name: string
    size: string
    type: string
    url?: string
    content?: string
    uploading?: boolean
    uploaded?: boolean
    summaryGenerated?: boolean
    processing?: boolean
    docId?: number
    uid?: number
  }

  const props = defineProps<{
    files: UploadFileData[]
  }>()

  void props

  const useSummaryMode = ref(false)

  const uploadRef = ref()

  const emit = defineEmits<{
    (e: 'update:files', files: UploadFileData[]): void
  }>()

  const generateSummaryForFile = async (file: UploadFileData) => {
    if (!file.content) {
      ElMessage.warning('请先上传文件')
      return
    }

    const updatedFiles = props.files.map(f => {
      if (f.name === file.name) {
        return { ...f, processing: true }
      }
      return f
    })
    emit('update:files', updatedFiles)

    try {
      await generateSummarySync(file.content)
      const finalFiles = props.files.map(f => {
        if (f.name === file.name) {
          return { ...f, processing: false, summaryGenerated: true }
        }
        return f
      })
      emit('update:files', finalFiles)
      ElMessage.success('摘要生成成功')
    } catch (error: any) {
      const errorFiles = props.files.map(f => {
        if (f.name === file.name) {
          return { ...f, processing: false }
        }
        return f
      })
      emit('update:files', errorFiles)
      ElMessage.error(`摘要生成失败: ${error.message || '未知错误'}`)
    }
  }

  const maxFiles = 5
  let uidCounter = 0

  const handleFileChange = async (elUploadFile: UploadFile, _uploadFiles: any) => {
    // 检查文件是否已经在列表中
    const fileName = elUploadFile.name
    const exists = props.files.some(f => f.name === fileName)
    if (exists) {
      // 清除 el-upload 中的重复文件
      uploadRef.value?.clearFiles()
      ElMessage.warning('文件已存在')
      return
    }

    if (elUploadFile.raw) {
      const newFileData: UploadFileData = {
        name: elUploadFile.raw.name,
        size: elUploadFile.raw.size > 1024 * 1024
          ? `${(elUploadFile.raw.size / (1024 * 1024)).toFixed(2)} MB`
          : `${(elUploadFile.raw.size / 1024).toFixed(2)} KB`,
        type: elUploadFile.raw.type,
        uploading: true,
        uid: ++uidCounter
      }

      // 先添加到列表
      const currentFiles = [...props.files, newFileData]
      emit('update:files', currentFiles)

      try {
        const result = await uploadFile(elUploadFile.raw!)
        const updatedFiles = props.files.map(f => {
          if (f.uid === newFileData.uid) {
            return {
              ...f,
              url: result.docUrl,
              content: result.content,
              uploading: false,
              uploaded: true
            }
          }
          return f
        })
        emit('update:files', updatedFiles)
        ElMessage.success(`文件 ${elUploadFile.name} 上传成功`)
        // 清除 el-upload 中的文件（因为我们已经手动管理了）
        uploadRef.value?.clearFiles()
      } catch (error: any) {
        // 上传失败，从列表移除
        const updatedFiles = props.files.filter(f => f.uid !== newFileData.uid)
        emit('update:files', updatedFiles)
        uploadRef.value?.clearFiles()
        ElMessage.error(`文件 ${elUploadFile.name} 上传失败: ${error.message || '未知错误'}`)
      }
    }
  }

  const handleExceed = () => {
    ElMessage.warning(`最多只能上传 ${maxFiles} 个文件`)
  }

  const handleRemove = (file: UploadFile) => {
    const index = props.files.findIndex(f => f.name === file.name || f.uid === file.uid)
    if (index > -1) {
      const updatedFiles = [...props.files]
      updatedFiles.splice(index, 1)
      emit('update:files', updatedFiles)
    }
  }

  const removeFile = (index: number) => {
    const updatedFiles = [...props.files]
    updatedFiles.splice(index, 1)
    emit('update:files', updatedFiles)
  }
</script>

<style lang="scss" scoped>
  .file-uploader {
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

    .upload-area {
      width: 100%;

      :deep(.el-upload-dragger) {
        padding: 20px;
        border-radius: 8px;
      }

      .upload-text {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        margin-top: 8px;

        em {
          color: #409eff;
          font-style: normal;
        }

        .upload-hint {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .file-list {
      margin-top: 16px;

      :deep(.el-divider__text) {
        font-size: 12px;
        color: #909399;
      }

      .file-item {
        display: flex;
        align-items: center;
        padding: 8px;
        border-radius: 6px;
        background: #f5f7fa;
        margin-bottom: 8px;

        .file-icon {
          margin-right: 12px;
          color: #409eff;
        }

        .file-info {
          flex: 1;
          overflow: hidden;

          .file-name {
            font-size: 14px;
            font-weight: 500;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .file-size {
            font-size: 12px;
            color: #909399;
          }
        }

        .upload-status {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #409eff;
          margin-right: 8px;

          .loading {
            animation: spin 1s linear infinite;
          }

          &.success {
            color: #67c23a;
          }
        }

        .file-actions {
          margin-left: 8px;
        }
      }
    }
  }

  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
</style>
