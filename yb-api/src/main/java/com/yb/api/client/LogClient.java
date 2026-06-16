package com.yb.api.client;

import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 日志服务 Feign 接口
 */
@FeignClient(
        name = "yb-log",
        path = "/api/log",
        fallbackFactory = com.yb.api.fallback.LogClientFallbackFactory.class
)
public interface LogClient {

    @GetMapping("/behavior")
    R<PageDTO<Map<String, Object>>> queryBehaviorLogs(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "20") long size);

    @GetMapping("/audit")
    R<PageDTO<Map<String, Object>>> queryAuditLogs(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "size", defaultValue = "20") long size);
}
