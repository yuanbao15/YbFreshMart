package com.yb.common.es.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Component;

/**
 * ES 索引管理工具
 * <p>
 * 提供索引的创建、删除、存在性检查等操作。
 * 使用示例：
 * <pre>{@code
 *   esIndexUtil.createIndex(ProductDocument.class);
 *   boolean exists = esIndexUtil.existsIndex(ProductDocument.class);
 * }</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsIndexUtil {

    private final ElasticsearchOperations operations;

    /**
     * 创建索引（如果不存在），自动应用实体的 @Mapping 和 @Setting 注解
     */
    public <T> boolean createIndex(Class<T> clazz) {
        IndexOperations indexOps = operations.indexOps(clazz);
        if (indexOps.exists()) {
            log.debug("索引已存在: {}", indexOps.getIndexCoordinates().getIndexName());
            return false;
        }
        indexOps.create();
        Document mapping = indexOps.createMapping(clazz);
        indexOps.putMapping(mapping);
        log.info("索引创建成功: {}", indexOps.getIndexCoordinates().getIndexName());
        return true;
    }

    /**
     * 删除索引
     */
    public <T> boolean deleteIndex(Class<T> clazz) {
        IndexOperations indexOps = operations.indexOps(clazz);
        if (indexOps.exists()) {
            indexOps.delete();
            log.info("索引已删除: {}", indexOps.getIndexCoordinates().getIndexName());
            return true;
        }
        return false;
    }

    /**
     * 检查索引是否存在
     */
    public <T> boolean existsIndex(Class<T> clazz) {
        return operations.indexOps(clazz).exists();
    }
}
