package com.yb.api.client;

import com.yb.api.dto.resp.ProductResp;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 搜索服务 Feign 接口
 */
@FeignClient(
        name = "yb-search",
        path = "/api/search",
        fallbackFactory = com.yb.api.fallback.SearchClientFallbackFactory.class
)
public interface SearchClient {

    @GetMapping("/product")
    R<PageDTO<ProductResp>> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "20") long size);
}
