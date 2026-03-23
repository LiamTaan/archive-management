package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysUserRole;

import java.util.List;

/**
 * 用户角色关联Service
 */
public interface SysUserRoleService extends IService<SysUserRole> {
    /**
     * 根据用户ID获取角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
    
    /**
     * 分配角色给用户
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 获取用户的角色名称列表
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getRoleNamesByUserId(Long userId);

    /**
     * 获取用户的角色编码列表
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);
    
    /**
     * 根据角色编码获取用户列表
     * @param roleCode 角色编码
     * @return 用户列表
     */
    List<String> getUsersByRoleCode(String roleCode);
}
