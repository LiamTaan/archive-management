package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electronic.archive.entity.SysMenu;
import com.electronic.archive.service.SysMenuService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SysMenuService sysMenuService;

    /**
     * 获取用户菜单列表
     * @return 菜单树
     */
    @Operation(summary = "获取用户菜单列表")
    @GetMapping("/getUserMenus")
    public ResponseResult<List<SysMenu>> getUserMenus() {
        // 此处简化实现，实际应该从SecurityContext中获取当前用户ID
        Long userId = 1L;
        
        List<SysMenu> menuList = sysMenuService.getMenuListByUserId(userId);
        List<SysMenu> menuTree = sysMenuService.buildMenuTree(menuList);
        
        return ResponseResult.success("获取菜单列表成功", menuTree);
    }

    /**
     * 获取所有菜单列表
     * @return 菜单树
     */
    @Operation(summary = "获取所有菜单列表")
    @GetMapping("/list")
    public ResponseResult<List<SysMenu>> getAllMenus() {
        List<SysMenu> menuList = sysMenuService.list();
        List<SysMenu> menuTree = sysMenuService.buildMenuTree(menuList);
        
        return ResponseResult.success("获取所有菜单列表成功", menuTree);
    }

    /**
     * 获取菜单详情
     * @param id 菜单ID
     * @return 菜单详情
     */
    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public ResponseResult<SysMenu> getMenuById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) {
            return ResponseResult.fail("菜单不存在");
        }
        
        return ResponseResult.success("获取菜单详情成功", menu);
    }

    /**
     * 新增菜单
     * @param menu 菜单信息
     * @return 操作结果
     */
    @Operation(summary = "新增菜单")
    @PostMapping("/add")
    public ResponseResult<Void> addMenu(@RequestBody SysMenu menu) {
        boolean success = sysMenuService.save(menu);
        if (success) {
            return ResponseResult.success("新增菜单成功");
        } else {
            return ResponseResult.fail("新增菜单失败");
        }
    }

    /**
     * 更新菜单
     * @param menu 菜单信息
     * @return 操作结果
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/update")
    public ResponseResult<Void> updateMenu(@RequestBody SysMenu menu) {
        boolean success = sysMenuService.updateById(menu);
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
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getParentId, id);
        long count = sysMenuService.count(queryWrapper);
        if (count > 0) {
            return ResponseResult.fail("该菜单下存在子菜单，无法删除");
        }
        
        boolean success = sysMenuService.removeById(id);
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
    public ResponseResult<List<SysMenu>> getMenuSelectList() {
        List<SysMenu> menuList = sysMenuService.list();
        return ResponseResult.success("获取菜单选择列表成功", menuList);
    }
}
