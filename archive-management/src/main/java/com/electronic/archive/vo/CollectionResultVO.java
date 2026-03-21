package com.electronic.archive.vo;

import lombok.Data;

/**
 * 采集结果VO
 */
@Data
public class CollectionResultVO {
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 结果描述
     */
    private String description;
    
    /**
     * 任务ID
     */
    private String taskId;
}