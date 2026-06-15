package com.yb.cart.service;

import com.yb.cart.dto.CartItemDTO;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务接口
 */
public interface CartService {

    /** 添加商品（已存在则叠加数量） */
    void addItem(Long userId, CartItemDTO item);

    /** 修改商品数量 */
    void updateQuantity(Long userId, Long skuId, Integer quantity);

    /** 删除购物车中的商品 */
    void removeItem(Long userId, Long skuId);

    /** 查询购物车列表 */
    List<CartItemDTO> getCart(Long userId);

    /** 查询购物车（Feign 契约：Map<skuId, quantity>） */
    Map<Long, Integer> getCartMap(Long userId);

    /** 清空购物车 */
    void clearCart(Long userId);
}
