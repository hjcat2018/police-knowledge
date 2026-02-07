<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" :disabled="isEdit" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="警号" prop="policeNo">
        <el-input v-model="formData.policeNo" placeholder="请输入警号" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { createUser, updateUser } from '@/api/user'
import { User } from '@/types/api'

const props = defineProps<{
  visible: boolean
  userData: User | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = computed(() => !!props.userData?.id)
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formData = ref({
  username: '',
  realName: '',
  phone: '',
  policeNo: '',
  status: 1 as 0 | 1,
  remark: ''
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在2-50个字符之间', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

watch(() => props.visible, (val) => {
  if (val && props.userData) {
    formData.value = {
      username: props.userData.username,
      realName: props.userData.realName,
      phone: props.userData.phone,
      policeNo: props.userData.policeNo,
      status: (props.userData.status ?? 1) as 0 | 1,
      remark: props.userData.remark || ''
    }
  } else if (val && !props.userData) {
    formData.value = {
      username: '',
      realName: '',
      phone: '',
      policeNo: '',
      status: 1,
      remark: ''
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
        await updateUser(props.userData!.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createUser(formData.value)
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
