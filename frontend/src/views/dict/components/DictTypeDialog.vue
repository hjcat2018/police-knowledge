<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑字典类型' : '新增字典类型'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px">
      <el-form-item label="类型编码" prop="kind">
        <el-input v-model="formData.kind" placeholder="如：origin_scope" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="类型名称" prop="detail">
        <el-input v-model="formData.detail" placeholder="如：归属地" />
      </el-form-item>
      <el-form-item label="别名">
        <el-input v-model="formData.alias" placeholder="别名（可选）" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="描述（可选）" />
      </el-form-item>
      <el-form-item label="图标">
        <el-input v-model="formData.icon" placeholder="图标class（可选）" />
      </el-form-item>
      <el-form-item label="颜色">
        <el-input v-model="formData.color" placeholder="颜色值（可选）" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="1" :max="999" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
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
import { createDictType, updateDictType, type SysDict, type DictTypeDTO } from '@/api/dict'

const props = defineProps<{
  visible: boolean
  typeData: SysDict | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!props.typeData?.id)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = ref<DictTypeDTO>({
  kind: '',
  detail: '',
  alias: '',
  description: '',
  icon: '',
  color: '',
  sort: 1,
  status: 1
})

const formRules: FormRules = {
  kind: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    { pattern: /^[a-z_]+$/, message: '编码只能包含小写字母和下划线', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入类型名称', trigger: 'blur' }
  ]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.typeData) {
      formData.value = {
        kind: props.typeData.kind,
        detail: props.typeData.detail,
        alias: props.typeData.alias || '',
        description: props.typeData.description || '',
        icon: props.typeData.icon || '',
        color: props.typeData.color || '',
        sort: props.typeData.sort || 1,
        status: props.typeData.status || 1
      }
    } else {
      formData.value = { kind: '', detail: '', alias: '', description: '', icon: '', color: '', sort: 1, status: 1 }
    }
  }
})

const handleClose = () => {
  formRef.value?.resetFields()
  dialogVisible.value = false
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (isEdit.value) {
        await updateDictType(props.typeData!.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createDictType(formData.value)
        ElMessage.success('创建成功')
      }
      emit('success')
      handleClose()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      loading.value = false
    }
  })
}
</script>
