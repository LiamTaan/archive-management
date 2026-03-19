package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案关联关系表
 * 用于记录同一业务流程下多个档案之间的关联关系
 */
@Data
@TableName("archive_relation")
public class ArchiveRelation {
    /**
     * 关联ID，主键，自增
     */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long id;

    /**
     * 主档案ID
     */
    @TableField("main_archive_id")
    private Long mainArchiveId;

    /**
     * 关联档案ID
     */
    @TableField("related_archive_id")
    private Long relatedArchiveId;

    /**
     * 关联类型（如：同一业务流程、补充协议、审批单据等）
     */
    @TableField("relation_type")
    private String relationType;

    /**
     * 关联描述
     */
    @TableField("relation_desc")
    private String description;

    /**
     * 创建人
     */
    @TableField("creator")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField("updater")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}