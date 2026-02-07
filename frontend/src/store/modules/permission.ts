import { defineStore } from 'pinia'
import { ref } from 'vue'
import { RouteRecordRaw } from 'vue-router'

export const usePermissionStore = defineStore(
  'permission',
  () => {
    const routes = ref<RouteRecordRaw[]>([])
    const sidebarRouters = ref<RouteRecordRaw[]>([])
    
    function generateRoutes(_roles: string[] = ['ADMIN']) {
      return []
    }
    
    return {
      routes,
      sidebarRouters,
      generateRoutes
    }
  },
  {
    persist: false
  }
)
