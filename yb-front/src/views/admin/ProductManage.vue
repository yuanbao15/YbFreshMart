<template>
  <div class="admin-page">
    <div class="page-header">
      <h1 class="page-title">📦 商品管理</h1>
      <button class="btn btn-primary" @click="showSpuForm(null)">+ 新增 SPU</button>
    </div>

    <!-- 筛选 -->
    <div class="card" style="margin-bottom:16px">
      <div class="filter-row">
        <select v-model.number="filterCategoryId" class="form-input" style="max-width:200px"
                @change="loadSpuPage(1)">
          <option :value="null">全部分类</option>
          <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">
            {{ '　'.repeat(cat.level - 1) + cat.name }}
          </option>
        </select>
        <input v-model="keyword" class="form-input" style="max-width:200px"
               placeholder="搜索商品名称" @keyup.enter="loadSpuPage(1)" />
        <button class="btn btn-primary btn-sm" @click="loadSpuPage(1)">查询</button>
      </div>
    </div>

    <!-- SPU 列表 -->
    <div class="loading" v-if="loading"><span class="spinner"></span>加载中...</div>
    <template v-else>
      <div class="card" v-for="spu in spuList" :key="spu.id" style="margin-bottom:12px">
        <div class="spu-row">
          <div class="spu-info">
            <strong>{{ spu.name }}</strong>
            <span class="spu-meta">ID: {{ spu.id }} | {{ spu.brand || '无品牌' }} | {{ spu.unit }}</span>
            <span class="spu-status" :style="{color: spu.status === 1 ? 'var(--primary)' : 'var(--danger)'}">
              {{ spu.status === 1 ? '上架' : '下架' }}
            </span>
          </div>
          <div class="spu-actions">
            <button class="btn btn-outline btn-sm" @click="showSpuForm(spu)">编辑</button>
            <button class="btn btn-primary btn-sm" @click="showSkuForm(spu.id, null)">+ 添加规格</button>
          </div>
        </div>

        <!-- SKU 列表 -->
        <div class="sku-table-wrap" v-if="skuMap[spu.id]?.length">
          <table class="admin-table">
            <thead>
              <tr>
                <th>SKU ID</th>
                <th>规格</th>
                <th>售价</th>
                <th>成本价</th>
                <th>库存</th>
                <th>销量</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sku in skuMap[spu.id]" :key="sku.id">
                <td>{{ sku.id }}</td>
                <td>{{ sku.spec || sku.name || '-' }}</td>
                <td class="price">¥{{ Number(sku.price).toFixed(2) }}</td>
                <td>{{ sku.costPrice ? '¥' + Number(sku.costPrice).toFixed(2) : '-' }}</td>
                <td>{{ sku.stock }}</td>
                <td>{{ sku.soldCount || 0 }}</td>
                <td :style="{color: sku.status === 1 ? 'var(--primary)' : 'var(--danger)'}">
                  {{ sku.status === 1 ? '上架' : '下架' }}
                </td>
                <td>
                  <button class="btn btn-outline btn-sm" @click="showSkuForm(spu.id, sku)">编辑</button>
                  <button class="btn btn-sm" style="color:var(--danger);margin-left:4px"
                          @click="handleDeleteSku(spu.id, sku)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else style="padding:12px;color:var(--text-light);font-size:13px">
          暂无规格，请点击"添加规格"
        </div>
      </div>
      <div class="empty-state" v-if="spuList.length === 0"><p>暂无商品</p></div>
    </template>

    <div class="pagination" v-if="total > size">
      <button :disabled="page <= 1" @click="loadSpuPage(page - 1)">上一页</button>
      <span class="current">{{ page }} / {{ Math.ceil(total / size) }}</span>
      <button :disabled="page >= Math.ceil(total / size)" @click="loadSpuPage(page + 1)">下一页</button>
    </div>

    <!-- SPU 编辑弹窗 -->
    <div class="modal" v-if="spuFormVisible" @click.self="spuFormVisible = false">
      <div class="modal-card card">
        <h3>{{ editingSpu ? '编辑 SPU' : '新增 SPU' }}</h3>
        <form @submit.prevent="handleSaveSpu">
          <div class="form-group">
            <label>名称 *</label>
            <input v-model="spuForm.name" class="form-input" required maxlength="200" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>所属类目 ID *</label>
              <input v-model.number="spuForm.categoryId" class="form-input" type="number" required />
            </div>
            <div class="form-group">
              <label>品牌</label>
              <input v-model="spuForm.brand" class="form-input" maxlength="50" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>单位</label>
              <input v-model="spuForm.unit" class="form-input" placeholder="500g/份" maxlength="20" />
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model.number="spuForm.status" class="form-input">
                <option :value="1">上架</option>
                <option :value="0">下架</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="spuForm.description" class="form-input" rows="2" maxlength="500"></textarea>
          </div>
          <div class="form-group">
            <label>主图 URL</label>
            <input v-model="spuForm.mainImage" class="form-input" placeholder="https://..." maxlength="500" />
          </div>
          <div class="form-error" v-if="spuError">{{ spuError }}</div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="spuFormVisible = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="spuSaving">
              {{ spuSaving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- SKU 编辑弹窗 -->
    <div class="modal" v-if="skuFormVisible" @click.self="skuFormVisible = false">
      <div class="modal-card card">
        <h3>{{ editingSku ? '编辑规格' : '添加规格' }}</h3>
        <form @submit.prevent="handleSaveSku">
          <div class="form-row">
            <div class="form-group">
              <label>规格名 *</label>
              <input v-model="skuForm.spec" class="form-input" placeholder="500g / 1kg" required maxlength="200" />
            </div>
            <div class="form-group">
              <label>SKU 名称</label>
              <input v-model="skuForm.name" class="form-input" maxlength="200" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>售价 *</label>
              <input v-model.number="skuForm.price" class="form-input" type="number" step="0.01" min="0" required />
            </div>
            <div class="form-group">
              <label>成本价</label>
              <input v-model.number="skuForm.costPrice" class="form-input" type="number" step="0.01" min="0" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>库存 *</label>
              <input v-model.number="skuForm.stock" class="form-input" type="number" min="0" required />
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model.number="skuForm.status" class="form-input">
                <option :value="1">上架</option>
                <option :value="0">下架</option>
              </select>
            </div>
          </div>
          <div class="form-error" v-if="skuError">{{ skuError }}</div>
          <div class="modal-actions">
            <button type="button" class="btn btn-outline" @click="skuFormVisible = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="skuSaving">
              {{ skuSaving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="toast.show" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCategoryTree, getSpuPage, getSkuList } from '../../api/product.js'
import { createSpu, updateSpu, createSku, updateSku, deleteSku } from '../../api/admin.js'

const toast = ref({ show: false, type: 'success', msg: '' })
function showToast(type, msg) {
  toast.value = { show: true, type, msg }
  setTimeout(() => { toast.value.show = false }, 2000)
}

// 类目（供筛选下拉）
const flatCategories = ref([])
function flattenTree(tree, list = []) {
  tree.forEach(c => { list.push(c); if (c.children?.length) flattenTree(c.children, list) })
  return list
}

// SPU 列表
const spuList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filterCategoryId = ref(null)
const keyword = ref('')

// SKU 缓存: { spuId: [sku, ...] }
const skuMap = reactive({})

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    flatCategories.value = flattenTree(res.data || [])
  } catch (e) { /* ignore */ }
}

async function loadSpuPage(p) {
  page.value = p || page.value
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filterCategoryId.value) params.categoryId = filterCategoryId.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getSpuPage(params)
    spuList.value = res.data?.records || []
    total.value = res.data?.total || 0
    // 加载每个 SPU 的 SKU
    for (const spu of spuList.value) {
      loadSkus(spu.id)
    }
  } catch (e) {
    showToast('error', e.message)
  } finally {
    loading.value = false
  }
}

