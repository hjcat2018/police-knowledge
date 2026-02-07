import axios, { AxiosResponse, InternalAxiosRequestConfig, AxiosError, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import router from '@/router'

const TOKEN_ERROR_CODE = 40102

interface ResponseData {
  code?: number
  message?: string
  data?: any
}

const isTokenError = (code: number, message: string): boolean => {
  return code === TOKEN_ERROR_CODE ||
         code === 40101 ||
         code === 401 ||
         message?.includes('token') ||
         message?.includes('未授权') ||
         message?.includes('登录') ||
         message?.includes('无效')
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: Error) => {
    return Promise.reject(error)
  }
)

const shouldRetry = (error: AxiosError): boolean => {
  if (!error.response) {
    return true
  }
  const status = error.response.status
  return status >= 500 || status === 408 || status === 429
}

const retryDelay = (retryCount: number): number => {
  return Math.pow(2, retryCount) * 1000
}

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message } = response.data as ResponseData

    if (code === 200 || response.status === 200) {
      return (response.data as any).data
    }

    if (isTokenError(code || 0, message || '')) {
      ElMessage.error('登录已过期，请重新登录')
      useUserStore().resetStore()
      router.push('/login')
      return Promise.reject(new Error('未授权'))
    }

    if (code === 403) {
      ElMessage.error('没有权限访问')
      return Promise.reject(new Error('没有权限'))
    }

    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  async (error: AxiosError) => {
    const config = error.config as AxiosRequestConfig & { _retryCount?: number }
    const { response } = error

    if (response) {
      const { status, data } = response as AxiosResponse<ResponseData>

      if (status === 401 || (data?.code && isTokenError(data.code || 0, data.message || ''))) {
        ElMessage.error('登录已过期，请重新登录')
        useUserStore().resetStore()
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 408) {
        ElMessage.error('请求超时，正在重试...')
      } else if (status >= 500) {
        if (data?.code && isTokenError(data.code || 0, data.message || '')) {
          ElMessage.error('登录已过期，请重新登录')
          useUserStore().resetStore()
          router.push('/login')
        } else {
          ElMessage.error(data?.message || '服务器错误')
        }
      } else {
        ElMessage.error(data?.message || '请求失败')
      }

      if (shouldRetry(error) && config && (config._retryCount || 0) < 3) {
        config._retryCount = (config._retryCount || 0) + 1
        const delay = retryDelay(config._retryCount)
        console.log(`[Request] 请求失败，${delay}ms后重试 (${config._retryCount}/3)`)
        await new Promise(resolve => setTimeout(resolve, delay))
        return service(config)
      }
    } else if (!error.response) {
      if (config && (config._retryCount || 0) < 3) {
        config._retryCount = (config._retryCount || 0) + 1
        const delay = retryDelay(config._retryCount)
        console.log(`[Request] 网络异常，${delay}ms后重试 (${config._retryCount}/3)`)
        await new Promise(resolve => setTimeout(resolve, delay))
        return service(config)
      }
      ElMessage.error('网络连接异常，请检查网络')
    }

    return Promise.reject(error)
  }
)

export const createCancelableRequest = <T = any>(config: AxiosRequestConfig): { request: Promise<T>, cancel: () => void } => {
  const controller = new AbortController()
  const cancel = () => controller.abort()
  const requestConfig = {
    ...config,
    signal: controller.signal
  }
  const request = service.request<T>(requestConfig).then((response: AxiosResponse<T>) => response.data)
  return { request, cancel }
}

export const requestWithCache = async <T = any>(
  key: string,
  config: AxiosRequestConfig,
  cacheTime: number = 5 * 60 * 1000
): Promise<T> => {
  const cacheKey = `cache:${key}`
  const cached = localStorage.getItem(cacheKey)

  if (cached) {
    try {
      const cachedData = JSON.parse(cached) as { data: T, timestamp: number }
      if (Date.now() - cachedData.timestamp < cacheTime) {
        console.log(`[Request] 缓存命中: ${key}`)
        return cachedData.data
      }
    } catch (e) {
      console.warn('[Request] 缓存解析失败')
    }
  }

  console.log(`[Request] 缓存未命中，请求: ${key}`)
  const response = await service.request<T>(config)
  const responseData = (response as AxiosResponse<T>).data

  try {
    localStorage.setItem(cacheKey, JSON.stringify({
      data: responseData,
      timestamp: Date.now()
    }))
  } catch (e) {
    console.warn('[Request] 缓存写入失败，可能超出存储限制')
  }

  return responseData
}

export const clearCache = (key?: string) => {
  if (key) {
    localStorage.removeItem(`cache:${key}`)
    console.log(`[Request] 清除缓存: ${key}`)
  } else {
    Object.keys(localStorage)
      .filter(k => k.startsWith('cache:'))
      .forEach(k => localStorage.removeItem(k))
    console.log('[Request] 清除所有缓存')
  }
}

export default service
