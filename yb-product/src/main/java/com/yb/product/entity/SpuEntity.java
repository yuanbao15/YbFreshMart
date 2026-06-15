package com.yb.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yb.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品 SPU 实体（标准化产品单元）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_spu")
public class SpuEntity extends BaseEntity {

    /** 类目 ID */
    private Long categoryId;

    /** SPU 名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 品牌 */
    private String brand;

    /** 主图 */
    private String mainImage;

    /** 图片列表（JSON） */
    private String images;

    /** 单位（斤/袋/箱） */
    private String unit;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;
}
