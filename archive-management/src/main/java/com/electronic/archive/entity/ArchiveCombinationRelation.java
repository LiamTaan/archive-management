package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案组合关系实体类
 */
@Data
@TableName("archive_combination_relation")
public class ArchiveCombinationRelation {
    /**
     * 关系ID
     */
    @TableId(value = "relation_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    /**
     * 组合ID
     */
    private Long combinationId;

    /**
     * 档案ID
     */
    private Long archiveId;

    /**
     * 档案在组合中的顺序
     */
    private Integer archiveOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}