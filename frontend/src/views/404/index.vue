<template>
  <div class="not-found-container">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  
  .content {
    text-align: center;
    color: #fff;
    
    h1 {
      font-size: 120px;
      font-weight: 700;
      margin: 0;
      background: linear-gradient(135deg, #1890ff, #52c41a);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    
    p {
      font-size: 20px;
      margin: 20px 0 40px;
      color: #ccc;
    }
  }
}
</style>
