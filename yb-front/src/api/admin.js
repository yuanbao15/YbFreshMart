import api from './index.js'

// ==================== 类目管理 ====================
export function createCategory(data) {
  return api.post('/product/category', data)
}
export function updateCategory(id, data) {
  return api.put(`/product/category/${id}`, data)
}
export function deleteCategory(id) {
  return api.delete(`/product/category/${id}`)
}

// ==================== SPU 管理 ====================
export function createSpu(data) {
  return api.post('/product/spu', data)
}
export function updateSpu(id, data) {
  return api.put(`/product/spu/${id}`, data)
}

// ==================== SKU 管理 ====================
export function createSku(data) {
  return api.post('/product/sku', data)
}
export function updateSku(id, data) {
  return api.put(`/product/sku/${id}`, data)
}
export function updateSkuStock(id, stock) {
  return api.put(`/product/sku/${id}/stock`, null, { params: { stock } })
}
export function deleteSku(id) {
  return api.delete(`/product/sku/${id}`)
}
