<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑文档' : '新建文档'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入文档标题" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="知识库" prop="kbId">
            <el-cascader
              v-model="formData.kbId"
              :options="kbTreeData"
              :props="{ value: 'id', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
              placeholder="请选择知识库"
              clearable
              filterable
              style="width: 100%"
              @change="handleKbChange" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="标签" prop="tags">
            <el-input
              v-model="formData.tags"
              placeholder="多个标签用逗号分隔" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="摘要" prop="summary">
        <el-input
          v-model="formData.summary"
          type="textarea"
          :rows="2"
          placeholder="请输入文档摘要" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="8"
          placeholder="请输入文档内容，或上传文件自动解析" />
      </el-form-item>

      <el-form-item label="上传文件">
        <el-upload
          ref="uploadRef"
          class="file-uploader"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :limit="1"
          accept=".doc,.docx,.xls,.xlsx,.pdf,.txt,.md">
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            选择文件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持 DOC, DOCX, XLS, XLSX, PDF, TXT, MD 文件
              <el-button
                v-if="uploadFile"
                type="primary"
                link
                :loading="parsingFile"
                @click="handleParseFile">
                解析文件
              </el-button>
              <span v-if="parseResult" class="parse-result">
                ✓ 已解析 {{ parseResult.length }} 字符
              </span>
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="作者" prop="author">
            <el-input v-model="formData.author" placeholder="请输入作者" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源时间" prop="sourceTime">
            <el-date-picker
              v-model="formData.sourceTime"
              type="datetime"
              placeholder="请选择来源时间"
              style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="归属地" prop="originScope">
            <el-select
              v-model="formData.originScope"
              placeholder="请选择归属地"
              clearable
              style="width: 100%">
              <el-option
                v-for="item in originScopeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="来源部门" prop="originDepartment">
            <el-cascader
              v-model="formData.originDepartment"
              :options="originDepartmentTree"
              :props="{ value: 'code', label: 'name', children: 'children', emitPath: false, checkStrictly: true }"
              placeholder="请选择来源部门"
              clearable
              filterable
              style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="封面" prop="cover">
        <el-input v-model="formData.cover" placeholder="请输入封面图片URL" />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="0">草稿</el-radio>
          <el-radio :value="1">发布</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        {{ loading ? '保存中...' : '确定' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue'
  import type { FormInstance, FormRules, UploadFile } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { Upload } from '@element-plus/icons-vue'
  import {
    createDocument,
    updateDocument,
    Document
  } from '@/api/doc'
  import { getKbCategoryList, getOriginScopeList, getOriginDepartmentList, type SysDict } from '@/api/dict'
  import { type OriginScope } from '@/types/api'

  interface ParseAndUploadResult {
    docUrl: string
    sourceTime: string
    content: string
  }

  function parseAndUploadFile(file: File): Promise<ParseAndUploadResult> {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/documents/parse-and-upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  import request from '@/utils/request'

  const props = defineProps<{
    visible: boolean
    docData: Document | null
    kbId?: number | null
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', visible: boolean): void
    (e: 'success'): void
  }>()

  const formRef = ref<FormInstance>()
  const uploadRef = ref()
  const loading = ref(false)
  const parsingFile = ref(false)
  const kbTreeData = ref<any[]>([])
  const originDepartmentTree = ref<any[]>([])
  const uploadFile = ref<UploadFile | null>(null)
  const parseResult = ref<string>('')
  const originScopeOptions = ref<{ value: string; label: string }[]>([])

  const isEdit = computed(() => !!props.docData?.id)

  const dialogVisible = computed({
    get: () => props.visible,
    set: val => emit('update:visible', val)
  })

  const formData = ref({
    title: '',
    content: '',
    summary: '',
    kbId: undefined as number | undefined,
    tags: '',
    author: '',
    source: '',
    cover: '',
    originScope: '' as OriginScope | '',
    originDepartment: '',
    status: 0 as 0 | 1,
    sourceTime: '',
    docUrl: ''
  })

  const formRules: FormRules = {
    title: [
      { required: true, message: '请输入文档标题', trigger: 'blur' },
      {
        min: 2,
        max: 200,
        message: '标题长度在2-200个字符之间',
        trigger: 'blur'
      }
    ],
    kbId: [{ required: true, message: '请选择知识库', trigger: 'change' }]
  }

  const fetchKbList = async () => {
    try {
      const kbList = await getKbCategoryList()
      kbTreeData.value = transformKbTree(kbList)
    } catch (error) {
      console.error('获取知识库分类列表失败', error)
    }
  }

  const transformKbTree = (list: SysDict[]): any[] => {
    return list.map(item => ({
      id: item.id,
      name: item.detail,
      children: item.children && item.children.length > 0 ? transformKbTree(item.children) : undefined
    }))
  }

  const handleKbChange = (value: any) => {
    if (value && value < 0) {
      formData.value.kbId = undefined
    }
  }

  const fetchOriginData = async () => {
    try {
      const [originScopeList, originDepartmentList] = await Promise.all([
        getOriginScopeList(1),
        getOriginDepartmentList(1)
      ])
      originScopeOptions.value = originScopeList.map(item => ({
        value: item.code,
        label: item.detail
      }))
      originDepartmentTree.value = buildDepartmentTree(originDepartmentList)
    } catch (error) {
      console.error('获取归属地或来源部门列表失败', error)
    }
  }

  const buildDepartmentTree = (list: SysDict[]): any[] => {
    const tree: any[] = []
    const map = new Map<string, any>()

    list.forEach(item => {
      map.set(item.code, {
        id: item.id,
        code: item.code,
        name: item.detail,
        children: []
      })
    })

    list.forEach(item => {
      const node = map.get(item.code)
      if (item.parentCode && map.has(item.parentCode)) {
        map.get(item.parentCode).children.push(node)
      } else {
        tree.push(node)
      }
    })

    return tree
  }

  watch(
    () => props.visible,
    val => {
      if (val) {
        fetchKbList()
        fetchOriginData()
        if (props.docData) {
          formData.value = {
            title: props.docData.title,
            content: props.docData.content || '',
            summary: props.docData.summary || '',
            kbId: props.docData.kbId || props.kbId || undefined,
            tags: props.docData.tags || '',
            author: props.docData.author || '',
            source: props.docData.source || '',
            cover: props.docData.cover || '',
            originScope: (props.docData.originScope as OriginScope) || '',
            originDepartment: props.docData.originDepartment || '',
            status: (props.docData.status ?? 1) as 0 | 1,
            sourceTime: props.docData.sourceTime || '',
            docUrl: props.docData.docUrl || ''
          }
        } else {
          formData.value.kbId = props.kbId || undefined
          formData.value.content = ''
          formData.value.summary = ''
          formData.value.tags = ''
          formData.value.author = ''
          formData.value.source = ''
          formData.value.cover = ''
          formData.value.originScope = ''
          formData.value.originDepartment = ''
          formData.value.status = 1
          formData.value.sourceTime = ''
          formData.value.docUrl = ''
        }
        uploadFile.value = null
        parseResult.value = ''
      }
    }
  )

  const handleFileChange = (file: UploadFile) => {
    uploadFile.value = file
    parseResult.value = ''
    // 自动填入文件名到标题
    if (file.name) {
      const fileNameWithoutExt = file.name.replace(/\.[^/.]+$/, '')
      formData.value.title = fileNameWithoutExt
    }
  }

  const handleFileRemove = () => {
    uploadFile.value = null
    parseResult.value = ''
  }

  const handleParseFile = async () => {
    if (!uploadFile.value) {
      ElMessage.warning('请先选择文件')
      return
    }

    const file = uploadFile.value.raw
    if (!file) {
      ElMessage.warning('文件无效')
      return
    }

    parsingFile.value = true
    try {
      const result = await parseAndUploadFile(file)
      parseResult.value = result.content
      formData.value.content = result.content
      formData.value.docUrl = result.docUrl
      if (result.sourceTime) {
        const d = new Date(result.sourceTime)
        const year = d.getFullYear()
        const month = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const hours = String(d.getHours()).padStart(2, '0')
        const minutes = String(d.getMinutes()).padStart(2, '0')
        const seconds = String(d.getSeconds()).padStart(2, '0')
        formData.value.sourceTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      }
      ElMessage.success('文件解析并上传完成')
    } catch (error) {
      ElMessage.error('文件解析或上传失败')
    } finally {
      parsingFile.value = false
    }
  }

  const handleClose = () => {
    emit('update:visible', false)
    formRef.value?.resetFields()
    uploadRef.value?.clearFiles()
    uploadFile.value = null
    parseResult.value = ''
  }

  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async valid => {
      if (!valid) return

      loading.value = true
      try {
        const submitData = { ...formData.value }
        if (submitData.sourceTime && typeof submitData.sourceTime === 'object' && 'getFullYear' in submitData.sourceTime) {
          const d = submitData.sourceTime as Date
          const year = d.getFullYear()
          const month = String(d.getMonth() + 1).padStart(2, '0')
          const day = String(d.getDate()).padStart(2, '0')
          const hours = String(d.getHours()).padStart(2, '0')
          const minutes = String(d.getMinutes()).padStart(2, '0')
          const seconds = String(d.getSeconds()).padStart(2, '0')
          submitData.sourceTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }
        if (isEdit.value) {
          await updateDocument(props.docData!.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await createDocument(submitData)
          ElMessage.success('创建成功')
        }
        emit('success')
        handleClose()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        loading.value = false
      }
    })
  }
</script>

<style lang="scss" scoped>
  .file-uploader {
    width: 100%;
  }

  .parse-result {
    margin-left: 10px;
    color: #67c23a;
    font-size: 12px;
  }
</style>
