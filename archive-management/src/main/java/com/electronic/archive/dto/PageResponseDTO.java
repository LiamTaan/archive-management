package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 统一分页响应DTO
 */
@Data
public class PageResponseDTO {
    /**
     * 数据列表
     */
    private List<?> records;
    
    /**
     * 总条数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer current;
    
    /**
     * 每页条数
     */
    private Integer size;
}
