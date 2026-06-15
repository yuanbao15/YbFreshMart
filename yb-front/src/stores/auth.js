import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi, refreshToken, getCurrentUser } from '../api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const phone = ref(localStorage.getItem('phone') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isLoggedIn = computed(() => !!token.value)

  function saveAuth(data) {
    token.value = data.token
    userId.value = data.userId
    phone.value = data.phone || ''
    nickname.value = data.nickname || ''
    role.value = data.role || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    if (data.phone) localStorage.setItem('phone', data.phone)
    if (data.nickname) localStorage.setItem('nickname', data.nickname)
    if (data.role) localStorage.setItem('role', data.role)
  }

  function clearAuth() {
    token.value = ''
    userId.value = ''
    phone.value = ''
    nickname.value = ''
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('phone')
    localStorage.removeItem('nickname')
    localStorage.removeItem('role')
  }

  async function login(phoneVal, password) {
    const res = await loginApi(phoneVal, password)
    saveAuth(res.data)
    return res.data
  }

  async function register(phoneVal, password, nick) {
    const res = await registerApi(phoneVal, password, nick)
    saveAuth(res.data)
    return res.data
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      clearAuth()
    }
  }

  async function refresh() {
    const res = await refreshToken()
    saveAuth(res.data)
    return res.data
  }

  async function fetchCurrentUser() {
    const res = await getCurrentUser()
    return res.data
  }

  return {
    token, userId, phone, nickname, role,
    isLoggedIn,
    saveAuth, clearAuth,
    login, register, logout, refresh, fetchCurrentUser
  }
})
