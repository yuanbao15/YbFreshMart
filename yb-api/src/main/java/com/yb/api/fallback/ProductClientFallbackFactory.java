package com.yb.api.fallback;

import com.yb.api.client.ProductClient;
import com.yb.api.dto.resp.ProductResp;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error("ProductClient 调用失败，触发降级", cause);
        return new ProductClient() {
            @Override
            public R<ProductResp> getSkuById(Long skuId) {
                return R.fail(500, "商品服务暂时不可用");
            }

            @Override
            public R<PageDTO<ProductResp>> page(Long page, Long size) {
                return R.fail(500, "商品服务暂时不可用");
            }
        };
    }
}
