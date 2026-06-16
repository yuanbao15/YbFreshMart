import api from './index.js'

// 搜索商品
export function searchProducts(params) {
  return api.get('/search/product', { params })
}
