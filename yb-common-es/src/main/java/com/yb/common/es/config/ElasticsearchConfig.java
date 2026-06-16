package com.yb.common.es.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * Elasticsearch 自动配置入口
 * <p>
 * Spring Boot 3.2 + Spring Data Elasticsearch 5.2 已自动配置：
 * <ul>
 *   <li>ElasticsearchClient（新 Java 客户端）</li>
 *   <li>ElasticsearchTemplate / ElasticsearchOperations</li>
 *   <li>ElasticsearchConverter（MappingElasticsearchConverter）</li>
 * </ul>
 * 本模块主要负责统一依赖版本，无需自定义 Bean。
 * <p>
 * {@code @AutoConfiguration} 注解确保 yb-common-es 的组件被 Spring 扫描。
 */
@AutoConfiguration
public class ElasticsearchConfig {
}
