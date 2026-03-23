package com.electronic.archive.config;

import com.electronic.archive.interceptor.ApiMonitoringInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiMonitoringInterceptor apiMonitoringInterceptor;

    @Autowired
    public WebMvcConfig(ApiMonitoringInterceptor apiMonitoringInterceptor) {
        this.apiMonitoringInterceptor = apiMonitoringInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册API监控拦截器，拦截所有请求
        registry.addInterceptor(apiMonitoringInterceptor)
                .addPathPatterns("/**")
                // 排除Swagger、Actuator和认证接口
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v3/**", "/swagger-ui/**", "/actuator/**", "/auth/**");
    }
}