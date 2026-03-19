package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 挂接日志表
 */
@Data
@TableName("hang_on_log")
public class HangOnLog {
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
     * 操作人
     */
    @TableField("operator")
    private String operateBy;

    /**
     * 操作时间
     */
    @TableField("operate_time")
    private LocalDateTime createTime;

    /**
     * 操作类型(0-挂接，1-修改，2-解除)
     */
    @TableField("operate_type")
    private Integer hangOnType;

    /**
     * 操作内容
     */
    @TableField("operate_content")
    private String description;

    /**
     * 挂接结果（1：成功，2：失败）
     */
    @TableField("result")
    private Integer result;

    /**
     * 失败原因
     */
    @TableField("reason")
    private String errorInfo;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}