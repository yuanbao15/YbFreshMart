package com.yb.common.web.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Favicon 控制器 — 所有服务自动生效，消除 404 报错
 */
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                .build();
    }
}
