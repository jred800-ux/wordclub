import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)

  // 初始化时从 localStorage 恢复 token 并获取用户信息
  async function init() {
    if (token.value) {
      try {
        const data = await api.get('/auth/me')
        user.value = data.data
      } catch {
        // token 失效，清除
        token.value = ''
        localStorage.removeItem('token')
      }
    }
  }

  async function login(username, password) {
    const data = await api.post('/auth/login', { username, password })
    token.value = data.data.token
    user.value = data.data.user
    localStorage.setItem('token', token.value)
    return data
  }

  async function register(username, email, password) {
    await api.post('/auth/register', { username, email, password })
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // 即使接口失败也清除本地状态
    }
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  async function fetchUser() {
    const data = await api.get('/auth/me')
    user.value = data.data
  }

  return {
    user, token, isLoggedIn,
    init, login, register, logout, fetchUser,
  }
})
