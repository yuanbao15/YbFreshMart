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

    /** 类目ID */
    private Long id;
    /** 父类目ID */
    private Long parentId;
    /** 类目名称 */
    private String name;
    /** 层级 */
    private Integer level;
    /** 排序 */
    private Integer sortOrder;
    /** 图标URL */
    private String icon;

    /** 子类目列表 */
    private List<CategoryTreeDTO> children;
}
