package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.dto.RoleQueryDTO;
import com.electronic.archive.entity.SysPermission;
import com.electronic.archive.entity.SysRole;
import com.electronic.archive.service.SysPermissionService;
import com.electronic.archive.service.SysRolePermissionService;
import com.electronic.archive.service.SysRoleService;
import com.electronic.archive.util.PageRequest;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private SysRoleService sysRoleService;
    
    @Autowired
    private SysPermissionService sysPermissionService;
    
    @Autowired
    private SysRolePermissionService sysRolePermissionService;

    /**
     * 获取角色列表
     * @param page 页码
     * @param size 每页条数
     * @param roleName 角色名称
     * @param status 状态
     * @return 角色列表
     */
    @Operation(summary = "获取角色列表")
    @GetMapping("/list")
    public ResponseResult<PageResult<SysRole>> getRoleList(RoleQueryDTO roleQueryDTO) {
        
        // 构建查询条件
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (roleQueryDTO.getRoleName() != null && !roleQueryDTO.getRoleName().isEmpty()) {
            queryWrapper.like(SysRole::getRoleName, roleQueryDTO.getRoleName());
        }
        if (roleQueryDTO.getStatus() != null && !roleQueryDTO.getStatus().isEmpty()) {
            queryWrapper.eq(SysRole::getStatus, roleQueryDTO.getStatus());
        }
        
        // 执行查询
        var rolePage = sysRoleService.page(roleQueryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<SysRole> result = PageResult.fromMpPage(rolePage);
        
        return ResponseResult.success("获取角色列表成功", result);
    }

    /**
     * 新增角色
     * @param sysRole 角色信息
     * @return 操作结果
     */
    @Operation(summary = "新增角色")
    @PostMapping("/add")
    public ResponseResult<Void> addRole(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return ResponseResult.success("新增角色成功");
    }

    /**
     * 更新角色
     * @param sysRole 角色信息
     * @return 操作结果
     */
    @Operation(summary = "更新角色")
    @PutMapping("/update")
    public ResponseResult<Void> updateRole(@RequestBody SysRole sysRole) {
        sysRoleService.updateById(sysRole);
        return ResponseResult.success("更新角色成功");
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteRole(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return ResponseResult.success("删除角色成功");
    }

    /**
     * 更新角色状态
     * @param id 角色ID
     * @param status 状态
     * @return 操作结果
     */
    @Operation(summary = "更新角色状态")
    @PutMapping("/status/{id}")
    public ResponseResult<Void> updateRoleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        SysRole sysRole = new SysRole();
        sysRole.setRoleId(id);
        sysRole.setStatus(status);
        sysRoleService.updateById(sysRole);
        return ResponseResult.success("更新角色状态成功");
    }

    /**
     * 获取角色权限列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Operation(summary = "获取角色权限列表")
    @GetMapping("/permissions/{roleId}")
    public ResponseResult<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        // 查询角色已关联的权限ID列表
        List<Long> permissionIds = sysRolePermissionService.getPermissionIdsByRoleId(roleId);
        return ResponseResult.success("获取角色权限列表成功", permissionIds);
    }

    /**
     * 保存角色权限
     * @param roleId 角色ID
     * @param permissions 权限ID列表
     * @return 操作结果
     */
    @Operation(summary = "保存角色权限")
    @PostMapping("/permissions/{roleId}")
    public ResponseResult<Void> saveRolePermissions(
            @PathVariable Long roleId, 
            @RequestBody List<Long> permissions) {
        // 保存角色权限关联关系
        boolean result = sysRolePermissionService.saveRolePermissions(roleId, permissions);
        if (result) {
            return ResponseResult.success("保存角色权限成功");
        } else {
            return ResponseResult.fail("保存角色权限失败");
        }
    }

    /**
     * 获取所有权限列表
     * @return 权限树
     */
    @Operation(summary = "获取所有权限列表")
    @GetMapping("/permission/list")
    public ResponseResult<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionService.list();
        
        // 将扁平列表转换为树结构
        List<SysPermission> permissionTree = buildPermissionTree(permissions);
        
        return ResponseResult.success("获取所有权限列表成功", permissionTree);
    }
    
    /**
     * 将扁平的权限列表转换为树结构
     * @param permissions 扁平的权限列表
     * @return 权限树
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions) {
        List<SysPermission> tree = new ArrayList<>();
        
        // 使用Map存储权限ID到权限对象的映射，方便快速查找
        Map<Long, SysPermission> permissionMap = new HashMap<>();
        for (SysPermission permission : permissions) {
            // 为每个权限对象添加children属性
            permissionMap.put(permission.getPermissionId(), permission);
        }
        
        // 构建树结构
        for (SysPermission permission : permissions) {
            Long parentId = permission.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点，直接添加到树中
                tree.add(permission);
            } else {
                // 子节点，添加到父节点的children中
                SysPermission parent = permissionMap.get(parentId);
                if (parent != null) {
                    // 初始化children列表
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permission);
                }
            }
        }
        
        return tree;
    }
}
