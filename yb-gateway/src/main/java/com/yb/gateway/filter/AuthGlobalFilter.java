package com.yb.gateway.filter;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 全局鉴权过滤器
 * <p>
 * 从请求头中获取 Authorization: Bearer {jwt}，解析后向下游传递 userId 等信息。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class AuthGlobalFilter implements GlobalFilter {

    @Value("${jwt.secret:FreshMart2024SecretKeyForJWTTokenGenerationMustBeLongEnough}")
    private String jwtSecret;

    /** 放行路径（不需要 Token） */
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/search",
            "/api/product/page",
            "/api/product/category",
            // "/api/user",     // 阶段二：正式接入 auth，不再放行
            "/doc.html",
            "/swagger-ui",
            "/webjars",
            "/v3/api-docs",
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("[Auth] 缺少 Token, path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // 3. 解析 JWT
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            // 4. 向后传递用户信息
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", role == null ? "user" : role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.warn("[Auth] JWT 解析失败, path={}, err={}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /** 判断是否在白名单 */
    private boolean isWhitePath(String path) {
        for (String white : WHITE_LIST) {
            if (path.startsWith(white)) {
                return true;
            }
        }
        return false;
    }
}
