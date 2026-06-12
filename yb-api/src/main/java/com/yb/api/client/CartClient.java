package com.yb.api.client;

import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 购物车服务 Feign 接口
 */
@FeignClient(
        name = "yb-cart",
        path = "/api/cart",
        fallbackFactory = com.yb.api.fallback.CartClientFallbackFactory.class
)
public interface CartClient {

    @GetMapping("/{userId}")
    R<Map<Long, Integer>> getCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/{userId}/clear")
    R<Void> clearCart(@PathVariable("userId") Long userId);
}
