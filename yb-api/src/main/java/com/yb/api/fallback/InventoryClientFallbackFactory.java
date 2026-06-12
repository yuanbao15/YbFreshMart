package com.yb.api.fallback;

import com.yb.api.client.InventoryClient;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        log.error("InventoryClient 调用失败，触发降级", cause);
        return new InventoryClient() {
            @Override
            public R<Integer> queryStock(Long skuId) {
                return R.fail(500, "库存服务暂时不可用");
            }

            @Override
            public R<Boolean> deductStock(Long skuId, Integer quantity) {
                return R.fail(500, "库存服务暂时不可用");
            }
        };
    }
}
