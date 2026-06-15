package com.yb.common.security.feign;

import cn.hutool.core.util.StrUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器
 * <p>
 * 发起 Feign 调用时，自动从当前请求上下文中获取 Authorization 请求头，
 * 携带到下游微服务的请求头中，实现 Token 链式传递。
 * <p>
 * 调用链：
 * 客户端 → Gateway(AuthGlobalFilter) → 服务A(FeignAuthInterceptor) → 服务B
 */
@Slf4j
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authHeader)) {
            template.header("Authorization", authHeader);
        }

        // 同时传递 X-User-Id、X-User-Role（用于非 Token 场景或内部调用）
        String userId = request.getHeader("X-User-Id");
        if (StrUtil.isNotBlank(userId)) {
            template.header("X-User-Id", userId);
        }
        String userRole = request.getHeader("X-User-Role");
        if (StrUtil.isNotBlank(userRole)) {
            template.header("X-User-Role", userRole);
        }
    }
}
