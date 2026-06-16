package com.yb.api.fallback;

import com.yb.api.client.SearchClient;
import com.yb.api.dto.resp.ProductResp;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchClientFallbackFactory implements FallbackFactory<SearchClient> {

    @Override
    public SearchClient create(Throwable cause) {
        log.error("SearchClient 调用失败，触发降级", cause);
        return new SearchClient() {
            @Override
            public R<PageDTO<ProductResp>> search(String keyword, Long categoryId, long page, long size) {
                return R.fail(500, "搜索服务暂时不可用");
            }
        };
    }
}
