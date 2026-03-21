package com.electronic.archive.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案采集进度VO
 */
@Data
public class CollectionProgressVO {
    
    /**
     * 任务ID
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
     * 更新时间
     */
    private LocalDateTime updateTime;
}
