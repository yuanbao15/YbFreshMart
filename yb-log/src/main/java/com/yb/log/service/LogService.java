package com.yb.log.service;

import com.yb.common.dto.PageDTO;
import com.yb.log.document.AuditLog;
import com.yb.log.document.BehaviorLog;

/**
 * 日志服务接口
 */
public interface LogService {

    // ---- 行为日志 ----

    void saveBehaviorLog(BehaviorLog behaviorLog);

    PageDTO<BehaviorLog> queryBehaviorLogs(Long userId, long page, long size);

    // ---- 审计日志 ----

    void saveAuditLog(AuditLog auditLog);

    PageDTO<AuditLog> queryAuditLogs(Long userId, long page, long size);
}
