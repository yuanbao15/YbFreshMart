package com.yb.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息响应
 */
@Data
public class UserSimpleResp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nickname;
    private String avatar;
}
