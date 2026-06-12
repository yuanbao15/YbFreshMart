package com.yb.api.client;

import com.yb.api.dto.resp.ProductResp;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品服务 Feign 接口
 */
@FeignClient(
        name = "yb-product",
        path = "/api/product",
        fallbackFactory = com.yb.api.fallback.ProductClientFallbackFactory.class
)
public interface ProductClient {

    @GetMapping("/{skuId}")
    R<ProductResp> getSkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("/page")
    R<PageDTO<ProductResp>> page(@RequestParam("page") Long page,
                                  @RequestParam("size") Long size);
}
