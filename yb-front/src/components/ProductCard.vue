<template>
  <div class="product-card" @click="goDetail">
    <div class="product-image">
      <img :src="spu.mainImage || 'https://placehold.co/300x300/e8f5e9/07c160?text=' + encodeURIComponent(spu.name || '商品')" :alt="spu.name" />
    </div>
    <div class="product-info">
      <h3 class="product-name">{{ spu.name }}</h3>
      <p class="product-brand" v-if="spu.brand">{{ spu.brand }}</p>
      <p class="product-desc" v-if="spu.description">{{ spu.description }}</p>
      <div class="product-footer">
        <span class="product-unit">{{ spu.unit || '件' }}</span>
        <span class="product-status" v-if="spu.status === 0" style="color: var(--text-light)">已下架</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  spu: { type: Object, required: true }
})

const router = useRouter()

function goDetail() {
  router.push(`/product/${props.spu.id}`)
}
</script>

<style scoped>
.product-card {
  background: var(--white);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}
.product-image {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f5f5f5;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.product-info {
  padding: 12px;
}
.product-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-brand {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 4px;
}
.product-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-light);
}
</style>
