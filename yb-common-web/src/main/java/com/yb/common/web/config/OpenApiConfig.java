package com.yb.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / Knife4j 公共配置
 * <p>
 * 每个服务在 application.yml 中设置自己的标题、描述、版本即可。
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:unknown}")
    private String appName;

    @Value("${swagger.title:FreshMart API}")
    private String title;

    @Value("${swagger.description:微服务接口文档}")
    private String description;

    @Value("${swagger.version:1.0.0}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("FreshMart Team")));
    }
}
