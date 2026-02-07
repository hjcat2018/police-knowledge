<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑知识库' : '新建知识库'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
    >
      <el-form-item label="名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入知识库名称" />
      </el-form-item>

      <el-form-item label="编码" prop="code">
        <el-input v-model="formData.code" :disabled="isEdit" placeholder="请输入知识库编码" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="formData.categoryId" placeholder="请选择分类" clearable style="width: 100%">
          <el-option label="法律法规" :value="1" />
          <el-option label="业务规范" :value="2" />
          <el-option label="操作指南" :value="3" />
          <el-option label="常见问题" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="是否公开" prop="isPublic">
        <el-radio-group v-model="formData.isPublic">
          <el-radio :value="0">私有</el-radio>
          <el-radio :value="1">公开</el-radio>
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
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createKnowledgeBase, updateKnowledgeBase, KnowledgeBase } from '@/api/kb'

const props = defineProps<{
  visible: boolean
  kbData: KnowledgeBase | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = computed(() => !!props.kbData?.id)
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = ref({
  name: '',
  code: '',
  description: '',
  categoryId: undefined as number | undefined,
  status: 1 as 0 | 1,
  isPublic: 0 as 0 | 1
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入知识库名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度在2-100个字符之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入知识库编码', trigger: 'blur' },
    { min: 2, max: 50, message: '编码长度在2-50个字符之间', trigger: 'blur' }
  ]
}

watch(() => props.visible, (val) => {
  if (val && props.kbData) {
    formData.value = {
      name: props.kbData.name,
      code: props.kbData.code,
      description: props.kbData.description || '',
      categoryId: props.kbData.categoryId || undefined,
      status: (props.kbData.status ?? 1) as 0 | 1,
      isPublic: (props.kbData.isPublic ?? 0) as 0 | 1
    }
  } else if (val && !props.kbData) {
    formData.value = {
      name: '',
      code: '',
      description: '',
      categoryId: undefined,
      status: 1,
      isPublic: 0
    }
  }
})

const handleClose = () => {
  emit('update:visible', false)
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      if (isEdit.value) {
        await updateKnowledgeBase(props.kbData!.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createKnowledgeBase(formData.value)
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
