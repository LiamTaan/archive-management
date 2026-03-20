package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口配置表
 */
@Data
@TableName("interface_config")
public class InterfaceConfig {
    /**
     * 配置ID，主键，自增
     */
    @TableId(value = "interface_id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口名称
     */
    private String interfaceName;
    
    /**
     * 接口编码
     */
    private String interfaceCode;

    /**
     * 业务系统名称
     */
    private String businessSystem;

    /**
     * 接口URL
     */
    @TableField("url")
    private String interfaceUrl;
    
    /**
     * 文件元信息接口URL
     */
    private String metadataUrl;

    /**
     * 请求方法（GET, POST, PUT, DELETE）
     */
    private String requestMethod;

    /**
     * 参数映射（JSON格式）
     */
    @TableField("request_params")
    private String requestParams;
    
    /**
     * 请求头（JSON格式）
     */
    private String requestHeaders;

    /**
     * 接口密钥
     */
    private String secretKey;

    /**
     * 超时时间(秒)
     */
    private Integer timeout;

    /**
     * 接口状态（1：启用，0：禁用）
     */
    private Integer status;
    
    /**
     * 单文件大小限制（MB），默认50MB
     */
    private Integer maxFileSize = 50;
    
    /**
     * 传输模式：DIRECT-直传，SHARD-分片
     */
    private String transferMode = "SHARD";
    
    /**
     * 采集调度规则（Cron表达式）
     */
    private String cronExpression;
    
    /**
     * 大文件分片大小(MB)，默认10MB
     */
    private Integer chunkSize = 10;
    
    /**
     * 文件存储路径
     */
    private String storagePath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}