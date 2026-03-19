package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集日志表
 */
@Data
@TableName("collection_log")
public class CollectionLog {
    /**
     * 日志ID，主键，自增
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long id;

    /**
     * 档案ID
     */
    @TableField("archive_id")
    private Long archiveId;

    /**
     * 采集方式(0-手动，1-批量，2-接口，3-外部导入)
     */
    @TableField("collection_type")
    private Integer collectionType;

    /**
     * 操作人
     */
    @TableField("operator")
    private String operateBy;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    @JsonProperty("createTime")
    private LocalDateTime operateTime;

    /**
     * 操作内容
     */
    @TableField("description")
    private String description;

    /**
     * 采集结果（1：成功，2：失败）
     */
    @TableField("result")
    private Integer result;

    /**
     * 失败原因
     */
    @TableField("error_info")
    private String errorInfo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}