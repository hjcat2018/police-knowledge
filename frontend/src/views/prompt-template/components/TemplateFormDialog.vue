<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'create' ? '新建模板' : '编辑模板'"
    width="600px"
    @close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="模板名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入模板名称" />
      </el-form-item>
      
      <el-form-item label="模板内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="请输入模板内容，使用 {{变量名}} 表示占位符"
          @input="handleContentChange"
        />
        <div class="template-tips">
          <span class="tips-label">可用变量：</span>
          <el-tag 
            v-for="v in availableVariables" 
            :key="v" 
            size="small" 
            class="variable-tag"
            :class="{ active: form.content.includes(formatVariable(v)) }"
          >
            {{ formatVariable(v) }}
          </el-tag>
        </div>
      </el-form-item>
      
      <el-form-item label="模板描述">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入模板描述（可选）" />
      </el-form-item>
      
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
        <span class="sort-tip">数字越小越靠前</span>
      </el-form-item>
      
      <el-form-item v-if="mode === 'create'">
        <el-checkbox v-model="form.isShared">共享给其他用户</el-checkbox>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { createTemplate, createSharedTemplate, updateTemplate, type PromptTemplate } from '@/api/promptTemplate'

const props = defineProps<{
  visible: boolean
  template: PromptTemplate | null
  mode: 'create' | 'edit'
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'submit'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  name: '',
  content: '',
  description: '',
  sort: 0,
  isShared: false
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 100, message: '模板名称不能超过100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入模板内容', trigger: 'blur' },
    { min: 1, message: '模板内容不能为空', trigger: 'blur' }
  ]
}

const predefinedVariables = ['content', 'question', 'language', 'username', 'date']

const formatVariable = (v: string) => `{{${v}}}`

const availableVariables = computed(() => {
  const variables = new Set<string>(predefinedVariables)
  const content = form.content
  const matches = content.match(/\{\{(\w+)\}\}/g) || []
  matches.forEach(m => {
    variables.add(m.replace(/[{}]/g, ''))
  })
  return Array.from(variables)
})

const handleContentChange = () => {
  formRef.value?.clearValidate('content')
}

watch(() => props.template, (template) => {
  if (template) {
    form.name = template.name
    form.content = template.content
    form.description = template.description || ''
    form.sort = template.sort || 0
  }
}, { immediate: true })

const handleClose = () => {
  emit('update:visible', false)
  formRef.value?.resetFields()
}

const extractVariables = (content: string): string => {
  const variables = new Set<string>()
  const matches = content.match(/\{\{(\w+)\}\}/g) || []
  matches.forEach(m => {
    variables.add(m.replace(/[{}]/g, ''))
  })
  return JSON.stringify(Array.from(variables))
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true
    
    const variables = extractVariables(form.content)
    const data = {
      name: form.name,
      content: form.content,
      variables,
      description: form.description,
      sort: form.sort
    }
    
    if (props.mode === 'create') {
      if (form.isShared) {
        await createSharedTemplate(data)
        ElMessage.success('创建成功，模板已共享')
      } else {
        await createTemplate(data)
        ElMessage.success('创建成功')
      }
    } else {
      await updateTemplate(props.template!.id, data)
      ElMessage.success('更新成功')
      emit('submit')
    }
    emit('submit')
    emit('submit')
    handleClose()
  } catch (error) {
    console.error('保存模板失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.template-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;

  .tips-label {
    font-size: 12px;
    color: #909399;
  }

  .variable-tag {
    cursor: pointer;
    transition: all 0.2s;

    &.active {
      background: #409eff;
      color: #fff;
      border-color: #409eff;
    }

    &:hover {
      transform: scale(1.05);
    }
  }
}

.sort-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
