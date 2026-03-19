package com.electronic.archive.vo;

import lombok.Data;

/**
 * 挂接结果视图对象
 */
@Data
public class HangOnResultVO {
    /**
     * 挂接总数
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
     * 挂接结果描述
     */
    private String description;
}