package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysPermission;
import com.electronic.archive.mapper.SysPermissionMapper;
import com.electronic.archive.service.SysPermissionService;
import com.electronic.archive.service.SysRolePermissionService;
import com.electronic.archive.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRolePermissionService sysRolePermissionService;

    /**
     * 根据用户ID获取按钮权限列表
     * @param userId 用户ID
     * @return 按钮权限编码集合
     */
    @Override
    public Set<String> getButtonPermissionsByUserId(Long userId) {
        Set<String> buttonPermissions = new HashSet<>();

        // 1. 获取用户角色
        List<String> roleNames = sysUserRoleService.getRoleNamesByUserId(userId);
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of("BUSINESS_OPERATOR"); // 默认角色
        }

        // 2. 根据角色获取权限ID列表
        List<Long> permissionIds = sysRolePermissionService.getPermissionIdsByRoleNames(roleNames);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return buttonPermissions;
        }

        // 3. 根据权限ID列表获取按钮权限
        List<SysPermission> permissions = this.list(new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermissionId, permissionIds)
                .eq(SysPermission::getPermissionType, "button")
                .eq(SysPermission::getStatus, 1));

        // 4. 提取按钮权限编码
        buttonPermissions = permissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());

        return buttonPermissions;
    }

    /**
     * 获取所有权限列表（包括按钮）
     * @return 权限树
     */
    @Override
    public List<SysPermission> getAllPermissions() {
        // 查询所有启用的权限
        List<SysPermission> permissions = this.list(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSortOrder));

        // 构建权限树
        return buildPermissionTree(permissions);
    }

    /**
     * 构建权限树
     * @param permissions 权限列表
     * @return 权限树
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions) {
        List<SysPermission> permissionTree = new ArrayList<>();

        // 确保所有权限对象的必要字段都有默认值
        List<SysPermission> safePermissions = permissions.stream().map(permission -> {
            if (permission.getPermissionName() == null) {
                permission.setPermissionName("");
            }
            if (permission.getPermissionCode() == null) {
                permission.setPermissionCode("");
            }
            if (permission.getPermissionType() == null) {
                permission.setPermissionType("");
            }
            if (permission.getPath() == null) {
                permission.setPath("");
            }
            if (permission.getComponent() == null) {
                permission.setComponent("");
            }
            if (permission.getIcon() == null) {
                permission.setIcon("");
            }
            // 初始化children列表为空列表，而不是null
            if (permission.getChildren() == null) {
                permission.setChildren(new ArrayList<>());
            }
            return permission;
        }).collect(Collectors.toList());

        // 首先找到所有根权限（parentId为0或null）
        List<SysPermission> rootPermissions = safePermissions.stream()
                .filter(permission -> permission.getParentId() == null || permission.getParentId() == 0)
                .collect(Collectors.toList());

        // 递归构建权限树
        for (SysPermission rootPermission : rootPermissions) {
            buildPermissionChild(rootPermission, safePermissions);
            permissionTree.add(rootPermission);
        }

        return permissionTree;
    }

    /**
     * 递归构建子权限
     * @param parentPermission 父权限
     * @param permissions 所有权限列表
     */
    private void buildPermissionChild(SysPermission parentPermission, List<SysPermission> permissions) {
        // 找到当前权限的所有子权限
        List<SysPermission> childPermissions = permissions.stream()
                .filter(permission -> parentPermission.getPermissionId().equals(permission.getParentId()))
                .collect(Collectors.toList());

        if (!childPermissions.isEmpty()) {
            parentPermission.setChildren(childPermissions);
            // 递归构建子权限的子权限
            for (SysPermission childPermission : childPermissions) {
                buildPermissionChild(childPermission, permissions);
            }
        }
    }

    @Override
    public List<SysPermission> getMenuListByUserId(Long userId) {
        return baseMapper.getMenuListByUserId(userId);
    }

    @Override
    public List<SysPermission> getMenusByRoleCodes(List<String> roleCodes) {
        return baseMapper.getMenusByRoleCodes(roleCodes);
    }

    @Override
    public List<SysPermission> buildMenuTree(List<SysPermission> menuList) {
        List<SysPermission> menuTree = new ArrayList<>();
        
        // 确保所有菜单对象的必要字段都有默认值
        List<SysPermission> safeMenuList = menuList.stream().map(menu -> {
            if (menu.getPermissionName() == null) {
                menu.setPermissionName("");
            }
            if (menu.getPermissionCode() == null) {
                menu.setPermissionCode("");
            }
            if (menu.getPermissionType() == null) {
                menu.setPermissionType("");
            }
            if (menu.getPath() == null) {
                menu.setPath("");
            }
            if (menu.getComponent() == null) {
                menu.setComponent("");
            }
            if (menu.getIcon() == null) {
                menu.setIcon("");
            }
            // 初始化children列表为空列表，而不是null
            if (menu.getChildren() == null) {
                menu.setChildren(new ArrayList<>());
            }
            return menu;
        }).collect(Collectors.toList());
        
        // 首先找到所有根菜单（parentId为0或null）
        List<SysPermission> rootMenus = safeMenuList.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());
        
        // 递归构建菜单树
        for (SysPermission rootMenu : rootMenus) {
            buildPermissionChild(rootMenu, safeMenuList);
            menuTree.add(rootMenu);
        }
        
        return menuTree;
    }
}