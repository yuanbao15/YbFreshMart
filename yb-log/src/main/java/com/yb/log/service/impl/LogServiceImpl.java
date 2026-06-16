package com.yb.log.service.impl;

import com.yb.common.dto.PageDTO;
import com.yb.log.document.AuditLog;
import com.yb.log.document.BehaviorLog;
import com.yb.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final MongoTemplate mongoTemplate;

    // ==================== 行为日志 ====================

    @Override
    public void saveBehaviorLog(BehaviorLog behaviorLog) {
        mongoTemplate.save(behaviorLog);
        log.debug("行为日志已保存: userId={}, action={}", behaviorLog.getUserId(), behaviorLog.getAction());
    }

    @Override
    public PageDTO<BehaviorLog> queryBehaviorLogs(Long userId, long page, long size) {
        Query query = new Query();
        if (userId != null) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }
        long total = mongoTemplate.count(query, BehaviorLog.class);

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .skip((page - 1) * size)
                .limit((int) size);
        List<BehaviorLog> list = mongoTemplate.find(query, BehaviorLog.class);

        return PageDTO.of(page, size, total, list);
    }

    // ==================== 审计日志 ====================

    @Override
    public void saveAuditLog(AuditLog auditLog) {
        mongoTemplate.save(auditLog);
        log.debug("审计日志已保存: userId={}, operation={}", auditLog.getUserId(), auditLog.getOperation());
    }

    @Override
    public PageDTO<AuditLog> queryAuditLogs(Long userId, long page, long size) {
        Query query = new Query();
        if (userId != null) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }
        long total = mongoTemplate.count(query, AuditLog.class);

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"))
                .skip((page - 1) * size)
                .limit((int) size);
        List<AuditLog> list = mongoTemplate.find(query, AuditLog.class);

        return PageDTO.of(page, size, total, list);
    }
}
