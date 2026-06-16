package com.yb.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息响应
 */
@Data
public class UserSimpleResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
}