async function loadSkus(spuId) {
  try {
    const res = await getSkuList(spuId)
    skuMap[spuId] = res.data || []
  } catch (e) { skuMap[spuId] = [] }
}

// ==================== SPU 表单 ====================
const spuFormVisible = ref(false)
const editingSpu = ref(null)
const spuSaving = ref(false)
const spuError = ref('')
const spuForm = reactive({ categoryId: null, name: '', brand: '', description: '', mainImage: '', unit: '', status: 1 })

function showSpuForm(spu) {
  spuError.value = ''
  if (spu) {
    editingSpu.value = spu
    spuForm.categoryId = spu.categoryId
    spuForm.name = spu.name
    spuForm.brand = spu.brand || ''
    spuForm.description = spu.description || ''
    spuForm.mainImage = spu.mainImage || ''
    spuForm.unit = spu.unit || ''
    spuForm.status = spu.status
  } else {
    editingSpu.value = null
    Object.assign(spuForm, { categoryId: null, name: '', brand: '', description: '', mainImage: '', unit: '', status: 1 })
  }
  spuFormVisible.value = true
}

async function handleSaveSpu() {
  if (!spuForm.name || !spuForm.categoryId) { spuError.value = '请填写名称和类目'; return }
  spuSaving.value = true; spuError.value = ''
  try {
    const data = { ...spuForm }
    if (editingSpu.value) {
      await updateSpu(editingSpu.value.id, data)
      showToast('success', 'SPU 已更新')
    } else {
      await createSpu(data)
      showToast('success', 'SPU 已创建')
    }
    spuFormVisible.value = false
    await loadSpuPage(page.value)
  } catch (e) { spuError.value = e.message } finally { spuSaving.value = false }
}

