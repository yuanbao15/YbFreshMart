package com.yb.search.service;

import com.yb.common.dto.PageDTO;
import com.yb.search.document.ProductDocument;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 全文检索商品
     *
     * @param keyword    搜索关键词
     * @param categoryId 类目筛选（null 表示不筛选）
     * @param page       页码
     * @param size       每页条数
     * @return 分页结果（含高亮片段）
     */
    PageDTO<ProductDocument> search(String keyword, Long categoryId, long page, long size);

    /**
     * 同步商品到 ES
     */
    void syncProduct(ProductDocument product);

    /**
     * 从 ES 删除商品
     */
    void deleteProduct(Long productId);
}
