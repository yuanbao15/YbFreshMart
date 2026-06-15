package com.yb.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU 实体（库存量单位）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sku")
public class SkuEntity extends BaseEntity {

    /** SPU ID */
    private Long spuId;

    /** SKU 名称 */
    private String name;

    /** SKU 图片 */
    private String image;

    /** 规格（如"500g"、"2斤"） */
    private String spec;

    /** 售价 */
    private BigDecimal price;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer soldCount;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;
}
