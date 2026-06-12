package com.yb.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_address")
public class AddressEntity extends BaseEntity {

    /** 用户 ID */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detail;

    /** 是否默认地址 0-否 1-是 */
    private Integer isDefault;
}
