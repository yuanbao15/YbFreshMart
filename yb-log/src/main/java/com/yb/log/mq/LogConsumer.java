package com.yb.log.mq;

import com.yb.common.constant.MqQueue;
import com.yb.log.document.AuditLog;
import com.yb.log.document.BehaviorLog;
import com.yb.log.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 日志消息消费者
 * <p>
 * 从 MQ 接收行为日志和审计日志消息，存入 MongoDB。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogConsumer {

    private final LogService logService;

    @RabbitListener(queues = MqQueue.LOG_BEHAVIOR)
    public void handleBehaviorLog(BehaviorLog behaviorLog) {
        log.info("收到行为日志: userId={}, action={}, target={}", behaviorLog.getUserId(), behaviorLog.getAction(), behaviorLog.getTarget());
        try {
            logService.saveBehaviorLog(behaviorLog);
        } catch (Exception e) {
            log.error("保存行为日志失败: {}", behaviorLog, e);
        }
    }

    @RabbitListener(queues = MqQueue.LOG_AUDIT)
    public void handleAuditLog(AuditLog auditLog) {
        log.info("收到审计日志: userId={}, operation={}, result={}", auditLog.getUserId(), auditLog.getOperation(), auditLog.getResult());
        try {
            logService.saveAuditLog(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", auditLog, e);
        }
    }
}
