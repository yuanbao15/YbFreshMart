package com.yb.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证授权服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.yb")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yb.api")
@MapperScan("com.yb.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
