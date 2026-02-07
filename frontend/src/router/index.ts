import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: { title: '', hidden: false, breadcrumb: [] },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer', hidden: false }
      },
      {
        path: 'kb/list',
        name: 'KbList',
        component: () => import('@/views/kb/list/index.vue'),
        meta: { title: '知识库列表', icon: 'Document', hidden: false, roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'kb/docs',
        name: 'DocList',
        component: () => import('@/views/doc/list/index.vue'),
        meta: { title: '文档管理', icon: 'Files', hidden: false, roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/search/index.vue'),
        meta: { title: '智能搜索', icon: 'Search', hidden: false }
      },
      {
        path: 'vector/stats',
        name: 'VectorStats',
        component: () => import('@/views/vector/stats.vue'),
        meta: { title: '向量统计', icon: 'DataAnalysis', hidden: false }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue' /* webpackPreload: true */),
        meta: { title: '智能问答', icon: 'ChatDotRound', hidden: false }
      },
      {
        path: 'chat/professional',
        name: 'ChatProfessional',
        component: () => import('@/views/chat/professional/index.vue'),
        meta: { title: '专业问答', icon: 'Reading', hidden: false }
      },
      {
        path: 'chat/normal',
        name: 'ChatNormal',
        component: () => import('@/views/chat/normal/index.vue'),
        meta: { title: '普通问答', icon: 'ChatLineRound', hidden: false }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', hidden: false, roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'system/dict',
        name: 'Dict',
        component: () => import('@/views/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Setting', hidden: false, roles: ['ADMIN'] }
      },
      {
        path: 'system/mcp',
        name: 'McpService',
        component: () => import('@/views/mcp-service/index.vue'),
        meta: { title: 'MCP服务管理', icon: 'Connection', hidden: false, roles: ['ADMIN', 'MANAGER'] }
      },
      {
        path: 'system/prompt-template',
        name: 'PromptTemplate',
        component: () => import('@/views/prompt-template/index.vue'),
        meta: { title: '提示词模板', icon: 'DocumentCopy', hidden: false, roles: ['ADMIN', 'MANAGER'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404/index.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title as string || ''} - 公安专网知识库系统`
  
  const userStore = useUserStore()
  
  if (to.path !== '/login') {
    if (!userStore.token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }
  
  next()
})

export function resetRouter() {
  const newRouter = createRouter({
    history: createWebHistory(),
    routes: constantRoutes,
    scrollBehavior: () => ({ top: 0 })
  });
  (router as any).matcher = (newRouter as any).matcher
}

export { constantRoutes }
export default router
