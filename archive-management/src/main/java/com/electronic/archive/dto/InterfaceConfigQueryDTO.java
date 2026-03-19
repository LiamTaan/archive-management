package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 接口配置查询DTO
 */
@Data
public class InterfaceConfigQueryDTO extends PageRequest {
    private String interfaceName;
    private Integer interfaceType;
    private Integer status;
}
