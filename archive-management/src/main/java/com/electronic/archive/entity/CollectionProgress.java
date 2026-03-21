package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案采集进度实体类
 */
@Data
@TableName("collection_progress")
public class CollectionProgress {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 采集任务ID
     */
    private String taskId;

    /**
     * 采集类型：0-手动上传，1-批量上传，2-自动接口采集，3-外部导入
     */
    private Integer collectionType;

    /**
     * 总进度（0-100）
     */
    private Integer progress;

    /**
     * 已处理数量
     */
    private Integer processedCount;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 状态：0-进行中，1-完成，2-失败
     */
    private Integer status;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
