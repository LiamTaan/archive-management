package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 统一分页请求DTO
 */
@Data
public class PageRequestDTO {
    /**
     * 当前页码，默认为1
     */
    private Integer current = 1;
    
    /**
     * 每页条数，默认为10
     */
    private Integer pageSize = 10;

    // records
    private List<Object> records;

    // total
    private Long total;

}