package com.yb.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品类目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class CategoryEntity extends BaseEntity {

    /** 父类目 ID，0 表示顶级 */
    private Long parentId;

    /** 类目名称 */
    private String name;

    /** 层级: 1,2,3 */
    private Integer level;

    /** 排序 */
    private Integer sortOrder;

    /** 图标 URL */
    private String icon;
}
