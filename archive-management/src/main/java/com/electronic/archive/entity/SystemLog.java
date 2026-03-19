package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统日志表
 */
@Data
@TableName("system_log")
public class SystemLog {
    /**
     * 日志ID，主键，自增
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long id;

    /**
     * 日志级别(0-INFO，1-WARN，2-ERROR，3-DEBUG)
     */
    @TableField("log_level")
    private Integer logLevel;

    /**
     * 日志类型(0-系统启动，1-系统关闭，2-配置变更，3-异常记录，4-性能监控)
     */
    @TableField("log_type")
    private Integer logType;

    /**
     * 日志标题
     */
    @TableField("title")
    private String title;

    /**
     * 日志内容
     */
    @TableField("content")
    private String content;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operateBy;

    /**
     * 操作IP
     */
    @TableField("operate_ip")
    private String operateIp;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 异常信息
     */
    @TableField("exception_info")
    private String exceptionInfo;

    /**
     * 执行时长(毫秒)
     */
    @TableField("execute_time")
    private Long executeTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}