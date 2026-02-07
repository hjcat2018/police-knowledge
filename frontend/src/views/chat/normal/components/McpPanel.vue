<template>
  <div class="mcp-panel">
    <div class="panel-header">
      <el-icon><Connection /></el-icon>
      <span>MCP 服务</span>
    </div>
    <div class="service-list" v-loading="loading">
      <div
        v-for="service in services"
        :key="service.id"
        class="service-item"
        :class="{ active: selectedServices.includes(service.id), disabled: service.enabled !== 1 }"
        @click="toggleService(service)">
        <div class="service-status">
          <el-tag :type="service.enabled === 1 ? 'success' : 'info'" size="small" effect="plain">
            {{ service.enabled === 1 ? '在线' : '离线' }}
          </el-tag>
        </div>
        <div class="service-info">
          <div class="service-name">{{ service.name }}</div>
          <div class="service-desc">{{ service.description || '暂无描述' }}</div>
        </div>
        <div class="service-check" v-if="selectedServices.includes(service.id)">
          <el-icon color="#67c23a"><CircleCheck /></el-icon>
        </div>
      </div>
      <el-empty v-if="!loading && services.length === 0" description="暂无MCP服务" :image-size="60" />
    </div>
    <div class="panel-footer">
      <el-button size="small" type="primary" plain @click="refreshServices" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
      <el-button size="small" type="success" plain @click="addService">
        <el-icon><Plus /></el-icon>
        添加
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Connection, CircleCheck, Refresh, Plus } from '@element-plus/icons-vue'
import { getMcpServices, type McpService } from '@/api/mcpService'
import router from '@/router'

const props = defineProps<{
  modelValue: number[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', ids: number[]): void
  (e: 'select', service: McpService): void
}>()

const services = ref<McpService[]>([])
const loading = ref(false)

const selectedServices = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

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

const toggleService = (service: McpService) => {
  if (service.enabled !== 1) {
    ElMessage.warning('该服务当前不可用')
    return
  }
  const index = selectedServices.value.indexOf(service.id)
  if (index > -1) {
    selectedServices.value = selectedServices.value.filter(id => id !== service.id)
  } else {
    selectedServices.value = [...selectedServices.value, service.id]
  }
  emit('update:modelValue', selectedServices.value)
  emit('select', service)
}

const refreshServices = async () => {
  await loadServices()
  ElMessage.success('刷新成功')
}

const addService = () => {
  router.push('/system/mcp')
}

onMounted(() => {
  loadServices()
})
</script>

<style lang="scss" scoped>
.mcp-panel {
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

  .service-list {
    max-height: 400px;
    overflow-y: auto;

    .service-item {
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      margin-bottom: 8px;
      border: 1px solid #e4e7ed;
      position: relative;

      &:hover {
        background: #f5f7fa;
      }

      &.active {
        border-color: #67c23a;
        background: #f0f9eb;
      }

      &.disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .service-status {
        margin-bottom: 8px;
      }

      .service-info {
        .service-name {
          font-weight: 500;
          margin-bottom: 4px;
        }

        .service-desc {
          font-size: 12px;
          color: #909399;
        }
      }

      .service-check {
        position: absolute;
        right: 12px;
        top: 50%;
        transform: translateY(-50%);
      }
    }
  }

  .panel-footer {
    display: flex;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #e4e7ed;
  }
}
</style>
