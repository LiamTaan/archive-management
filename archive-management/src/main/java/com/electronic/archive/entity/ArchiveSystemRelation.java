package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案与目标系统关联关系表
 */
@Data
@TableName("archive_system_relation")
public class ArchiveSystemRelation {
    /**
     * 关联ID，主键，自增
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long id;

    /**
     * 档案ID
     */
    @TableField("archive_id")
    private Long archiveId;

    /**
     * 目标系统接口编码
     */
    @TableField("system_code")
    private String systemCode;

    /**
     * 目标系统中文名称
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 挂接状态（0-未挂接，1-已挂接，2-挂接失败）
     */
    @TableField("hang_on_status")
    private Integer hangOnStatus;

    /**
     * 挂接时间
     */
    @TableField("hang_on_time")
    private LocalDateTime hangOnTime;

    /**
     * 挂接方式(0-自动，1-手动)
     */
    @TableField("hang_on_type")
    private Integer hangOnType;

    /**
     * 操作人
     */
    @TableField("operate_by")
    private String operateBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}