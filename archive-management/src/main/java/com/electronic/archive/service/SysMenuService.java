package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysMenu;

import java.util.List;

/**
 * 系统菜单Service
 */
public interface SysMenuService extends IService<SysMenu> {
    
    /**
     * 根据用户ID获取菜单列表
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> getMenuListByUserId(Long userId);
    
    /**
     * 构建菜单树
     * @param menuList 菜单列表
     * @return 菜单树
     */
    List<SysMenu> buildMenuTree(List<SysMenu> menuList);
}
