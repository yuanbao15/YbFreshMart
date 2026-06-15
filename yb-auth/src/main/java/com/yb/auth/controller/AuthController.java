package com.yb.auth.controller;

import com.yb.auth.dto.LoginReq;
import com.yb.auth.dto.LoginResp;
import com.yb.auth.dto.RegisterReq;
import com.yb.auth.service.AuthService;
import com.yb.common.dto.R;
import com.yb.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 手机号+密码登录 */
    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        LoginResp resp = authService.login(req);
        return R.ok(resp);
    }

    /** 手机号注册 */
    @PostMapping("/register")
    public R<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        LoginResp resp = authService.register(req);
        return R.ok(resp);
    }

    /** 刷新 Token */
    @PostMapping("/refresh")
    public R<LoginResp> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        LoginResp resp = authService.refreshToken(token);
        return R.ok(resp);
    }

    /** 登出 */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        authService.logout(token);
        return R.ok();
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public R<String> me() {
        Long userId = UserContext.getCurrentUserId();
        String role = UserContext.getCurrentUserRole();
        return R.ok("userId=" + userId + ", role=" + role);
    }
}
