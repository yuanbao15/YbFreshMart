<template>
  <div class="cart-page">
    <h1 class="page-title">🛒 购物车</h1>

    <div class="loading" v-if="loading">
      <span class="spinner"></span>加载中...
    </div>
    <div class="empty-state" v-else-if="cartItems.length === 0">
      <div class="icon">🛒</div>
      <p>购物车是空的</p>
      <router-link to="/" class="btn btn-primary" style="margin-top:16px">去逛逛</router-link>
    </div>
    <template v-else>
      <div class="card">
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.skuId" class="cart-item">
            <div class="cart-item-image">
              <img :src="item.image || 'https://placehold.co/80x80/f5f5f5/999?text=N/A'" :alt="item.name" />
            </div>
            <div class="cart-item-info">
              <h4 class="cart-item-name">{{ item.name }}</h4>
              <span class="price price-medium">¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <span class="cart-add-time" v-if="item.addTime">
                添加于 {{ formatTime(item.addTime) }}
              </span>
            </div>
            <div class="cart-item-quantity">
              <button class="qty-btn" @click="changeQty(item, -1)"
                      :disabled="item.quantity <= 1">−</button>
              <span class="qty-value">{{ item.quantity }}</span>
              <button class="qty-btn" @click="changeQty(item, 1)">+</button>
            </div>
            <div class="cart-item-subtotal price price-medium">
              ¥{{ (Number(item.price || 0) * item.quantity).toFixed(2) }}
            </div>
            <button class="btn btn-sm" style="color:var(--danger)"
                    @click="removeItem(item)">删除</button>
          </div>
        </div>
      </div>

      <!-- 底部结算 -->
      <div class="card cart-footer">
        <div class="footer-left">
          <button class="btn btn-sm" style="color:var(--danger)"
                  @click="handleClear">清空购物车</button>
        </div>
        <div class="footer-right">
          <span class="total-text">
            共 <strong>{{ totalCount }}</strong> 件，合计
          </span>
          <span class="price price-large">¥{{ totalPrice.toFixed(2) }}</span>
          <button class="btn btn-primary" disabled title="订单功能将在阶段四实现">
            去结算
          </button>
        </div>
      </div>
    </template>

    <!-- Toast -->
    <div v-if="toast.show" class="toast" :class="'toast-' + toast.type">{{ toast.msg }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getCartDetail, updateCartItem, removeCartItem, clearCart } from '../api/cart.js'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()

const cartItems = ref([])
const loading = ref(true)
const toast = ref({ show: false, type: 'success', msg: '' })

function showToast(type, msg) {
  toast.value = { show: true, type, msg }
  setTimeout(() => { toast.value.show = false }, 2000)
}

const totalCount = computed(() => cartItems.value.reduce((s, i) => s + (i.quantity || 0), 0))
const totalPrice = computed(() => cartItems.value.reduce((s, i) => s + (i.price || 0) * (i.quantity || 0), 0))

onMounted(() => loadCart())

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartDetail(auth.userId)
    cartItems.value = res.data || []
  } catch (e) {
    showToast('error', e.message || '加载购物车失败')
  } finally {
    loading.value = false
  }
}

async function changeQty(item, delta) {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  try {
    await updateCartItem(auth.userId, item.skuId, newQty)
    item.quantity = newQty
  } catch (e) {
    showToast('error', e.message || '修改数量失败')
  }
}

async function removeItem(item) {
  try {
    await removeCartItem(auth.userId, item.skuId)
    cartItems.value = cartItems.value.filter(i => i.skuId !== item.skuId)
    showToast('success', '已删除')
  } catch (e) {
    showToast('error', e.message || '删除失败')
  }
}

async function handleClear() {
  if (!confirm('确定要清空购物车吗？')) return
  try {
    await clearCart(auth.userId)
    cartItems.value = []
    showToast('success', '购物车已清空')
  } catch (e) {
    showToast('error', e.message || '清空失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}
</script>

<style scoped>
.cart-list {
  display: flex;
  flex-direction: column;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}
.cart-item:last-child {
  border-bottom: none;
}
.cart-item-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius);
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}
.cart-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cart-item-info {
  flex: 1;
  min-width: 0;
}
.cart-item-name {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cart-add-time {
  display: block;
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}
.cart-item-quantity {
  display: flex;
  align-items: center;
  gap: 0;
}
.qty-btn {
  width: 30px;
  height: 30px;
  border: 1px solid var(--border);
  background: white;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}
.qty-btn:first-child { border-radius: 4px 0 0 4px; }
.qty-btn:last-child { border-radius: 0 4px 4px 0; }
.qty-btn:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
.qty-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.qty-value {
  width: 40px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  font-weight: 500;
}
.cart-item-subtotal {
  min-width: 80px;
  text-align: right;
}
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}
.footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.total-text {
  font-size: 15px;
  color: var(--text-light);
}
.total-text strong {
  color: var(--text);
}

@media (max-width: 768px) {
  .cart-item {
    flex-wrap: wrap;
  }
  .cart-item-image {
    width: 60px;
    height: 60px;
  }
  .cart-item-subtotal {
    order: 10;
    width: 100%;
    text-align: left;
    margin-left: 76px;
  }
  .cart-footer {
    flex-direction: column;
    gap: 12px;
  }
  .footer-right {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
