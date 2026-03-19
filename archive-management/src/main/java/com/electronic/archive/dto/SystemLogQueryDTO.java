package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 系统日志查询DTO
 */
@Data
public class SystemLogQueryDTO extends PageRequest {
    /**
     * 日志级别
     */
    private Integer logLevel;
    
    /**
     * 日志类型
     */
    private Integer logType;
    
    /**
     * 操作人
     */
    private String operateBy;
    
    /**
     * 操作IP
     */
    private String operateIp;
}