import api from './index.js'

// 登录
export function login(phone, password) {
  return api.post('/auth/login', { phone, password })
}

// 注册
export function register(phone, password, nickname) {
  return api.post('/auth/register', { phone, password, nickname })
}

// 刷新 Token
export function refreshToken() {
  return api.post('/auth/refresh')
}

// 登出
export function logout() {
  return api.post('/auth/logout')
}

// 获取当前用户信息
export function getCurrentUser() {
  return api.get('/auth/me')
}
