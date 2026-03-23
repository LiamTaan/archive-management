package com.electronic.archive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 电子档案管理系统主启动类
 */
@SpringBootApplication
@MapperScan("com.electronic.archive.mapper")
@EnableAsync // 启用Spring异步执行支持
@EnableScheduling // 启用Spring定时任务支持
public class ArchiveManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArchiveManagementApplication.class, args);
    }
}