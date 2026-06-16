package com.yb.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息查询请求
 */
@Data
public class UserProfileReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;
    /** 手机号 */
    private String phone;
}
