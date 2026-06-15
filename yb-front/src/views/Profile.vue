<template>
  <div class="profile-page">
    <h1 class="page-title">👤 个人中心</h1>

    <div class="card profile-card">
      <div class="profile-header">
        <div class="avatar">{{ (auth.nickname || auth.phone || '?')[0] }}</div>
        <div class="profile-summary">
          <h2>{{ auth.nickname || '未设置昵称' }}</h2>
          <p>{{ auth.phone }}</p>
          <span class="role-badge">{{ auth.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
        </div>
      </div>

      <div class="profile-details">
        <div class="detail-row">
          <span class="detail-label">用户 ID</span>
          <span class="detail-value">{{ auth.userId }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">角色</span>
          <span class="detail-value">{{ auth.role }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Token</span>
          <span class="detail-value token-value">
            {{ auth.token ? auth.token.substring(0, 30) + '...' : '-' }}
          </span>
        </div>
      </div>
    </div>

    <div class="card" style="margin-top:16px">
      <h3 style="margin-bottom:16px">快捷操作</h3>
      <div class="action-links">
        <router-link to="/cart" class="action-link">
          <span>🛒</span> 我的购物车
        </router-link>
        <router-link to="/" class="action-link">
          <span>🥬</span> 浏览商品
        </router-link>
      </div>
    </div>

    <div class="card" style="margin-top:16px">
      <h3 style="margin-bottom:16px">Phase 2 接口测试</h3>
      <div class="test-area">
        <div class="test-item">
          <span class="test-label">当前用户信息 (GET /api/auth/me)</span>
          <button class="btn btn-outline btn-sm" @click="testAuthMe" :disabled="testLoading">
            {{ testLoading === 'me' ? '...' : '测试' }}
          </button>
          <span class="test-result" v-if="testResults.me" :class="testResults.me.ok ? 'ok' : 'fail'">
            {{ testResults.me.data }}
          </span>
        </div>
        <div class="test-item">
          <span class="test-label">刷新 Token (POST /api/auth/refresh)</span>
          <button class="btn btn-outline btn-sm" @click="testRefresh" :disabled="testLoading">
            {{ testLoading === 'refresh' ? '...' : '测试' }}
          </button>
          <span class="test-result" v-if="testResults.refresh" :class="testResults.refresh.ok ? 'ok' : 'fail'">
            {{ testResults.refresh.data }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '../stores/auth.js'
import { getCurrentUser, refreshToken } from '../api/auth.js'

const auth = useAuthStore()

const testLoading = ref(false)
const testResults = reactive({ me: null, refresh: null })

async function testAuthMe() {
  testLoading.value = 'me'
  try {
    const res = await getCurrentUser()
    testResults.me = { ok: true, data: JSON.stringify(res.data) }
  } catch (e) {
    testResults.me = { ok: false, data: e.message }
  } finally {
    testLoading.value = false
  }
}

async function testRefresh() {
  testLoading.value = 'refresh'
  try {
    const data = await auth.refresh()
    testResults.refresh = { ok: true, data: '新 Token: ' + data.token?.substring(0, 30) + '...' }
  } catch (e) {
    testResults.refresh = { ok: false, data: e.message }
  } finally {
    testLoading.value = false
  }
}
</script>

<style scoped>
.profile-card {
  padding: 24px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border);
}
.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--primary);
  color: white;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.profile-summary h2 {
  font-size: 20px;
  margin-bottom: 4px;
}
.profile-summary p {
  font-size: 14px;
  color: var(--text-light);
}
.role-badge {
  display: inline-block;
  padding: 2px 10px;
  background: #e8f5e9;
  color: var(--primary);
  border-radius: 10px;
  font-size: 12px;
  margin-top: 4px;
}
.profile-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-row {
  display: flex;
  align-items: center;
}
.detail-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-light);
}
.detail-value {
  font-size: 14px;
  font-weight: 500;
}
.token-value {
  font-family: monospace;
  font-size: 12px;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.action-links {
  display: flex;
  gap: 12px;
}
.action-link {
  padding: 10px 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  text-decoration: none;
  color: var(--text);
  font-size: 15px;
  transition: all 0.2s;
}
.action-link:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.test-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.test-item {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.test-label {
  font-size: 14px;
  color: var(--text-light);
  min-width: 260px;
}
.test-result {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 4px;
}
.test-result.ok {
  color: var(--primary);
  background: #e8f5e9;
  word-break: break-all;
}
.test-result.fail {
  color: var(--danger);
  background: #fff0f0;
}
</style>
