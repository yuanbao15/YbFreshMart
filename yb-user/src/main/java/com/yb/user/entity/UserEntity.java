package com.yb.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    /** 手机号 */
    private String phone;

    /** 密码（加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 性别 0-未知 1-男 2-女 */
    private Integer gender;

    /** 状态 0-禁用 1-正常 */
    private Integer status;
}
