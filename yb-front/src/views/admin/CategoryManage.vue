<template>
  <div class="admin-page">
    <div class="page-header">
      <h1 class="page-title">📁 类目管理</h1>
      <button class="btn btn-primary" @click="showForm(null)">+ 新增类目</button>
    </div>

    <div class="loading" v-if="loading">
      <span class="spinner"></span>加载中...
    </div>

    <div class="card" v-else>
      <table class="admin-table" v-if="flatList.length">
        <thead>
          <tr>
            <th style="width:60px">ID</th>
            <th>名称</th>
            <th style="width:60px">层级</th>
            <th style="width:80px">排序</th>
            <th style="width:100px">图标</th>
            <th style="width:160px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cat in flatList" :key="cat.id"
              :style="{ paddingLeft: (cat.level - 1) * 24 + 'px' }">
            <td>{{ cat.id }}</td>
            <td>
              <span v-if="cat.level > 1" style="color:#ccc;margin-right:4px">
                {{ '└'.padStart(cat.level, '　') }}
              </span>
              {{ cat.name }}
            </td>
            <td>{{ cat.level }}</td>
            <td>{{ cat.sortOrder }}</td>
            <td>{{ cat.icon }}</td>
            <td>
              <button class="btn btn-outline btn-sm" @click="showForm(cat)">编辑</button>
              <button class="btn btn-sm" style="color:var(--danger);margin-left:6px"
                      @click="handleDelete(cat)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="empty-state" v-else><p>暂无类目</p></div>
    </div>

    <!-- 编辑弹窗 -->
    <div class="modal" v-if="formVisible" @click.self="formVisible = false">
      <div class="modal-card card">
        <h3>{{ editing ? '编辑类目' : '新增类目' }}</h3>
        <form @submit.prevent="handleSave">
          <div class="form-group">
            <label>名称 *</label>
            <input v-model="form.name" class="form-input" required maxlength="50" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>父级 ID <span style="font-weight:400;color:var(--text-light)">（0=顶级）</span></label>
              <input v-model.number="form.parentId" class="form-input" type="number" min="0" />
            </div>
            <div class="form-group">
              <label>层级</label>
              <input v-model.number="form.level" class="form-input" type="number" min="1" max="3" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>排序</label>
              <input v-model.number="form.sortOrder" class="form-input" type="number" min="0" />
            </div>
            <div class="form-group">
              <label>图标</label>
              <input v-model="form.icon" class="form-input" placeholder="🥬" maxlength="10" />
            </div>
          </div>
          <div class="form-error" v-if="error">{{ error }}</div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="formVisible = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="toast.show" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategoryTree } from '../../api/product.js'
import { createCategory, updateCategory, deleteCategory } from '../../api/admin.js'

const flatList = ref([])
const loading = ref(false)
const toast = ref({ show: false, type: 'success', msg: '' })

const formVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const error = ref('')
const form = ref({ parentId: 0, name: '', level: 1, sortOrder: 0, icon: '' })

function showToast(type, msg) {
  toast.value = { show: true, type, msg }
  setTimeout(() => { toast.value.show = false }, 2000)
}

// 把树形类目展开为平铺列表
function flattenTree(tree, list = []) {
  tree.forEach(cat => {
    list.push(cat)
    if (cat.children?.length) flattenTree(cat.children, list)
  })
  return list
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    flatList.value = flattenTree(res.data || [])
  } catch (e) {
    showToast('error', e.message)
  } finally {
    loading.value = false
  }
}

function showForm(cat) {
  error.value = ''
  if (cat) {
    editing.value = cat
    form.value = {
      parentId: cat.parentId || 0,
      name: cat.name,
      level: cat.level || 1,
      sortOrder: cat.sortOrder || 0,
      icon: cat.icon || ''
    }
  } else {
    editing.value = null
    form.value = { parentId: 0, name: '', level: 1, sortOrder: 0, icon: '' }
  }
  formVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { error.value = '请输入类目名称'; return }
  saving.value = true
  error.value = ''
  try {
    const data = { ...form.value, id: undefined }
    if (editing.value) {
      await updateCategory(editing.value.id, data)
      showToast('success', '类目已更新')
    } else {
      await createCategory(data)
      showToast('success', '类目已创建')
    }
    formVisible.value = false
    await loadData()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

async function handleDelete(cat) {
  if (!confirm(`确定删除类目「${cat.name}」吗？有子类目时可能失败。`)) return
  try {
    await deleteCategory(cat.id)
    showToast('success', '类目已删除')
    await loadData()
  } catch (e) {
    showToast('error', e.message)
  }
}

onMounted(loadData)
</script>

<style scoped>
.admin-table {
  width: 100%;
  border-collapse: collapse;
}
.admin-table th {
  text-align: left;
  padding: 10px 8px;
  border-bottom: 2px solid var(--border);
  font-size: 13px;
  color: var(--text-light);
  font-weight: 500;
}
.admin-table td {
  padding: 10px 8px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-card {
  width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal-card h3 {
  margin-bottom: 16px;
}
.form-row {
  display: flex;
  gap: 12px;
}
.form-row .form-group {
  flex: 1;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
</style>
