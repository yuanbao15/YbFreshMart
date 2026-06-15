<template>
  <div class="detail-page">
    <div class="loading" v-if="loading">
      <span class="spinner"></span>加载中...
    </div>
    <template v-else-if="spu">
      <!-- SPU 基本信息 -->
      <div class="card detail-header">
        <div class="detail-image">
          <img :src="spu.mainImage || 'https://placehold.co/400x400/e8f5e9/07c160?text=' + encodeURIComponent(spu.name || '')" :alt="spu.name" />
        </div>
        <div class="detail-info">
          <h1 class="detail-name">{{ spu.name }}</h1>
          <p class="detail-brand" v-if="spu.brand">品牌：{{ spu.brand }}</p>
          <p class="detail-desc" v-if="spu.description">{{ spu.description }}</p>
          <p class="detail-unit">规格：{{ spu.unit || '件' }}</p>
          <p class="detail-status" v-if="spu.status !== 1" style="color:var(--danger)">该商品已下架</p>
        </div>
      </div>

      <!-- SKU 列表 -->
      <div class="card" style="margin-top:16px">
        <h3 class="sku-title">规格选择</h3>
        <div class="loading" v-if="skuLoading">
          <span class="spinner"></span>
        </div>
        <div class="empty-state" v-else-if="skuList.length === 0">
          <p>暂无规格</p>
        </div>
        <div class="sku-list" v-else>
          <div v-for="sku in skuList" :key="sku.id" class="sku-item"
               :class="{ 'sku-disabled': sku.status !== 1 || sku.stock <= 0 }">
            <div class="sku-main">
              <div class="sku-info">
                <span class="sku-spec" v-if="sku.spec">{{ sku.spec }}</span>
                <span class="sku-name" v-else>{{ sku.name || '默认规格' }}</span>
                <span class="sku-stock" :class="{ 'no-stock': sku.stock <= 0 }">
                  库存：{{ sku.stock || 0 }}
                </span>
              </div>
              <div class="sku-price-action">
                <span class="price price-large">¥{{ Number(sku.price).toFixed(2) }}</span>
                <div class="sku-actions">
                  <button class="btn btn-primary btn-sm" @click="addToCart(sku)"
                          :disabled="sku.status !== 1 || sku.stock <= 0">
                    🛒 加入购物车
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <div class="empty-state" v-else>
      <p>商品不存在</p>
      <router-link to="/" class="btn btn-outline" style="margin-top:12px">返回首页</router-link>
    </div>

    <!-- Toast -->
    <div v-if="toast.show" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSpuDetail, getSkuList } from '../api/product.js'
import { addToCart as addToCartApi } from '../api/cart.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const spu = ref(null)
const skuList = ref([])
const loading = ref(true)
const skuLoading = ref(false)

const toast = ref({ show: false, type: 'success', msg: '' })

function showToast(type, msg) {
  toast.value = { show: true, type, msg }
  setTimeout(() => { toast.value.show = false }, 2000)
}

onMounted(async () => {
  try {
    const res = await getSpuDetail(route.params.spuId)
    spu.value = res.data
    // 加载 SKU 列表
    skuLoading.value = true
    const skuRes = await getSkuList(route.params.spuId)
    skuList.value = skuRes.data || []
  } catch (e) {
    console.error('加载商品详情失败:', e)
    showToast('error', e.message || '加载失败')
  } finally {
    loading.value = false
    skuLoading.value = false
  }
})

async function addToCart(sku) {
  if (!auth.isLoggedIn) {
    showToast('info', '请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await addToCartApi(auth.userId, {
      skuId: sku.id,
      spuId: spu.value.id,
      name: spu.value.name + (sku.spec ? ' - ' + sku.spec : ''),
      image: sku.image || spu.value.mainImage || '',
      price: sku.price,
      quantity: 1
    })
    showToast('success', '已加入购物车')
  } catch (e) {
    showToast('error', e.message || '添加失败')
  }
}
</script>

<style scoped>
.detail-header {
  display: flex;
  gap: 24px;
}
.detail-image {
  width: 400px;
  height: 400px;
  flex-shrink: 0;
  border-radius: var(--radius);
  overflow: hidden;
  background: #f5f5f5;
}
.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-info {
  flex: 1;
}
.detail-name {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 12px;
}
.detail-brand {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 6px;
}
.detail-desc {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 12px;
  line-height: 1.6;
}
.detail-unit {
  font-size: 14px;
  color: var(--text-light);
  margin-bottom: 6px;
}
.detail-status {
  margin-top: 12px;
  font-size: 14px;
}

.sku-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
.sku-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
}
.sku-item:last-child {
  border-bottom: none;
}
.sku-disabled {
  opacity: 0.5;
}
.sku-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sku-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sku-spec {
  font-size: 15px;
  font-weight: 500;
}
.sku-name {
  font-size: 15px;
}
.sku-stock {
  font-size: 13px;
  color: var(--text-light);
}
.sku-stock.no-stock {
  color: var(--danger);
}
.sku-price-action {
  display: flex;
  align-items: center;
  gap: 16px;
}
.sku-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
  }
  .detail-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }
  .sku-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .sku-price-action {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
