import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器 —— 自动带 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器 —— 统一处理错误
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 业务错误
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.hash = '#/login'
        return Promise.reject(new Error('登录已过期，请重新登录'))
      }
      if (status === 403) {
        return Promise.reject(new Error('没有权限'))
      }
      return Promise.reject(new Error(error.response.data?.message || `服务器错误(${status})`))
    }
    return Promise.reject(new Error('网络异常，请检查网络连接'))
  }
)

export default api
