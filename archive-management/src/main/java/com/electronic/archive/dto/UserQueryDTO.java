package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO extends PageRequest {
    /**
     * 用户名（支持模糊查询）
     */
    private String username;
    
    /**
     * 用户状态（0-禁用，1-启用）
     */
    private String status;
}