package com.yb.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户详细信息响应
 */
@Data
public class UserProfileResp implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String email;
    private Integer gender;
    private Integer status;
    private LocalDateTime createTime;
}
