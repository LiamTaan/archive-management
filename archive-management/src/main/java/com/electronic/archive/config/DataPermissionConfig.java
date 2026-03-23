package com.electronic.archive.config;

import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.interceptor.DataPermissionInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限配置类
 * 用于注册数据权限拦截器到Spring AOP中
 */
@Configuration
public class DataPermissionConfig {

    @Autowired
    private DataPermissionInterceptor dataPermissionInterceptor;

    /**
     * 创建数据权限AOP切面
     * @return Advisor
     */
    @Bean
    public Advisor dataPermissionAdvisor() {
        // 创建AspectJ表达式切点，匹配所有带有@DataPermission注解的方法
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(com.electronic.archive.annotation.DataPermission)");
        
        // 创建Advisor，将切点和拦截器关联
        return new DefaultPointcutAdvisor(pointcut, dataPermissionInterceptor);
    }
}