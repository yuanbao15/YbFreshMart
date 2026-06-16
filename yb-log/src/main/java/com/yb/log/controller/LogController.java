package com.yb.log.controller;

import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import com.yb.log.document.AuditLog;
import com.yb.log.document.BehaviorLog;
import com.yb.log.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 日志接口（查询 + 写入）
 */
@Tag(name = "日志服务", description = "行为日志 & 审计日志查询与写入")
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    // ==================== 查询 ====================

    @Operation(summary = "查询行为日志")
    @GetMapping("/behavior")
    public R<PageDTO<BehaviorLog>> queryBehaviorLogs(
            @Parameter(description = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") long size) {
        return R.ok(logService.queryBehaviorLogs(userId, page, size));
    }

    @Operation(summary = "查询审计日志")
    @GetMapping("/audit")
    public R<PageDTO<AuditLog>> queryAuditLogs(
            @Parameter(description = "用户ID（可选）") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") long size) {
        return R.ok(logService.queryAuditLogs(userId, page, size));
    }

    // ==================== 写入（测试用，不需要 MQ） ====================

    @Operation(summary = "写入行为日志")
    @PostMapping("/behavior")
    public R<Void> saveBehaviorLog(@RequestBody BehaviorLog behaviorLog) {
        if (behaviorLog.getTimestamp() == null) {
            behaviorLog.setTimestamp(LocalDateTime.now());
        }
        logService.saveBehaviorLog(behaviorLog);
        return R.ok();
    }

    @Operation(summary = "写入审计日志")
    @PostMapping("/audit")
    public R<Void> saveAuditLog(@RequestBody AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        logService.saveAuditLog(auditLog);
        return R.ok();
    }

    // ==================== 批量写入测试数据 ====================

    @Operation(summary = "一键灌入测试日志")
    @PostMapping("/batch")
    public R<String> seedTestData() {
        List<String> actions = List.of("VIEW", "SEARCH", "ADD_CART", "CLICK", "VIEW");
        List<String> targets = List.of("有机菠菜", "红富士苹果", "鲜牛奶 1L", "猪里脊", "哈密瓜");
        List<String> ips = List.of("192.168.1.100", "10.0.0.5", "172.16.0.1");

        IntStream.range(0, 10).forEach(i -> {
            BehaviorLog b = new BehaviorLog();
            b.setUserId((long) (1001 + i % 3));
            b.setAction(actions.get(i % actions.size()));
            b.setTarget("1000" + (i + 1));
            b.setTargetDesc(targets.get(i % targets.size()));
            b.setIp(ips.get(i % ips.size()));
            b.setUserAgent("Mozilla/5.0 (Windows NT 10.0) Chrome/120");
            b.setTimestamp(LocalDateTime.now().minusMinutes(i * 5));
            logService.saveBehaviorLog(b);
        });

        List<String> ops = List.of("LOGIN", "REGISTER", "UPDATE_PROFILE", "LOGIN", "GRANT");
        List<String> results = List.of("SUCCESS", "FAILURE", "SUCCESS", "SUCCESS", "SUCCESS");
        List<String> details = List.of(
                "138****1111 登录成功",
                "138****2222 注册失败：手机号已存在",
                "修改昵称为 张三",
                "138****3333 登录成功",
                "管理员授予用户1001角色"
        );

        IntStream.range(0, 5).forEach(i -> {
            AuditLog a = new AuditLog();
            a.setUserId(1001L + i);
            a.setOperation(ops.get(i));
            a.setDetail(details.get(i));
            a.setResult(results.get(i));
            a.setIp("192.168.1.1");
            a.setTimestamp(LocalDateTime.now().minusHours(i));
            logService.saveAuditLog(a);
        });

        return R.ok("已写入 10 条行为日志 + 5 条审计日志");
    }
}
