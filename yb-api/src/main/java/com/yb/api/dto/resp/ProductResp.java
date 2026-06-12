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

    private Long id;
    private Long spuId;
    private String name;
    private String image;
    private BigDecimal price;
    private Integer stock;
}
