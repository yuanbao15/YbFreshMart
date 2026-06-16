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

    /** 用户ID */
    private Long id;
    /** 手机号 */
    private String phone;
    /** 昵称 */
    private String nickname;
    /** 头像URL */
    private String avatar;
    /** 邮箱 */
    private String email;
    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;
    /** 状态: 0-禁用 1-正常 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
}
