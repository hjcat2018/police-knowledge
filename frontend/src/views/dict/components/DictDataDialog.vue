<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑字典' : '新增字典'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
     	<el-form-item v-if="formData.parentCode" label="父节点编码" prop="parentCode">
        <el-input v-model="formData.parentCode" placeholder="无" disabled style="width: 100%" />
      </el-form-item>
      <el-form-item label="字典编码" prop="code">
        <el-input v-model="formData.code" placeholder="如：national" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="字典名称" prop="detail">
        <el-input v-model="formData.detail" placeholder="如：国家级" />
      </el-form-item>
      <el-form-item label="别名">
        <el-input v-model="formData.alias" placeholder="别名（可选）" />
      </el-form-item>
       	<el-form-item  label="层级" prop="level">
	        <el-input-number v-model="formData.level" :min="1" :max="10" controls-position="right" />
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
      <el-form-item label="是否叶子">
        <el-switch v-model="formData.leaf" :active-value="1" :inactive-value="0" />
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
      <el-form-item label="系统内置">
        <el-switch v-model="formData.isSystem" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="公共数据">
        <el-switch v-model="formData.isPublic" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="备注（可选）" />
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
import { createDict, updateDict, type SysDict, type DictDataDTO } from '@/api/dict'

const props = defineProps<{
  visible: boolean
  data: SysDict | null
  kind?: string
  parentOptions: SysDict[]
  parentId?: number | null
  parentCode?: string | null
  parentLevel?: number | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!props.data?.id)

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = ref<DictDataDTO>({
  kind: '',
  code: '',
  detail: '',
  alias: '',
  description: '',
  icon: '',
  color: '',
  level: 1,
  parentId: null,
  parentCode: null,
  sort: 1,
  status: 1,
  leaf: 0,
  isSystem: 0,
  isPublic: 1,
  remark: ''
})

const formRules: FormRules = {
  code: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入字典名称', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择层级', trigger: 'change' }
  ]
}

watch(() => props.visible, (val) => {
  if (val) {
    formData.value.kind = props.kind || ''
    if (props.data) {
      formData.value = {
        kind: props.data.kind,
        code: props.data.code,
        detail: props.data.detail,
        alias: props.data.alias || '',
        description: props.data.description || '',
        icon: props.data.icon || '',
        color: props.data.color || '',
        level: props.data.level,
        parentId: props.data.parentId || null,
        parentCode: props.data.parentCode || null,
        sort: props.data.sort || 1,
        status: props.data.status || 1,
        leaf: props.data.leaf ?? 0,
        isSystem: props.data.isSystem ?? 0,
        isPublic: props.data.isPublic ?? 1,
        remark: props.data.remark || ''
      }
    } else {
      console.log('Dialog opened, parentCode:', props.parentCode, 'parentLevel:', props.parentLevel)
      const hasParent = !!props.parentCode
      formData.value = {
        kind: props.kind || '',
        code: '',
        detail: '',
        alias: '',
        description: '',
        icon: '',
        color: '',
        level: hasParent ? (props.parentLevel || 1) + 1 : 1,
        parentId: props.parentId || null,
        parentCode: props.parentCode || null,
        sort: 1,
        status: 1,
        leaf: 0,
        isSystem: 0,
        isPublic: 1,
        remark: ''
      }
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
        await updateDict(props.data!.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createDict(formData.value)
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
