package com.yb.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求（Feign 接口用）
 */
@Data
public class UserCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String phone;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String nickname;
}
