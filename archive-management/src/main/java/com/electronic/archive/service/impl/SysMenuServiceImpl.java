package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysMenu;
import com.electronic.archive.mapper.SysMenuMapper;
import com.electronic.archive.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统菜单Service实现类
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuListByUserId(Long userId) {
        // 此处简化实现，实际应该根据用户角色获取菜单
        // 目前返回所有启用的菜单
        return this.baseMapper.selectList(null).stream()
                .filter(menu -> menu.getStatus() == 1)
                .sorted((a, b) -> Integer.compare(a.getSort(), b.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menuList) {
        List<SysMenu> menuTree = new ArrayList<>();
        
        // 首先找到所有根菜单（parentId为0或null）
        List<SysMenu> rootMenus = menuList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());
        
        // 递归构建菜单树
        for (SysMenu rootMenu : rootMenus) {
            buildMenuChild(rootMenu, menuList);
            menuTree.add(rootMenu);
        }
        
        return menuTree;
    }
    
    /**
     * 递归构建子菜单
     * @param parentMenu 父菜单
     * @param menuList 所有菜单列表
     */
    private void buildMenuChild(SysMenu parentMenu, List<SysMenu> menuList) {
        // 找到当前菜单的所有子菜单
        List<SysMenu> childMenus = menuList.stream()
                .filter(menu -> parentMenu.getMenuId().equals(menu.getParentId()))
                .sorted((a, b) -> Integer.compare(a.getSort(), b.getSort()))
                .collect(Collectors.toList());
        
        if (!childMenus.isEmpty()) {
            parentMenu.setChildren(childMenus);
            // 递归构建子菜单的子菜单
            for (SysMenu childMenu : childMenus) {
                buildMenuChild(childMenu, menuList);
            }
        }
    }
}
