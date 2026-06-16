package com.yb.api.fallback;

import com.yb.api.client.LogClient;
import com.yb.common.dto.PageDTO;
import com.yb.common.dto.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class LogClientFallbackFactory implements FallbackFactory<LogClient> {

    @Override
    public LogClient create(Throwable cause) {
        log.error("LogClient 调用失败，触发降级", cause);
        return new LogClient() {
            @Override
            public R<PageDTO<Map<String, Object>>> queryBehaviorLogs(Long userId, long page, long size) {
                return R.fail(500, "日志服务暂时不可用");
            }

            @Override
            public R<PageDTO<Map<String, Object>>> queryAuditLogs(Long userId, long page, long size) {
                return R.fail(500, "日志服务暂时不可用");
            }
        };
    }
}
