package com.yb.common.security.config;

import com.yb.common.security.interceptor.UserInfoInterceptor;
import com.yb.common.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全模块自动配置
 * <p>
 * 自动注册：
 * 1. JwtUtil —— JWT 生成/解析工具
 * 2. UserInfoInterceptor —— 从请求头提取用户信息到 ThreadLocal
 * <p>
 * FeignAuthInterceptor 在 {@link FeignAutoConfiguration} 中单独注册，
 * 避免没有 Feign 的服务加载时报 NoClassDefFoundError。
 */
@AutoConfiguration
@Import(FeignAutoConfiguration.class)
public class SecurityAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor())
                .addPathPatterns("/**")
                .order(10);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(
            @Value("${jwt.secret:FreshMart2024SecretKeyForJWTTokenGenerationMustBeLongEnough}") String secret,
            @Value("${jwt.expiration:86400000}") long expiration) {
        return new JwtUtil(secret, expiration);
    }
}
