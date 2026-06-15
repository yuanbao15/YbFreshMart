import api from './index.js'

// 获取类目树
export function getCategoryTree() {
  return api.get('/product/category/tree')
}

// 分页查询 SPU
export function getSpuPage(params) {
  return api.get('/product/spu/page', { params })
}

// SPU 详情
export function getSpuDetail(id) {
  return api.get(`/product/spu/${id}`)
}

// SKU 详情（Feign 契约）
export function getSkuDetail(skuId) {
  return api.get(`/product/${skuId}`)
}

// SPU 下的 SKU 列表
export function getSkuList(spuId) {
  return api.get('/product/sku/list', { params: { spuId } })
}

// SKU 分页
export function getSkuPage(params) {
  return api.get('/product/page', { params })
}
