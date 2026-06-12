package com.yb.api.client;

import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务 Feign 接口
 */
@FeignClient(
        name = "yb-inventory",
        path = "/api/inventory",
        fallbackFactory = com.yb.api.fallback.InventoryClientFallbackFactory.class
)
public interface InventoryClient {

    @GetMapping("/query/{skuId}")
    R<Integer> queryStock(@PathVariable("skuId") Long skuId);

    @PostMapping("/deduct")
    R<Boolean> deductStock(@RequestParam("skuId") Long skuId,
                           @RequestParam("quantity") Integer quantity);
}
