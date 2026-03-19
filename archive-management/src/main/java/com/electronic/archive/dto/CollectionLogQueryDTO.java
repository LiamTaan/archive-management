package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 采集日志查询DTO
 */
@Data
public class CollectionLogQueryDTO extends PageRequest {
    private Long archiveId;
    private Integer collectionType;
    private String operateBy;
    private Integer result;
}