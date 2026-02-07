<template>
  <div class="model-selector">
    <el-popover
      placement="bottom-start"
      :width="400"
      trigger="click"
      @show="showPopover = true"
      @hide="showPopover = false">
      <template #reference>
        <el-button size="small" :type="selectedModels.length > 0 ? 'success' : 'default'">
          <el-icon><Grid /></el-icon>
          模型 {{ selectedModels.length > 0 ? `(${selectedModels.length})` : '' }}
          <el-icon class="popover-arrow"><ArrowDown v-if="!showPopover" /><ArrowUp v-else /></el-icon>
        </el-button>
      </template>
      <div class="model-selector-content">
        <div class="model-header">
          <span>选择模型 (最多 {{ maxModels }} 个)</span>
          <el-button size="small" text @click="clearSelection" v-if="selectedModels.length > 0">
            清空
          </el-button>
        </div>
        <div class="model-list">
          <div
            v-for="model in availableModels"
            :key="model.id"
            class="model-item"
            :class="{ selected: selectedModels.includes(model.id), disabled: !selectedModels.includes(model.id) && selectedModels.length >= maxModels }"
            @click.stop="toggleModel(model.id)">
            <div class="model-info">
              <el-icon><Cpu /></el-icon>
              <span class="model-name">{{ model.name }}</span>
              <el-tag type="success" size="small" effect="plain">{{ model.provider }}</el-tag>
            </div>
            <div class="model-desc">{{ model.description }}</div>
            <div class="model-check" v-if="selectedModels.includes(model.id)">
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </el-popover>
    <div class="selected-models" v-if="selectedModels.length > 0">
      <el-tag
        v-for="modelId in selectedModels"
        :key="modelId"
        type="success"
        size="small"
        closable
        @close="removeModel(modelId)">
        {{ getModelName(modelId) }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Grid, ArrowDown, ArrowUp, Cpu, CircleCheck } from '@element-plus/icons-vue'

  interface Model {
    id: string
    name: string
    provider: string
    description: string
  }

  const props = defineProps<{
    modelValue: string[]
    maxModels?: number
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string[]): void
    (e: 'change', value: string[]): void
  }>()

  const showPopover = ref(false)
  const localSelected = ref<string[]>([])

  onMounted(() => {
    localSelected.value = [...props.modelValue]
  })

  watch(() => props.modelValue, (newVal) => {
    localSelected.value = [...newVal]
  }, { deep: true })

  const selectedModels = computed({
    get: () => localSelected.value,
    set: (value) => {
      localSelected.value = value
      emit('update:modelValue', value)
    }
  })

  const availableModels: Model[] = [
    { id: 'deepseek-reasoner', name: 'DeepSeek Reasoner', provider: 'OpenAI', description: '深度求索思考对话模型' },
    { id: 'deepseek-chat', name: 'Deepseek Chat', provider: 'OpenAI', description: '擅长多轮交互与内容生成' },
    { id: 'claude-3-opus', name: 'Claude 3 Opus', provider: 'Anthropic', description: '长上下文理解能力强' },
    { id: 'claude-3-sonnet', name: 'Claude 3 Sonnet', provider: 'Anthropic', description: '平衡性能与成本' },
    { id: 'ERNIE-4.5', name: '文心一言 4.5', provider: '百度', description: '中文理解能力强' },
    { id: 'ERNIE-4', name: '文心一言 4', provider: '百度', description: '快速中文响应' },
    { id: 'Spark', name: '讯飞星火', provider: '科大讯飞', description: '多模态交互能力' },
    { id: 'glm-4.7', name: 'GLM-4.7', provider: 'ZhipuAI', description: '编码能力、长程任务规划与工具协同模型' }
  ]

  const maxModels = computed(() => props.maxModels || 6)

  const getModelName = (modelId: string) => {
    const model = availableModels.find(m => m.id === modelId)
    return model ? model.name : modelId
  }

  const toggleModel = (modelId: string) => {
    const index = selectedModels.value.indexOf(modelId)
    if (index > -1) {
      selectedModels.value = selectedModels.value.filter(id => id !== modelId)
    } else {
      if (selectedModels.value.length >= maxModels.value) {
        ElMessage.warning(`最多选择 ${maxModels.value} 个模型`)
        return
      }
      selectedModels.value = [...selectedModels.value, modelId]
    }
    emit('change', selectedModels.value)
  }

  const removeModel = (modelId: string) => {
    selectedModels.value = selectedModels.value.filter(id => id !== modelId)
    emit('change', selectedModels.value)
  }

  const clearSelection = () => {
    selectedModels.value = []
    emit('change', selectedModels.value)
  }
</script>

<style lang="scss" scoped>
  .model-selector {
    display: flex;
    align-items: center;
    gap: 8px;

    .popover-arrow {
      margin-left: 4px;
      font-size: 12px;
    }

    .selected-models {
      display: flex;
      gap: 4px;
      flex-wrap: wrap;
    }
  }

  .model-selector-content {
    .model-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 12px;
      border-bottom: 1px solid #e4e7ed;
      margin-bottom: 12px;
      font-weight: 500;
    }

    .model-list {
      max-height: 300px;
      overflow-y: auto;

      .model-item {
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

        &.selected {
          border-color: #67c23a;
          background: #f0f9eb;

          .model-check {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
          }
        }

        &.disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .model-info {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 4px;

          .model-name {
            font-weight: 500;
            flex: 1;
          }
        }

        .model-desc {
          font-size: 12px;
          color: #909399;
          padding-left: 24px;
        }
      }
    }
  }
</style>
