package com.yb.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建/更新请求
 */
@Data
public class UserSaveReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码 */
    @Size(min = 6, max = 20, message = "密码长度 6-20 位")
    private String password;

    /** 昵称 */
    @Size(max = 20, message = "昵称最长 20 个字符")
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;
}
