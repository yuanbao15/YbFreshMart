<template>
  <div class="home-page">
    <div class="home-layout">
      <!-- 左侧类目树 -->
      <aside class="category-sidebar card">
        <h3 class="sidebar-title">商品分类</h3>
        <div class="loading" v-if="catLoading">
          <span class="spinner"></span>加载中...
        </div>
        <ul class="category-tree" v-else>
          <li v-for="cat in categories" :key="cat.id" class="category-item"
              :class="{ active: activeCategoryId === cat.id }"
              @click="handleCatClick(cat)">
            <span class="category-icon">{{ cat.icon || '📁' }}</span>
            <span class="category-name">{{ cat.name }}</span>
            <span class="category-arrow" v-if="cat.children?.length">›</span>
            <!-- 二级 -->
            <ul v-if="cat.children?.length && expandedId === cat.id" class="sub-tree">
              <li v-for="sub in cat.children" :key="sub.id" class="sub-item"
                  :class="{ active: activeCategoryId === sub.id }"
                  @click.stop="handleCatClick(sub, cat.id)">
                <span class="category-icon">{{ sub.icon || '📂' }}</span>
                {{ sub.name }}
                <!-- 三级 -->
                <ul v-if="sub.children?.length" class="sub-tree sub-tree-3">
                  <li v-for="sub3 in sub.children" :key="sub3.id" class="sub-item"
                      :class="{ active: activeCategoryId === sub3.id }"
                      @click.stop="handleCatClick(sub3, cat.id)">
                    <span class="category-icon">{{ sub3.icon || '📄' }}</span>
                    {{ sub3.name }}
                  </li>
                </ul>
              </li>
            </ul>
          </li>
        </ul>
      </aside>

      <!-- 右侧商品列表 -->
      <section class="product-area">
        <div class="product-toolbar card">
          <input v-model="keyword" class="form-input" style="max-width:300px"
                 placeholder="搜索商品..." @keyup.enter="searchProducts" />
          <button class="btn btn-primary btn-sm" @click="searchProducts">搜索</button>
          <span class="result-count" v-if="total > 0">共 {{ total }} 个商品</span>
        </div>

        <div class="loading" v-if="loading">
          <span class="spinner"></span>加载中...
        </div>
        <div class="empty-state" v-else-if="spuList.length === 0">
          <p>暂无商品</p>
        </div>
        <div class="product-grid" v-else>
          <ProductCard v-for="spu in spuList" :key="spu.id" :spu="spu" />
        </div>

        <div class="pagination" v-if="total > size">
          <button :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
          <span class="current">{{ page }} / {{ Math.ceil(total / size) }}</span>
          <button :disabled="page >= Math.ceil(total / size)" @click="goPage(page + 1)">下一页</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategoryTree, getSpuPage } from '../api/product.js'
import ProductCard from '../components/ProductCard.vue'

const categories = ref([])
const catLoading = ref(false)
const expandedId = ref(null)      // 控制一级类目的展开/折叠
const activeCategoryId = ref(null) // 当前筛选的类目

const spuList = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(12)
const total = ref(0)

onMounted(() => {
  loadCategories()
  loadProducts()
})

async function loadCategories() {
  catLoading.value = true
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } catch (e) {
    console.error('加载类目失败:', e)
  } finally {
    catLoading.value = false
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getSpuPage(params)
    spuList.value = res.data?.records || []
    total.value = res.data?.total || 0
    page.value = res.data?.current || res.data?.page || 1
  } catch (e) {
    console.error('加载商品失败:', e)
  } finally {
    loading.value = false
  }
}

function handleCatClick(cat, parentId) {
  if (parentId) {
    // 二级或三级：设置筛选，保持父级展开
    activeCategoryId.value = activeCategoryId.value === cat.id ? null : cat.id
    expandedId.value = parentId  // 确保父级一级类目保持展开
  } else {
    // 一级：切换展开，同时设置筛选
    if (expandedId.value === cat.id) {
      expandedId.value = null
    } else {
      expandedId.value = cat.id
    }
    activeCategoryId.value = activeCategoryId.value === cat.id ? null : cat.id
  }
  page.value = 1
  loadProducts()
}

function searchProducts() {
  page.value = 1
  loadProducts()
}

function goPage(p) {
  page.value = p
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.home-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}
.category-sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 72px;
}
.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}
.category-tree {
  list-style: none;
}
.category-item {
  padding: 8px 6px;
  cursor: pointer;
  border-radius: 6px;
  font-size: 14px;
  transition: background 0.15s;
  user-select: none;
}
.category-item:hover {
  background: #f0fff4;
}
.category-item.active {
  background: #e8f5e9;
  color: var(--primary);
  font-weight: 500;
}
.category-icon {
  margin-right: 6px;
}
.category-arrow {
  float: right;
  font-size: 16px;
  color: var(--text-light);
  transition: transform 0.2s;
}
.category-item.active .category-arrow {
  transform: rotate(90deg);
}
.sub-tree {
  list-style: none;
  margin-top: 4px;
  padding-left: 28px;
}
.sub-tree-3 {
  padding-left: 20px;
}
.sub-item {
  padding: 6px 4px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px;
}
.sub-item:hover {
  background: #f0fff4;
}
.sub-item.active {
  color: var(--primary);
  font-weight: 500;
}

.product-area {
  flex: 1;
  min-width: 0;
}
.product-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.result-count {
  font-size: 13px;
  color: var(--text-light);
  margin-left: auto;
}
</style>
