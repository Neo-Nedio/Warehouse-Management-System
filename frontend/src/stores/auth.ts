import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { User, LoginResponse } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)

  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
  }

  const isAuthenticated = computed(() => !!user.value)

  function login(response: LoginResponse) {
    user.value = response.user
    localStorage.setItem('user', JSON.stringify(response.user))
    localStorage.setItem('accessToken', response.tokens.accessToken)
    localStorage.setItem('refreshToken', response.tokens.refreshToken)
  }

  function logout() {
    user.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function updateUser(updatedUser: User) {
    user.value = updatedUser
    localStorage.setItem('user', JSON.stringify(updatedUser))
  }

  async function refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      throw new Error('没有refreshToken')
    }
    try {
      // 使用axios直接调用，避免经过request拦截器导致循环
      const axios = (await import('axios')).default
      const BASE_URL = import.meta.env.MODE === 'development' ? '/api' : 'http://localhost:8080'
      const response = await axios.post(
        `${BASE_URL}/user/refresh`,
        null,
        {
          params: { RefreshToken: refreshToken },
          headers: {
            'Content-Type': 'application/json',
          },
        }
      )
      const refreshData = response.data
      if (refreshData && refreshData.code === 200 && refreshData.data) {
        // 更新用户信息和token
        login(refreshData.data)
        return refreshData.data
      }
      throw new Error('刷新token失败')
    } catch (error: any) {
      // 不自动logout，让调用方决定如何处理
      // 只抛出错误，不清除登录状态
      console.error('刷新token失败:', error)
      throw error
    }
  }

  return {
    user,
    isAuthenticated,
    login,
    logout,
    updateUser,
    refreshToken,
  }
})
