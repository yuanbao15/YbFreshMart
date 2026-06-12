package com.yb.api.fallback;

import com.yb.api.client.CartClient;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CartClientFallbackFactory implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {
        log.error("CartClient 调用失败，触发降级", cause);
        return new CartClient() {
            @Override
            public R<Map<Long, Integer>> getCart(Long userId) {
                return R.fail(500, "购物车服务暂时不可用");
            }

            @Override
            public R<Void> clearCart(Long userId) {
                return R.fail(500, "购物车服务暂时不可用");
            }
        };
    }
}
