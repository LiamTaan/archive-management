package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysPermission;

import java.util.List;
import java.util.Set;

public interface SysPermissionService extends IService<SysPermission> {
    
    /**
     * 根据用户ID获取按钮权限列表
     * @param userId 用户ID
     * @return 按钮权限编码集合
     */
    Set<String> getButtonPermissionsByUserId(Long userId);
    
    /**
     * 获取所有权限列表（包括按钮）
     * @return 权限树
     */
    List<SysPermission> getAllPermissions();
    
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
    
    /**
     * 构建菜单树
     * @param menuList 菜单列表
     * @return 菜单树
     */
    List<SysPermission> buildMenuTree(List<SysPermission> menuList);
}