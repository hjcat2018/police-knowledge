<template>
  <div class="template-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提示词模板管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>新建模板
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane :label="tabLabels.my" name="my">
          <TemplateList :templates="myTemplates" @edit="handleEdit" @delete="handleDelete" @set-default="handleSetDefault" />
        </el-tab-pane>
        <el-tab-pane :label="tabLabels.system" name="system">
          <TemplateList :templates="systemTemplates" :editable="false" />
        </el-tab-pane>
        <el-tab-pane :label="tabLabels.shared" name="shared">
          <TemplateList :templates="sharedTemplates" @copy="handleCopy" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <TemplateFormDialog
      v-model:visible="dialogVisible"
      :template="currentTemplate"
      :mode="dialogMode"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTemplates, createTemplate, deleteTemplate, setDefault, TemplateType, type PromptTemplate } from '@/api/promptTemplate'
import TemplateList from './components/TemplateList.vue'
import TemplateFormDialog from './components/TemplateFormDialog.vue'

const activeTab = ref('my')
const myTemplates = ref<PromptTemplate[]>([])
const systemTemplates = ref<PromptTemplate[]>([])
const sharedTemplates = ref<PromptTemplate[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const currentTemplate = ref<PromptTemplate | null>(null)

const tabLabels = {
  my: '我的模板',
  system: '系统模板',
  shared: '共享模板'
}

const loadTemplates = async () => {
  try {
    const [myRes, systemRes, sharedRes] = await Promise.all([
      getTemplates(TemplateType.MY),
      getTemplates(TemplateType.SYSTEM),
      getTemplates(TemplateType.SHARED)
    ])
    myTemplates.value = (myRes as unknown as PromptTemplate[]) || []
    systemTemplates.value = (systemRes as unknown as PromptTemplate[]) || []
    sharedTemplates.value = (sharedRes as unknown as PromptTemplate[]) || []
  } catch (error) {
    console.error('加载模板列表失败:', error)
    ElMessage.error('加载模板列表失败')
  }
}

const handleTabChange = () => {
  loadTemplates()
}

const showCreateDialog = () => {
  currentTemplate.value = null
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const handleEdit = (template: PromptTemplate) => {
  currentTemplate.value = template
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await deleteTemplate(id)
    ElMessage.success('删除成功')
    await loadTemplates()
  } catch (error) {
    console.error('删除模板失败:', error)
    ElMessage.error('删除模板失败')
  }
}

const handleSetDefault = async (id: number) => {
  try {
    await setDefault(id)
    ElMessage.success('设置成功')
    await loadTemplates()
  } catch (error) {
    console.error('设置默认模板失败:', error)
    ElMessage.error('设置默认模板失败')
  }
}

const handleCopy = async (template: PromptTemplate) => {
  try {
    await createTemplate({
      name: `${template.name} (副本)`,
      content: template.content,
      variables: template.variables,
      description: template.description
    })
    ElMessage.success('复制成功')
    await loadTemplates()
  } catch (error) {
    console.error('复制模板失败:', error)
    ElMessage.error('复制模板失败')
  }
}

const handleSubmit = async () => {
  await loadTemplates()
}

onMounted(loadTemplates)
</script>

<style lang="scss" scoped>
.template-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
