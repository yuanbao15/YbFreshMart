package com.yb.common.security.config;

import com.yb.common.security.feign.FeignAuthInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Feign 鉴权拦截器自动配置
 * <p>
 * 单独拆分，使用 @ConditionalOnClass 确保只有 classpath 上有 Feign 时才加载。
 * 避免 yb-cart 等不需要 Feign 的服务加载时因找不到 feign.RequestInterceptor 而启动失败。
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class FeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FeignAuthInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}
