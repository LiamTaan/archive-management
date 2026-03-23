package com.electronic.archive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 */
@Configuration
public class OpenAPIConfig {

    /**
     * 配置OpenAPI信息
     * @return OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("电子档案管理系统API")
                        .version("1.0.0")
                        .description("电子档案管理系统的权限控制与档案管理API接口文档")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("support@electronic-archive.com")
                                .url("https://www.electronic-archive.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}