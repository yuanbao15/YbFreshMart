package com.yb.log.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志（MongoDB 文档）
 * <p>
 * 记录系统中的关键操作，用于安全审计和追溯。
 */
@Data
@Document(collection = "audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /** 操作用户 ID */
    @Indexed
    private Long userId;

    /** 操作类型：LOGIN / REGISTER / UPDATE_PROFILE / DELETE / GRANT 等 */
    private String operation;

    /** 操作详情 */
    private String detail;

    /** 操作结果：SUCCESS / FAILURE */
    private String result;

    /** 操作来源 IP */
    private String ip;

    /** 操作时间 */
    @Indexed
    private LocalDateTime timestamp;
}
