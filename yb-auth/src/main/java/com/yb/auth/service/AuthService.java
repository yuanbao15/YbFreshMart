package com.yb.auth.service;

import com.yb.auth.dto.LoginReq;
import com.yb.auth.dto.LoginResp;
import com.yb.auth.dto.RegisterReq;

/**
 * 认证服务接口
 */
public interface AuthService {

    /** 手机号+密码登录 */
    LoginResp login(LoginReq req);

    /** 手机号注册 */
    LoginResp register(RegisterReq req);

    /** 刷新 Token */
    LoginResp refreshToken(String oldToken);

    /** 登出（Token 加入黑名单） */
    void logout(String token);
}
