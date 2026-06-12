package com.yb.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页响应
 */
@Data
public class PageDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页码 */
    private Long page;

    /** 每页大小 */
    private Long size;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;

    public PageDTO() {}

    public PageDTO(Long page, Long size, Long total, Long pages, List<T> records) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }

    public static <T> PageDTO<T> of(Long page, Long size, Long total, List<T> records) {
        long pages = (total + size - 1) / size;
        return new PageDTO<>(page, size, total, pages, records);
    }
}
