package com.yb.common.security.util;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 - 生成、解析、校验 Token
 * <p>
 * Payload 包含：userId、role、iat（签发时间）、exp（过期时间）
 */
@Slf4j
public class JwtUtil {

    private final String secret;
    private final long expiration;

    public JwtUtil(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /** 生成 JWT Token */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role == null ? "user" : role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getKey())
                .compact();
    }

    /** 解析 JWT Token，返回 Claims */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 Token 是否有效 */
    public boolean validateToken(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /** 从 Token 中获取 userId */
    public Long getUserId(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 从 Token 中获取 role */
    public String getUserRole(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 获取 Token 剩余有效时间（毫秒） */
    public long getRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    /** 刷新 Token：解析旧 Token → 生成新 Token */
    public String refreshToken(String oldToken) {
        Claims claims = parseToken(oldToken);
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        return generateToken(userId, role);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
