package com.yb.common.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Favicon — 所有服务自动生效，消除 404 报错
 */
@AutoConfiguration
public class FaviconConfig {

    @Bean
    public FaviconController faviconController() {
        return new FaviconController();
    }

    @RestController
    static class FaviconController {

        @GetMapping("/favicon.ico")
        public ResponseEntity<Void> favicon() {
            return ResponseEntity.noContent()
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                    .build();
        }
    }
}
