package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.SysPermission;

import java.util.List;

public interface SysPermissionMapper extends BaseMapper<SysPermission> {
    /**
     * 根据用户ID获取菜单列表
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysPermission> getMenuListByUserId(Long userId);
    
    /**
     * 根据角色编码获取菜单列表
     * @param roleCodes 角色编码列表
     * @return 菜单列表
     */
    List<SysPermission> getMenusByRoleCodes(List<String> roleCodes);
}