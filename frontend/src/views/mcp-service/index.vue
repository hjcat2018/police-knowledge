<template>
  <div class="mcp-service-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>MCP服务管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>添加服务
          </el-button>
        </div>
      </template>

      <el-table :data="services" v-loading="loading" stripe>
        <el-table-column prop="name" label="服务名称" min-width="120" />
        <el-table-column prop="code" label="服务编码" min-width="100" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="enabled" label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configUrl" label="API URL" min-width="150" show-overflow-tooltip />
        <el-table-column prop="configMethod" label="请求方法" min-width="80" align="center" />
        <el-table-column prop="sort" label="排序" min-width="60" align="center" />
        <el-table-column prop="createdTime" label="创建时间" min-width="150" />
        <el-table-column label="操作" min-width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              :type="row.enabled === 1 ? 'warning' : 'success'" 
              link 
              size="small" 
              @click="handleToggle(row)">
              {{ row.enabled === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogMode === 'add' ? '添加MCP服务' : '编辑MCP服务'" 
      width="600px"
      destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="服务编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入服务编码（唯一）" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入服务描述" />
        </el-form-item>
        <el-form-item label="API URL" prop="configUrl">
          <el-input v-model="formData.configUrl" placeholder="http://localhost:9000/mcp/service" />
        </el-form-item>
        <el-form-item label="认证方式" prop="configAuthType">
          <el-select v-model="formData.configAuthType" placeholder="请选择认证方式" style="width: 100%">
            <el-option label="API Key" value="api_key" />
            <el-option label="Bearer Token" value="bearer" />
            <el-option label="OAuth2" value="oauth2" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证凭证" prop="configCredentials">
          <el-input v-model="formData.configCredentials" type="password" show-password placeholder="请输入认证凭证" />
        </el-form-item>
        <el-form-item label="请求方法" prop="configMethod">
          <el-radio-group v-model="formData.configMethod">
            <el-radio value="GET">GET</el-radio>
            <el-radio value="POST">POST</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="超时时间" prop="configTimeout">
          <el-input-number v-model="formData.configTimeout" :min="1" :max="300" placeholder="秒" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="formData.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        
        <el-divider content-position="left">进程启动模式（可选）</el-divider>
        
        <el-form-item label="启动命令" prop="command">
          <el-input v-model="formData.command" placeholder="/usr/bin/python3" />
        </el-form-item>
        <el-form-item label="命令参数" prop="args">
          <el-input v-model="formData.args" type="textarea" :rows="2" placeholder="/path/to/server.py" />
        </el-form-item>
        <el-form-item label="环境变量" prop="env">
          <el-input v-model="formData.env" type="textarea" :rows="2" placeholder="KEY1=value1&#10;KEY2=value2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getMcpServices, createMcpService, updateMcpService, deleteMcpService, toggleMcpService, type McpService, type CreateMcpServiceRequest } from '@/api/mcpService'

const loading = ref(false)
const services = ref<McpService[]>([])
const dialogVisible = ref(false)
const dialogMode = ref<'add' | 'edit'>('add')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const formData = ref<CreateMcpServiceRequest>({
  name: '',
  code: '',
  description: '',
  enabled: 1,
  configUrl: '',
  configAuthType: 'api_key',
  configCredentials: '',
  configTimeout: 60,
  configMethod: 'POST',
  sort: 0,
  command: '',
  args: '',
  env: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入服务编码', trigger: 'blur' }]
}

const loadServices = async () => {
  loading.value = true
  try {
    const res = await getMcpServices()
    services.value = (res as unknown as McpService[]) || []
  } catch (error) {
    console.error('加载MCP服务列表失败:', error)
    ElMessage.error('加载MCP服务列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  formData.value = {
    name: '',
    code: '',
    description: '',
    enabled: 1,
    configUrl: '',
    configAuthType: 'api_key',
    configCredentials: '',
    configTimeout: 60,
    configMethod: 'POST',
    sort: 0,
    command: '',
    args: '',
    env: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row: McpService) => {
  dialogMode.value = 'edit'
  formData.value = {
    name: row.name,
    code: row.code,
    description: row.description || '',
    enabled: row.enabled,
    configUrl: row.configUrl || '',
    configAuthType: row.configAuthType || 'api_key',
    configCredentials: '',  // 编辑时不带回显
    configTimeout: row.configTimeout || 60,
    configMethod: row.configMethod || 'POST',
    sort: row.sort || 0,
    command: row.command || '',
    args: row.args || '',
    env: row.env || ''
  }
  dialogVisible.value = true
}

const handleToggle = async (row: McpService) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.enabled === 1 ? '禁用' : '启用'} "${row.name}" 吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await toggleMcpService(row.id)
    ElMessage.success('操作成功')
    await loadServices()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row: McpService) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${row.name}" 吗？此操作不可恢复。`,
      '确认删除',
      { type: 'warning' }
    )
    await deleteMcpService(row.id)
    ElMessage.success('删除成功')
    await loadServices()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogMode.value === 'add') {
          await createMcpService(formData.value)
          ElMessage.success('添加成功')
        } else {
          const editingRow = services.value.find(s => s.code === formData.value.code)
          if (editingRow) {
            await updateMcpService(editingRow.id, formData.value)
            ElMessage.success('更新成功')
          }
        }
        dialogVisible.value = false
        await loadServices()
      } catch (error: any) {
        console.error('保存失败:', error)
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => {
  loadServices()
})
</script>

<style lang="scss" scoped>
.mcp-service-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
