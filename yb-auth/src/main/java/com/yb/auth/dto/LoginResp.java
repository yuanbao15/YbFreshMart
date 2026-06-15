package com.yb.auth.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录/注册响应
 */
@Data
public class LoginResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT Token */
    private String token;

    /** 用户 ID */
    private Long userId;

    /** 手机号 */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 角色 */
    private String role;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
