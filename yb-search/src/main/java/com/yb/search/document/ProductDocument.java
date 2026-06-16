package com.yb.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品 ES 索引文档
 */
@Data
@Document(indexName = "product")
@Setting(shards = 1, replicas = 0)
public class ProductDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品（SKU）ID */
    @Id
    private Long id;

    /** SPU ID */
    @Field(type = FieldType.Long)
    private Long spuId;

    /**
     * 商品名称（ik_smart 分词，支持中文全文检索）
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    /** 商品图片 */
    @Field(type = FieldType.Keyword, index = false)
    private String image;

    /** 商品价格 */
    @Field(type = FieldType.Double)
    private BigDecimal price;

    /** 库存数量 */
    @Field(type = FieldType.Integer)
    private Integer stock;

    /**
     * 类目 ID（用于分类筛选）
     */
    @Field(type = FieldType.Long)
    private Long categoryId;

    /**
     * 类目名称
     */
    @Field(type = FieldType.Keyword)
    private String categoryName;
}
