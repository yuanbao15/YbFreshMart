package com.yb.search.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.yb.common.dto.PageDTO;
import com.yb.search.document.ProductDocument;
import com.yb.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations operations;

    @Override
    public PageDTO<ProductDocument> search(String keyword, Long categoryId, long page, long size) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 关键词搜索（ik 分词匹配 name 字段）
        if (keyword != null && !keyword.isBlank()) {
            boolBuilder.must(Query.of(q -> q
                    .match(m -> m
                            .field("name")
                            .query(keyword))));
        }

        // 类目筛选（term 精确过滤）
        if (categoryId != null) {
            boolBuilder.filter(Query.of(q -> q
                    .term(t -> t
                            .field("categoryId")
                            .value(FieldValue.of(categoryId)))));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(PageRequest.of((int) page - 1, (int) size))
                .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        List<ProductDocument> list = hits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        log.debug("搜索 keyword={}, categoryId={} → 命中 {} 条", keyword, categoryId, hits.getTotalHits());

        return PageDTO.of(page, size, hits.getTotalHits(), list);
    }

    @Override
    public void syncProduct(ProductDocument product) {
        operations.save(product);
        log.info("商品已同步到 ES: id={}, name={}", product.getId(), product.getName());
    }

    @Override
    public void deleteProduct(Long productId) {
        operations.delete(String.valueOf(productId), ProductDocument.class);
        log.info("商品已从 ES 删除: id={}", productId);
    }
}
