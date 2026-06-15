package com.yb.cart.controller;

import com.yb.cart.dto.CartItemDTO;
import com.yb.cart.service.CartService;
import com.yb.common.dto.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** 查询购物车列表（Feign 契约：CartClient.getCart） */
    @GetMapping("/{userId}")
    public R<Map<Long, Integer>> getCart(@PathVariable Long userId) {
        Map<Long, Integer> cartMap = cartService.getCartMap(userId);
        return R.ok(cartMap);
    }

    /** 查询购物车详情（含商品信息） */
    @GetMapping("/{userId}/detail")
    public R<List<CartItemDTO>> getCartDetail(@PathVariable Long userId) {
        List<CartItemDTO> items = cartService.getCart(userId);
        return R.ok(items);
    }

    /** 添加商品到购物车 */
    @PostMapping("/{userId}/items")
    public R<Void> addItem(@PathVariable Long userId, @Valid @RequestBody CartItemDTO item) {
        cartService.addItem(userId, item);
        return R.ok();
    }

    /** 修改购物车商品数量 */
    @PutMapping("/{userId}/items/{skuId}")
    public R<Void> updateQuantity(@PathVariable Long userId,
                                   @PathVariable Long skuId,
                                   @RequestParam Integer quantity) {
        cartService.updateQuantity(userId, skuId, quantity);
        return R.ok();
    }

    /** 删除购物车商品 */
    @DeleteMapping("/{userId}/items/{skuId}")
    public R<Void> removeItem(@PathVariable Long userId, @PathVariable Long skuId) {
        cartService.removeItem(userId, skuId);
        return R.ok();
    }

    /** 清空购物车（Feign 契约：CartClient.clearCart） */
    @DeleteMapping("/{userId}/clear")
    public R<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return R.ok();
    }
}
