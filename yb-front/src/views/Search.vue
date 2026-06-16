<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索商品..."
        @keyup.enter="handleSearch"
        class="search-input"
      />
      <button @click="handleSearch" class="search-btn">🔍 搜索</button>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <span
        :class="{ active: !selectedCategory }"
        @click="selectedCategory = null; handleSearch()"
      >全部</span>
      <span
        v-for="cat in categories"
        :key="cat.id"
        :class="{ active: selectedCategory === cat.id }"
        @click="selectedCategory = cat.id; handleSearch()"
      >{{ cat.name }}</span>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 搜索结果 -->
    <div v-else-if="list.length" class="search-results">
      <p class="result-info">共 {{ total }} 条结果</p>
      <div class="product-grid">
        <div v-for="item in list" :key="item.id" class="product-card" @click="goDetail(item)">
          <img :src="item.image || '/placeholder.png'" :alt="item.name" class="product-img" />
          <div class="product-info">
            <h3 class="product-name" v-html="item.name"></h3>
            <span class="product-price">¥{{ item.price }}</span>
            <span class="product-stock">库存: {{ item.stock || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="pages > 1">
        <button :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
        <span>{{ page }} / {{ pages }}</span>
        <button :disabled="page >= pages" @click="changePage(page + 1)">下一页</button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="searched" class="empty">
      <p>未找到与 "{{ lastKeyword }}" 相关的商品</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { searchProducts } from '../api/search.js'
import { getCategoryTree } from '../api/product.js'

const router = useRouter()

const keyword = ref('')
const lastKeyword = ref('')
const selectedCategory = ref(null)
const categories = ref([])
const list = ref([])
const total = ref(0)
const page = ref(1)
const pages = ref(1)
const size = 20
const loading = ref(false)
const searched = ref(false)

// 加载类目列表
async function loadCategories() {
  try {
    const res = await getCategoryTree()
    if (res.data?.code === 200) {
      flattenTree(res.data.data || [], categories.value)
    }
  } catch (e) {
    console.error('加载类目失败', e)
  }
}

function flattenTree(nodes, result) {
  for (const node of nodes) {
    result.push({ id: node.id, name: node.name })
    if (node.children?.length) flattenTree(node.children, result)
  }
}

async function handleSearch() {
  page.value = 1
  await doSearch()
}

async function changePage(p) {
  page.value = p
  await doSearch()
}

async function doSearch() {
  loading.value = true
  searched.value = true
  lastKeyword.value = keyword.value
  try {
    const params = { page: page.value, size }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (selectedCategory.value) params.categoryId = selectedCategory.value

    const res = await searchProducts(params)
    if (res.data?.code === 200) {
      const d = res.data.data
      list.value = d.records || []
      total.value = d.total || 0
      pages.value = d.pages || 1
    }
  } catch (e) {
    console.error('搜索失败', e)
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function goDetail(item) {
  if (item.spuId) {
    router.push(`/product/${item.spuId}`)
  }
}

loadCategories()
</script>

<style scoped>
.search-page { max-width: 1200px; margin: 20px auto; padding: 0 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.search-input { flex: 1; padding: 12px 16px; font-size: 16px; border: 2px solid #e0e0e0; border-radius: 8px; outline: none; }
.search-input:focus { border-color: #4CAF50; }
.search-btn { padding: 12px 24px; background: #4CAF50; color: #fff; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; }
.search-btn:hover { background: #388E3C; }

.category-filter { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 24px; }
.category-filter span { padding: 6px 16px; background: #f5f5f5; border-radius: 20px; cursor: pointer; font-size: 14px; }
.category-filter span.active { background: #4CAF50; color: #fff; }
.category-filter span:hover { background: #e8f5e9; }
.category-filter span.active:hover { background: #388E3C; }

.loading, .empty { text-align: center; padding: 60px 0; color: #999; }
.result-info { color: #666; margin-bottom: 16px; }

.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.product-card { border: 1px solid #eee; border-radius: 8px; overflow: hidden; cursor: pointer; transition: box-shadow .2s; }
.product-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,.1); }
.product-img { width: 100%; height: 200px; object-fit: cover; }
.product-info { padding: 12px; }
.product-name { font-size: 15px; margin-bottom: 8px; }
.product-name :deep(em) { color: #e53935; font-style: normal; font-weight: bold; }
.product-price { color: #e53935; font-size: 18px; font-weight: bold; }
.product-stock { color: #999; font-size: 12px; margin-left: 12px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 24px; }
.pagination button { padding: 8px 20px; border: 1px solid #ddd; background: #fff; border-radius: 6px; cursor: pointer; }
.pagination button:hover:not(:disabled) { border-color: #4CAF50; color: #4CAF50; }
.pagination button:disabled { opacity: .4; cursor: default; }
</style>
