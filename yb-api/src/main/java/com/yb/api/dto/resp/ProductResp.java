package com.yb.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品响应
 */
@Data
public class ProductResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品（SKU）ID */
    private Long id;
    /** SPU ID */
    private Long spuId;
    /** 商品名称 */
    private String name;
    /** 商品图片 */
    private String image;
    /** 商品价格 */
    private BigDecimal price;
    /** 库存数量 */
    private Integer stock;
    /** 类目 ID */
    private Long categoryId;
    /** 类目名称 */
    private String categoryName;
}
