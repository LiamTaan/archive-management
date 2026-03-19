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
     * 业务系统名称
     */
    private String businessSystem;

    /**
     * 接口URL
     */
    @TableField("url")
    private String interfaceUrl;

    /**
     * 请求方法（GET, POST, PUT, DELETE）
     */
    private String requestMethod;

    /**
     * 参数映射（JSON格式）
     */
    @TableField("param_mapping")
    private String requestParams;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}