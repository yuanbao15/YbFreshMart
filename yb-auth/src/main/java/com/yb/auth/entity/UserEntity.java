package com.yb.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户认证实体（yb_auth.t_user）
 * <p>
 * 只存储认证相关字段：手机号、密码、状态。
 * 用户档案信息（昵称、头像等）存储在 yb-user 服务。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    /** 手机号 */
    private String phone;

    /** 加密密码 */
    private String password;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLogin;
}
