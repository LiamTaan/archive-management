package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 档案组合查询DTO
 * 用于接收档案组合查询请求参数
 */
@Data
public class ArchiveCombinationQueryDTO extends PageRequest {
    /**
     * 组合名称（支持模糊查询）
     */
    private String combinationName;
    
    /**
     * 组合状态
     */
    private Integer status;
    
    /**
     * 组合类型
     */
    private String combinationType;
}