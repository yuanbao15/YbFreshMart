import api from './index.js'

// 查询购物车详情
export function getCartDetail(userId) {
  return api.get(`/cart/${userId}/detail`)
}

// 添加商品到购物车
export function addToCart(userId, item) {
  return api.post(`/cart/${userId}/items`, item)
}

// 修改商品数量
export function updateCartItem(userId, skuId, quantity) {
  return api.put(`/cart/${userId}/items/${skuId}`, null, { params: { quantity } })
}

// 删除购物车商品
export function removeCartItem(userId, skuId) {
  return api.delete(`/cart/${userId}/items/${skuId}`)
}

// 清空购物车
export function clearCart(userId) {
  return api.delete(`/cart/${userId}/clear`)
}
