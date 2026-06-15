package com.yb.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.yb.common.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户信息拦截器
 * <p>
 * 从请求头中提取 X-User-Id 和 X-User-Role（由 Gateway AuthGlobalFilter 传入），
 * 存入 UserContext ThreadLocal，业务代码可直接获取。
 * <p>
 * 请求结束后在 afterCompletion 中清理 ThreadLocal，防止内存泄漏。
 */
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if (StrUtil.isNotBlank(userIdStr)) {
            try {
                UserContext.setCurrentUserId(Long.valueOf(userIdStr));
            } catch (NumberFormatException e) {
                log.warn("[Security] X-User-Id 格式不正确: {}", userIdStr);
            }
        }
        if (StrUtil.isNotBlank(role)) {
            UserContext.setCurrentUserRole(role);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
