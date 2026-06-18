package com.yb.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.yb")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.yb.api")
@MapperScan("com.yb.product.mapper")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
