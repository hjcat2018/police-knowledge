import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { login, logout, getUserInfo, changePassword } from '@/api/auth'
import { UserInfo, LoginParams, ChangePasswordParams } from '@/types/api'
import { usePermissionStore } from './permission'

export const useUserStore = defineStore(
  'user',
  () => {
    const router = useRouter()
    const permissionStore = usePermissionStore()
    
    const token = ref<string>(localStorage.getItem('token') || '')
    const userInfo = ref<UserInfo | null>(null)
    const roles = ref<string[]>([])
    const permissions = ref<Set<string>>(new Set())
    
    const isLoggedIn = computed(() => !!token.value)
    const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
    
    async function loginAction(loginData: LoginParams) {
      try {
        const response = await login(loginData)
        token.value = response.token
        localStorage.setItem('token', response.token)
        
        await getUserInfoAction()
        permissionStore.generateRoutes()
        
        router.push('/dashboard')
        return response
      } catch (error) {
        throw error
      }
    }
    
    async function getUserInfoAction() {
      try {
        const response = await getUserInfo()
        userInfo.value = response
        roles.value = response.roles || []
        permissions.value = new Set(response.permissions || [])
        return response
      } catch (error) {
        throw error
      }
    }
    
    async function logoutAction() {
      try {
        await logout()
      } finally {
        resetStore()
        router.push('/login')
      }
    }
    
    async function changePasswordAction(data: ChangePasswordParams) {
      return await changePassword(data)
    }
    
    function resetStore() {
      token.value = ''
      userInfo.value = null
      roles.value = []
      permissions.value = new Set()
      localStorage.removeItem('token')
    }
    
    function hasRole(role: string) {
      return roles.value.includes(role)
    }
    
    function hasPermission(permission: string) {
      return permissions.value.has(permission)
    }
    
    return {
      token,
      userInfo,
      roles,
      permissions,
      isLoggedIn,
      userName,
      loginAction,
      getUserInfoAction,
      logoutAction,
      changePasswordAction,
      resetStore,
      hasRole,
      hasPermission
    }
  }
)
