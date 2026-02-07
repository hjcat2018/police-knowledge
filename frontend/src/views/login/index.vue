<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <svg viewBox="0 0 32 32" width="48" height="48">
            <defs>
              <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#1890ff;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#096dd9;stop-opacity:1" />
              </linearGradient>
            </defs>
            <rect width="32" height="32" rx="4" fill="url(#grad)"/>
            <path d="M8 10h16v2H8zm0 5h12v2H8zm0 5h14v2H8z" fill="#fff"/>
            <circle cx="24" cy="22" r="4" fill="#52c41a"/>
          </svg>
        </div>
        <h1>公安专网知识库系统</h1>
        <p>Police Internal Knowledge Base System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            size="large"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2024 公安专网知识库系统</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { LoginParams } from '@/types/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginParams>({
  username: '',
  password: '',
  deviceType: 'PC'
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在2-50个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      await userStore.loginAction(loginForm)
      ElMessage.success('登录成功')
      
      const redirect = route.query.redirect as string
      router.push(redirect || '/dashboard')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo {
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 12px;
    color: #666;
    margin: 0;
  }
}

.login-form {
  .login-button {
    width: 100%;
    font-size: 16px;
    letter-spacing: 2px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  
  p {
    font-size: 12px;
    color: #999;
    margin: 0;
  }
}
</style>
