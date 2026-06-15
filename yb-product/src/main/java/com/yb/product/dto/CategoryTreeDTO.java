package com.yb.product.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类目树节点
 */
@Data
public class CategoryTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer sortOrder;
    private String icon;

    /** 子类目列表 */
    private List<CategoryTreeDTO> children;
}
