package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统参数表
 */
@Data
@TableName("system_param")
public class SystemParam {
    /**
     * 参数ID，主键，自增
     */
    @TableId(value = "param_id", type = IdType.AUTO)
    private Long id;

    /**
     * 参数名称
     */
    @TableField("param_name")
    private String paramName;

    /**
     * 参数键名
     */
    @TableField("param_key")
    private String paramKey;

    /**
     * 参数值
     */
    @TableField("param_value")
    private String paramValue;

    /**
     * 参数类型(0-字符串，1-数字，2-布尔值，3-枚举)
     */
    @TableField("param_type")
    private Integer paramType;

    /**
     * 参数描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态(0-禁用，1-启用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operateBy;

    /**
     * 操作时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}