package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysRolePermission;

import java.util.List;

/**
 * 角色权限关联Service
 */
public interface SysRolePermissionService extends IService<SysRolePermission> {
    /**
     * 根据角色ID获取权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
    
    /**
     * 保存角色权限关联关系
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean saveRolePermissions(Long roleId, List<Long> permissionIds);
}