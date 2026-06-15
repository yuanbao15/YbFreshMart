<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="auth-title">登录</h2>
      <p class="auth-subtitle">欢迎回到 FreshMart 🥬</p>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" class="form-input" type="text"
                 placeholder="请输入手机号" maxlength="11" autocomplete="tel"
                 @input="onPhoneInput" @compositionstart="phoneComposing = true"
                 @compositionend="phoneComposing = false; onPhoneInput($event)" />
          <span class="field-hint" v-if="phoneHint">{{ phoneHint }}</span>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" class="form-input" type="password"
                 placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <div class="form-error" v-if="error">{{ error }}</div>
        <button type="submit" class="btn btn-primary btn-lg" style="width:100%"
                :disabled="loading">
          <span v-if="loading" class="spinner" style="width:18px;height:18px;border-width:2px"></span>
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <p class="auth-switch">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ phone: '', password: '' })
const loading = ref(false)
const error = ref('')
const phoneHint = ref('')
const phoneComposing = ref(false)

function onPhoneInput(e) {
  if (phoneComposing.value) return
  const raw = form.phone
  // 过滤非数字字符
  const filtered = raw.replace(/\D/g, '')
  if (raw !== filtered) {
    form.phone = filtered
    phoneHint.value = '手机号只能输入数字'
  } else {
    phoneHint.value = ''
  }
}

async function handleLogin() {
  error.value = ''
  if (!form.phone || !form.password) {
    error.value = '请填写手机号和密码'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    error.value = '请输入正确的手机号'
    return
  }
  loading.value = true
  try {
    await auth.login(form.phone, form.password)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 140px);
}
.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
}
.auth-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}
.auth-subtitle {
  color: var(--text-light);
  margin-bottom: 28px;
  font-size: 14px;
}
.field-hint {
  display: block;
  color: var(--warning);
  font-size: 12px;
  margin-top: 4px;
}
.form-error {
  color: var(--danger);
  font-size: 13px;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #fff0f0;
  border-radius: var(--radius);
}
.auth-switch {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: var(--text-light);
}
.auth-switch a {
  color: var(--primary);
  text-decoration: none;
}
</style>
