package com.electronic.archive.vo;

import lombok.Data;

/**
 * 采集结果视图对象
 */
@Data
public class CollectionResultVO {
    /**
     * 采集总数
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
     * 采集结果描述
     */
    private String description;
}