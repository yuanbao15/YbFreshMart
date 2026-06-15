package com.yb.common.security.context;

/**
 * 用户上下文 - ThreadLocal 封装
 * <p>
 * Gateway 鉴权后将 userId/role 放入请求头 X-User-Id / X-User-Role，
 * UserInfoInterceptor 拦截后存入 UserContext，业务代码可直接获取当前用户信息。
 *
 * <pre>
 * 使用示例：
 *   Long userId = UserContext.getCurrentUserId();
 *   String role = UserContext.getCurrentUserRole();
 * </pre>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    public static void setCurrentUserRole(String role) {
        USER_ROLE.set(role);
    }

    public static String getCurrentUserRole() {
        return USER_ROLE.get();
    }

    /** 请求结束后清理 ThreadLocal，防止内存泄漏 */
    public static void clear() {
        USER_ID.remove();
        USER_ROLE.remove();
    }
}
