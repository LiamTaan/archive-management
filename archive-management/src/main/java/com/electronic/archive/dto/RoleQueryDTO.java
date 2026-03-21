package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 角色查询DTO
 */
@Data
public class RoleQueryDTO extends PageRequest {
    /**
     * 角色名称（支持模糊查询）
     */
    private String roleName;
    
    /**
     * 角色编码（支持模糊查询）
     */
    private String roleCode;
    
    /**
     * 角色状态（0-禁用，1-启用）
     */
    private String status;
}