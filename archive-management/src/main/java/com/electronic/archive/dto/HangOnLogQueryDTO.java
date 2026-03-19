package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 挂接日志查询DTO
 */
@Data
public class HangOnLogQueryDTO extends PageRequest {
    private Long archiveId;
    private String operateBy;
    private Integer hangOnType;
    private Integer result;
}
