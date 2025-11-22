import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ApiResult } from '../types'
import { userApi } from './user'
import { useAuthStore } from '@/stores/auth'

const BASE_URL = import.meta.env.MODE === 'development' ? '/api' : 'http://localhost:8080'

// 标记是否正在刷新token
let isRefreshing = false
// 存储待重试的请求
let failedQueue: Array<{
  resolve: (value?: any) => void
  reject: (reason?: any) => void
}> = []

class ApiRequest {
  private instance: AxiosInstance

  constructor() {
    this.instance = axios.create({
      baseURL: BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken')
        if (token && config.headers) {
          config.headers['token'] = token
        } else {
          console.warn('请求缺少accessToken:', config.url)
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResult>) => {
        const { data } = response
        if (data.code === 200) {
          return data
        } else {
          // 检查是否是token相关的错误
          if (data.msg && (data.msg.includes('token') || data.msg.includes('登录') || data.msg.includes('无效') || data.msg.includes('过期'))) {
            console.log('检测到token相关错误:', data.msg)
            return this.handleTokenError(response.config)
          }
          return Promise.reject(new Error(data.msg || '请求失败'))
        }
      },
      async (error) => {
        if (error.response) {
          const { status, data } = error.response
          
          // 401 未授权或 token 相关错误
          if (status === 401 || (data?.msg && (data.msg.includes('token') || data.msg.includes('登录') || data.msg.includes('无效') || data.msg.includes('过期')))) {
            console.log('检测到401或token相关错误:', status, data?.msg)
            return this.handleTokenError(error.config)
          }
          return Promise.reject(new Error(data?.msg || '请求失败'))
        } else if (error.request) {
          console.error('请求失败，可能是跨域问题:', error.message)
          return Promise.reject(new Error('请求失败，请检查网络连接或跨域配置'))
        } else {
          return Promise.reject(error)
        }
      }
    )
  }

  private async handleTokenError(originalRequest?: InternalAxiosRequestConfig) {
    // 如果是刷新token的请求本身失败，直接跳转登录，避免循环
    if (originalRequest?.url?.includes('/user/refresh')) {
      console.log('刷新token请求失败，清除认证信息')
      this.clearAuthAndRedirect()
      return Promise.reject(new Error('刷新token失败，请重新登录'))
    }

    // 如果没有 refreshToken，直接跳转登录
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      console.log('没有refreshToken，清除认证信息')
      this.clearAuthAndRedirect()
      return Promise.reject(new Error('未登录或登录已过期'))
    }

    // 如果正在刷新token，将请求加入队列
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then(() => {
        // 重试原请求
        if (originalRequest) {
          const token = localStorage.getItem('accessToken')
          if (token && originalRequest.headers) {
            originalRequest.headers['token'] = token
          }
          return this.instance(originalRequest)
        }
      })
    }

    isRefreshing = true

    try {
      // 使用axios直接调用刷新接口，避免使用request实例导致循环
      const refreshResponse = await axios.post(
        `${BASE_URL}/user/refresh`,
        null,
        {
          params: { RefreshToken: refreshToken },
          headers: {
            'Content-Type': 'application/json',
          },
        }
      )

      const refreshData = refreshResponse.data
      if (refreshData && refreshData.code === 200 && refreshData.data) {
        const authStore = useAuthStore()
        authStore.login(refreshData.data)
        
        // 处理队列中的请求
        failedQueue.forEach(({ resolve }) => {
          resolve()
        })
        failedQueue = []
        isRefreshing = false

        // 重试原请求
        if (originalRequest) {
          const newToken = localStorage.getItem('accessToken')
          if (newToken && originalRequest.headers) {
            originalRequest.headers['token'] = newToken
          }
          return this.instance(originalRequest)
        }
      } else {
        throw new Error('刷新token失败')
      }
    } catch (error: any) {
      // 刷新失败，清空队列并跳转登录
      console.error('刷新token失败:', error)
      failedQueue.forEach(({ reject }) => {
        reject(error)
      })
      failedQueue = []
      isRefreshing = false
      // 只有在确实无法刷新token时才清除认证信息
      // 避免因为网络问题等临时错误导致用户被登出
      const errorMsg = error.response?.data?.msg || error.message || ''
      const isAuthError = error.response?.status === 401 || 
                         errorMsg.includes('token') || 
                         errorMsg.includes('登录') || 
                         errorMsg.includes('无效') || 
                         errorMsg.includes('过期')
      if (isAuthError) {
        console.log('token验证失败，清除认证信息')
      this.clearAuthAndRedirect()
      } else {
        console.log('刷新token失败，但可能是临时错误，不清除认证信息')
      }
      return Promise.reject(error)
    }
  }

  private clearAuthAndRedirect() {
    console.log('清除认证信息并跳转到登录页')
    const authStore = useAuthStore()
    authStore.logout()
    // 延迟跳转，避免在请求处理过程中立即跳转
    setTimeout(() => {
    window.location.href = '/login'
    }, 100)
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
    return this.instance.get(url, config)
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
    return this.instance.post(url, data, config)
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
    return this.instance.put(url, data, config)
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResult<T>> {
    return this.instance.delete(url, config)
  }
}

export default new ApiRequest()
