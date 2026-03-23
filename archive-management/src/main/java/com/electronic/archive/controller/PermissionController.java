package com.electronic.archive.controller;

import com.electronic.archive.entity.SysPermission;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysPermissionService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 权限管理控制器
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户按钮权限列表
     * @return 按钮权限编码集合
     */
    @Operation(summary = "获取用户按钮权限列表")
    @GetMapping("/getUserButtonPermissions")
    public ResponseResult<Set<String>> getUserButtonPermissions() {
        // 从SecurityContext中获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.fail("用户未登录");
        }

        String username = authentication.getName();
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }

        Set<String> buttonPermissions = sysPermissionService.getButtonPermissionsByUserId(user.getUserId());
        return ResponseResult.success("获取用户按钮权限列表成功", buttonPermissions);
    }

    /**
     * 获取所有权限列表
     * @return 权限树
     */
    @Operation(summary = "获取所有权限列表")
    @GetMapping("/getAllPermissions")
    public ResponseResult<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionService.getAllPermissions();
        return ResponseResult.success("获取所有权限列表成功", permissions);
    }
}