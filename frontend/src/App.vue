<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import router from '@/router'
import { useUserStore } from '@/store/modules/user'
import '@/styles/index.scss'

const userStore = useUserStore()

onMounted(async () => {
  if (userStore.token) {
    try {
      await userStore.getUserInfoAction()
      if (router.currentRoute.value.path === '/login') {
        router.push('/dashboard')
      }
    } catch (error) {
      console.error('[App] Failed to get user info:', error)
    }
  }
})
</script>

<style lang="scss" scoped>
</style>
