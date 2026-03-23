package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.SysRolePermission;

import java.util.List;

/**
 * 角色权限关联Mapper
 */
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    /**
     * 根据角色ID获取权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
    
    /**
     * 根据角色名称列表获取权限ID列表
     * @param roleNames 角色名称列表
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleNames(List<String> roleNames);
    
    /**
     * 根据角色ID删除所有关联的权限
     * @param roleId 角色ID
     * @return 删除的数量
     */
    int deleteByRoleId(Long roleId);
}