package com.yb.search.repository;

import com.yb.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 商品搜索 Repository
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    /**
     * 按名称模糊搜索（ik 分词）
     */
    Page<ProductDocument> findByName(String name, Pageable pageable);

    /**
     * 按类目 ID 查询
     */
    Page<ProductDocument> findByCategoryId(Long categoryId, Pageable pageable);
}
