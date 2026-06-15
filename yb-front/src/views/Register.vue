<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="auth-title">注册</h2>
      <p class="auth-subtitle">加入 FreshMart，享受新鲜生活 🥬</p>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" class="form-input" type="text"
                 placeholder="请输入手机号" maxlength="11" autocomplete="tel"
                 @input="onPhoneInput" @compositionstart="phoneComposing = true"
                 @compositionend="phoneComposing = false; onPhoneInput($event)" />
          <span class="field-hint" v-if="phoneHint">{{ phoneHint }}</span>
        </div>
        <div class="form-group">
          <label>昵称 <span style="color:var(--text-light);font-weight:400">（选填）</span></label>
          <input v-model="form.nickname" class="form-input" type="text"
                 placeholder="给自己取个名字吧" maxlength="20" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" class="form-input" type="password"
                 placeholder="6-20位密码" autocomplete="new-password" />
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input v-model="form.confirmPassword" class="form-input" type="password"
                 placeholder="再次输入密码" autocomplete="new-password" />
        </div>
        <div class="form-error" v-if="error">{{ error }}</div>
        <button type="submit" class="btn btn-primary btn-lg" style="width:100%"
                :disabled="loading">
          <span v-if="loading" class="spinner" style="width:18px;height:18px;border-width:2px"></span>
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <p class="auth-switch">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ phone: '', nickname: '', password: '', confirmPassword: '' })
const loading = ref(false)
const error = ref('')
const phoneHint = ref('')
const phoneComposing = ref(false)

function onPhoneInput(e) {
  if (phoneComposing.value) return
  const raw = form.phone
  const filtered = raw.replace(/\D/g, '')
  if (raw !== filtered) {
    form.phone = filtered
    phoneHint.value = '手机号只能输入数字'
  } else {
    phoneHint.value = ''
  }
}

async function handleRegister() {
  error.value = ''
  if (!form.phone || !form.password) {
    error.value = '请填写手机号和密码'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    error.value = '请输入正确的手机号'
    return
  }
  if (form.password.length < 6 || form.password.length > 20) {
    error.value = '密码长度需为6-20位'
    return
  }
  if (form.password !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    await auth.register(form.phone, form.password, form.nickname || undefined)
    router.push('/')
  } catch (e) {
    error.value = e.message || '注册失败'
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
