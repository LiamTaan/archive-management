package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.entity.SysPermission;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysPermissionService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户菜单列表
     * @return 菜单树
     */
    @Operation(summary = "获取用户菜单列表")
    @GetMapping("/getUserMenus")
    public ResponseResult<List<SysPermission>> getUserMenus() {
        // 从SecurityContext中获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.fail("用户未登录");
        }
        
        String username = authentication.getName();
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }
        
        Long userId = user.getUserId();
        List<SysPermission> menuList = sysPermissionService.getMenuListByUserId(userId);
        List<SysPermission> menuTree = sysPermissionService.buildMenuTree(menuList);
        
        return ResponseResult.success("获取菜单列表成功", menuTree);
    }

    /**
     * 获取所有菜单列表
     * @return 菜单树
     */
    @Operation(summary = "获取所有菜单列表")
    @GetMapping("/list")
    public ResponseResult<List<SysPermission>> getAllMenus() {
        // 只查询目录和菜单类型的权限，排除按钮和API
        List<SysPermission> menuList = sysPermissionService.list(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermissionType, "directory", "menu")
                .orderByAsc(SysPermission::getSortOrder)
        );
        List<SysPermission> menuTree = sysPermissionService.buildMenuTree(menuList);
        
        return ResponseResult.success("获取所有菜单列表成功", menuTree);
    }

    /**
     * 获取菜单详情
     * @param id 菜单ID
     * @return 菜单详情
     */
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public ResponseResult<SysPermission> getMenuById(@PathVariable Long id) {
        SysPermission menu = sysPermissionService.getById(id);
        if (menu == null) {
            return ResponseResult.fail("菜单不存在");
        }
        
        return ResponseResult.success("获取菜单详情成功", menu);
    }

    /**
     * 新增菜单
     * @param permission 菜单信息
     * @return 操作结果
     */
    @Operation(summary = "新增菜单")
    @PostMapping("/add")
    public ResponseResult<Void> addMenu(@RequestBody SysPermission permission) {
        // 确保只添加目录或菜单类型
        if (!"directory".equals(permission.getPermissionType()) && !"menu".equals(permission.getPermissionType())) {
            permission.setPermissionType("menu"); // 默认设置为菜单类型
        }
        
        boolean success = sysPermissionService.save(permission);
        if (success) {
            return ResponseResult.success("新增菜单成功");
        } else {
            return ResponseResult.fail("新增菜单失败");
        }
    }

    /**
     * 更新菜单
     * @param permission 菜单信息
     * @return 操作结果
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/update")
    public ResponseResult<Void> updateMenu(@RequestBody SysPermission permission) {
        // 确保只更新目录或菜单类型
        if (!"directory".equals(permission.getPermissionType()) && !"menu".equals(permission.getPermissionType())) {
            permission.setPermissionType("menu"); // 默认设置为菜单类型
        }
        
        boolean success = sysPermissionService.updateById(permission);
        if (success) {
            return ResponseResult.success("更新菜单成功");
        } else {
            return ResponseResult.fail("更新菜单失败");
        }
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 操作结果
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteMenu(@PathVariable Long id) {
        // 检查是否有子菜单
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getParentId, id);
        long count = sysPermissionService.count(queryWrapper);
        if (count > 0) {
            return ResponseResult.fail("该菜单下存在子菜单，无法删除");
        }
        
        boolean success = sysPermissionService.removeById(id);
        if (success) {
            return ResponseResult.success("删除菜单成功");
        } else {
            return ResponseResult.fail("删除菜单失败");
        }
    }

    /**
     * 获取菜单列表（用于下拉选择）
     * @return 菜单列表
     */
    @Operation(summary = "获取菜单列表（用于下拉选择）")
    @GetMapping("/selectList")
    public ResponseResult<List<SysPermission>> getMenuSelectList() {
        // 只查询目录和菜单类型的权限
        List<SysPermission> menuList = sysPermissionService.list(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getPermissionType, "directory", "menu")
                .orderByAsc(SysPermission::getSortOrder)
        );
        return ResponseResult.success("获取菜单选择列表成功", menuList);
    }
}
