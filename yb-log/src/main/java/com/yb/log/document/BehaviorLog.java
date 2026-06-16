package com.yb.log.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为日志（MongoDB 文档）
 */
@Data
@Document(collection = "behavior_log")
public class BehaviorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档ID */
    @Id
    private String id;

    /** 用户 ID */
    @Indexed
    private Long userId;

    /** 行为类型：VIEW / CLICK / ADD_CART / PURCHASE / SEARCH */
    private String action;

    /** 行为目标（如商品ID、页面路径） */
    private String target;

    /** 目标描述 */
    private String targetDesc;

    /** 客户端 IP */
    private String ip;

    /** User-Agent */
    private String userAgent;

    /** 行为发生时间 */
    @Indexed
    private LocalDateTime timestamp;
}
