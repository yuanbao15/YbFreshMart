<template>
  <div class="log-page">
    <h2>📋 日志查询</h2>

    <!-- 日志类型切换 -->
    <div class="tab-bar">
      <button :class="{ active: tab === 'behavior' }" @click="switchTab('behavior')">行为日志</button>
      <button :class="{ active: tab === 'audit' }" @click="switchTab('audit')">审计日志</button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <input v-model="userId" type="number" placeholder="用户ID（可选）" class="filter-input" />
      <button @click="query" class="filter-btn">查询</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 日志列表 -->
    <div v-else-if="list.length" class="log-list">
      <table>
        <thead>
          <tr>
            <th>时间</th>
            <th>用户ID</th>
            <th v-if="tab === 'behavior'">行为类型</th>
            <th v-if="tab === 'behavior'">目标</th>
            <th v-if="tab === 'audit'">操作</th>
            <th v-if="tab === 'audit'">结果</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in list" :key="log.id">
            <td>{{ formatTime(log.timestamp) }}</td>
            <td>{{ log.userId }}</td>
            <td v-if="tab === 'behavior'">{{ log.action }}</td>
            <td v-if="tab === 'behavior'">{{ log.targetDesc || log.target }}</td>
            <td v-if="tab === 'audit'">{{ log.operation }}</td>
            <td v-if="tab === 'audit'">
              <span :class="log.result === 'SUCCESS' ? 'success' : 'failure'">{{ log.result }}</span>
            </td>
            <td class="detail-cell">{{ tab === 'behavior' ? (log.userAgent || '-') : (log.detail || '-') }}</td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination" v-if="pages > 1">
        <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span>{{ page }} / {{ pages }}（共 {{ total }} 条）</span>
        <button :disabled="page >= pages" @click="changePage(page + 1)">下一页</button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="queried" class="empty">暂无日志记录</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getBehaviorLogs, getAuditLogs } from '../../api/log.js'

const tab = ref('behavior')
const userId = ref('')
const list = ref([])
const page = ref(1)
const pages = ref(1)
const total = ref(0)
const loading = ref(false)
const queried = ref(false)

function switchTab(t) {
  tab.value = t
  page.value = 1
  query()
}

async function changePage(p) {
  page.value = p
  await query()
}

async function query() {
  loading.value = true
  queried.value = true
  try {
    const params = { page: page.value, size: 20 }
    if (userId.value) params.userId = userId.value

    const fn = tab.value === 'behavior' ? getBehaviorLogs : getAuditLogs
    const res = await fn(params)
    if (res.code === 200) {
      const d = res.data
      list.value = d.records || []
      total.value = d.total || 0
      pages.value = d.pages || 1
    }
  } catch (e) {
    console.error('查询日志失败', e)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function formatTime(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  return d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.log-page { max-width: 1200px; margin: 20px auto; padding: 0 16px; }
h2 { margin-bottom: 16px; }

.tab-bar { display: flex; gap: 8px; margin-bottom: 16px; }
.tab-bar button { padding: 8px 20px; border: 1px solid #ddd; background: #fff; border-radius: 6px; cursor: pointer; font-size: 14px; }
.tab-bar button.active { background: #1976D2; color: #fff; border-color: #1976D2; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.filter-input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; width: 200px; }
.filter-btn { padding: 8px 20px; background: #1976D2; color: #fff; border: none; border-radius: 6px; cursor: pointer; }

.loading, .empty { text-align: center; padding: 40px 0; color: #999; }

table { width: 100%; border-collapse: collapse; font-size: 14px; }
th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #fafafa; font-weight: 600; }
tr:hover { background: #f9f9f9; }
.detail-cell { max-width: 300px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.success { color: #4CAF50; font-weight: bold; }
.failure { color: #e53935; font-weight: bold; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 20px; }
.pagination button { padding: 6px 16px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:hover:not(:disabled) { border-color: #1976D2; color: #1976D2; }
.pagination button:disabled { opacity: .4; cursor: default; }
</style>
