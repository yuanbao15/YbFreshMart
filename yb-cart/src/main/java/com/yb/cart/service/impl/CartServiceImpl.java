package com.yb.cart.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yb.cart.dto.CartItemDTO;
import com.yb.cart.service.CartService;
import com.yb.common.constant.RedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车服务实现 - 基于 Redis Hash
 * <p>
 * Redis 结构：
 * Key = cart:{userId}
 * Type = Hash
 *   Field = skuId (String)
 *   Value = CartItemDTO JSON (String)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private String cartKey(Long userId) {
        return RedisKey.CART_PREFIX + userId;
    }

    @Override
    public void addItem(Long userId, CartItemDTO item) {
        String key = cartKey(userId);
        String field = String.valueOf(item.getSkuId());

        // 检查是否已存在
        String existingJson = (String) redisTemplate.opsForHash().get(key, field);
        if (existingJson != null) {
            CartItemDTO existingItem = fromJson(existingJson);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                redisTemplate.opsForHash().put(key, field, toJson(existingItem));
                log.info("[Cart] 购物车商品数量叠加, userId={}, skuId={}, quantity={}",
                        userId, item.getSkuId(), existingItem.getQuantity());
                return;
            }
        }

        // 新商品
        item.setAddTime(LocalDateTime.now());
        redisTemplate.opsForHash().put(key, field, toJson(item));
        log.info("[Cart] 添加商品到购物车, userId={}, skuId={}", userId, item.getSkuId());
    }

    @Override
    public void updateQuantity(Long userId, Long skuId, Integer quantity) {
        String key = cartKey(userId);
        String field = String.valueOf(skuId);

        String existingJson = (String) redisTemplate.opsForHash().get(key, field);
        if (existingJson == null) {
            log.warn("[Cart] 购物车中无此商品, userId={}, skuId={}", userId, skuId);
            return;
        }

        CartItemDTO item = fromJson(existingJson);
        if (item != null) {
            item.setQuantity(quantity);
            redisTemplate.opsForHash().put(key, field, toJson(item));
            log.info("[Cart] 修改购物车商品数量, userId={}, skuId={}, quantity={}",
                    userId, skuId, quantity);
        }
    }

    @Override
    public void removeItem(Long userId, Long skuId) {
        String key = cartKey(userId);
        String field = String.valueOf(skuId);
        redisTemplate.opsForHash().delete(key, field);
        log.info("[Cart] 删除购物车商品, userId={}, skuId={}", userId, skuId);
    }

    @Override
    public List<CartItemDTO> getCart(Long userId) {
        String key = cartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        List<CartItemDTO> list = new ArrayList<>();
        for (Object value : entries.values()) {
            CartItemDTO item = fromJson((String) value);
            if (item != null) {
                list.add(item);
            }
        }
        // 按添加时间排序
        list.sort((a, b) -> {
            if (a.getAddTime() == null) return 1;
            if (b.getAddTime() == null) return -1;
            return a.getAddTime().compareTo(b.getAddTime());
        });
        return list;
    }

    @Override
    public Map<Long, Integer> getCartMap(Long userId) {
        List<CartItemDTO> items = getCart(userId);
        Map<Long, Integer> map = new HashMap<>();
        for (CartItemDTO item : items) {
            map.put(item.getSkuId(), item.getQuantity());
        }
        return map;
    }

    @Override
    public void clearCart(Long userId) {
        String key = cartKey(userId);
        redisTemplate.delete(key);
        log.info("[Cart] 清空购物车, userId={}", userId);
    }

    /** CartItemDTO → JSON 字符串 */
    private String toJson(CartItemDTO item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.error("[Cart] 序列化购物车项失败: {}", e.getMessage());
            return "{}";
        }
    }

    /** JSON 字符串 → CartItemDTO */
    private CartItemDTO fromJson(String json) {
        try {
            return objectMapper.readValue(json, CartItemDTO.class);
        } catch (JsonProcessingException e) {
            log.error("[Cart] 解析购物车项失败: {}", e.getMessage());
            return null;
        }
    }
}
