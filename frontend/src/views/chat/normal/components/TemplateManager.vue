<template>
  <el-dialog
    v-model="visible"
    title="模板管理"
    width="600px"
    destroy-on-close>
    <div class="template-manager">
      <div class="toolbar">
        <el-button type="primary" @click="addTemplate">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
      </div>
      <el-table :data="localTemplates" style="width: 100%" max-height="400">
        <el-table-column prop="name" label="模板名称" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small" effect="plain">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="150">
          <template #default="{ row, $index }">
            <el-button size="small" type="primary" text @click="editTemplate(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button size="small" type="danger" text @click="deleteTemplate($index)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="showEditDialog"
    :title="isEditing ? '编辑模板' : '新建模板'"
    width="500px"
    destroy-on-close>
    <el-form :model="editForm" label-width="80px">
      <el-form-item label="名称">
        <el-input v-model="editForm.name" placeholder="请输入模板名称" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="editForm.type" placeholder="选择类型" style="width: 100%">
          <el-option label="文本处理" value="text" />
          <el-option label="翻译" value="translate" />
          <el-option label="案例分析" value="case" />
          <el-option label="法规解读" value="regulation" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="editForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
      </el-form-item>
      <el-form-item label="模板内容">
        <el-input
          v-model="editForm.content"
          type="textarea"
          :rows="6"
          placeholder="使用 {{question}} 表示用户问题，{{content}} 表示内容，{{language}} 表示语言等变量" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showEditDialog = false">取消</el-button>
      <el-button type="primary" @click="saveTemplate">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Plus, Edit, Delete } from '@element-plus/icons-vue'

  interface PromptTemplate {
    id: number
    name: string
    content: string
    description: string
    type: string
  }

  const props = defineProps<{
    modelValue: boolean
    templates: PromptTemplate[]
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void
    (e: 'save', template: PromptTemplate): void
  }>()

  const visible = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })

  const localTemplates = ref<PromptTemplate[]>([...props.templates])
  const showEditDialog = ref(false)
  const isEditing = ref(false)
  const editIndex = ref(-1)
  const editForm = ref<PromptTemplate>({
    id: 0,
    name: '',
    content: '',
    description: '',
    type: 'text'
  })

  watch(() => props.templates, (newVal) => {
    localTemplates.value = [...newVal]
  }, { deep: true })

  const getTypeColor = (type: string): 'success' | 'warning' | 'danger' | 'info' => {
    const colors: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
      text: 'info',
      translate: 'success',
      case: 'warning',
      regulation: 'danger'
    }
    return colors[type] || 'info'
  }

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      text: '文本',
      translate: '翻译',
      case: '案例',
      regulation: '法规'
    }
    return labels[type] || '通用'
  }

  const addTemplate = () => {
    isEditing.value = false
    editIndex.value = -1
    editForm.value = {
      id: Date.now(),
      name: '',
      content: '',
      description: '',
      type: 'text'
    }
    showEditDialog.value = true
  }

  const editTemplate = (template: PromptTemplate) => {
    isEditing.value = true
    editIndex.value = localTemplates.value.findIndex(t => t.id === template.id)
    editForm.value = { ...template }
    showEditDialog.value = true
  }

  const deleteTemplate = async (index: number) => {
    try {
      await ElMessageBox.confirm('确定要删除这个模板吗？', '提示', { type: 'warning' })
      localTemplates.value.splice(index, 1)
      ElMessage.success('删除成功')
    } catch {}
  }

  const saveTemplate = () => {
    if (!editForm.value.name || !editForm.value.content) {
      ElMessage.warning('请填写模板名称和内容')
      return
    }

    if (isEditing.value && editIndex.value > -1) {
      localTemplates.value[editIndex.value] = { ...editForm.value }
    } else {
      localTemplates.value.push({ ...editForm.value })
    }

    showEditDialog.value = false
    emit('save', editForm.value)
    ElMessage.success('保存成功')
  }
</script>

<style lang="scss" scoped>
  .template-manager {
    .toolbar {
      margin-bottom: 16px;
    }
  }
</style>