// ==================== SKU 表单 ====================
const skuFormVisible = ref(false)
const editingSku = ref(null)
const currentSpuId = ref(null)
const skuSaving = ref(false)
const skuError = ref('')
const skuForm = reactive({ name: '', spec: '', price: null, costPrice: null, stock: 0, status: 1 })

function showSkuForm(spuId, sku) {
  skuError.value = ''
  currentSpuId.value = spuId
  if (sku) {
    editingSku.value = sku
    skuForm.name = sku.name || ''
    skuForm.spec = sku.spec || ''
    skuForm.price = sku.price
    skuForm.costPrice = sku.costPrice
    skuForm.stock = sku.stock
    skuForm.status = sku.status
  } else {
    editingSku.value = null
    Object.assign(skuForm, { name: '', spec: '', price: null, costPrice: null, stock: 0, status: 1 })
  }
  skuFormVisible.value = true
}

async function handleSaveSku() {
  if (!skuForm.price && skuForm.price !== 0) { skuError.value = '请填写售价'; return }
  skuSaving.value = true; skuError.value = ''
  try {
    const data = { ...skuForm, spuId: currentSpuId.value, id: undefined }
    if (editingSku.value) {
      await updateSku(editingSku.value.id, data)
      showToast('success', 'SKU 已更新')
    } else {
      await createSku(data)
      showToast('success', 'SKU 已创建')
    }
    skuFormVisible.value = false
    await loadSkus(currentSpuId.value)
  } catch (e) { skuError.value = e.message } finally { skuSaving.value = false }
}

async function handleDeleteSku(spuId, sku) {
  if (!confirm(`确定删除规格「${sku.spec || sku.name}」吗？`)) return
  try {
    await deleteSku(sku.id)
    showToast('success', 'SKU 已删除')
    await loadSkus(spuId)
  } catch (e) { showToast('error', e.message) }
}

onMounted(() => { loadCategories(); loadSpuPage(1) })
</script>

<style scoped>
.filter-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.spu-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  margin-bottom: 8px;
  border-bottom: 1px dashed var(--border);
}
.spu-info { display: flex; flex-direction: column; gap: 2px; }
.spu-meta { font-size: 12px; color: var(--text-light); }
.spu-status { font-size: 12px; }
.spu-actions { display: flex; gap: 8px; flex-shrink: 0; }
.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.admin-table th {
  text-align: left;
  padding: 6px 8px;
  border-bottom: 1px solid var(--border);
  font-weight: 500;
  color: var(--text-light);
  font-size: 12px;
}
.admin-table td {
  padding: 6px 8px;
  border-bottom: 1px solid var(--border);
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
  width: 520px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal-card h3 { margin-bottom: 16px; }
.form-row { display: flex; gap: 12px; }
.form-row .form-group { flex: 1; }
.form-error {
  color: var(--danger);
  font-size: 13px;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: #fff0f0;
  border-radius: var(--radius);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
</style>
