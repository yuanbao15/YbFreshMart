package com.yb.cart.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车单项
 */
@Data
public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SKU ID */
    private Long skuId;

    /** SPU ID */
    private Long spuId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String image;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 加入购物车时间 */
    private LocalDateTime addTime;
}
