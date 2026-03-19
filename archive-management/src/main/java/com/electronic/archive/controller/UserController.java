package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.dto.UserQueryDTO;
import com.electronic.archive.entity.SysRole;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysRoleService;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.util.PageRequest;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SysRoleService sysRoleService;
    
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户列表
     * @param page 页码
     * @param size 每页条数
     * @param username 用户名
     * @param status 状态
     * @return 用户列表
     */
    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public ResponseResult<PageResult<SysUser>> getUserList(UserQueryDTO userQueryDTO) {
        
        // 构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (userQueryDTO.getUsername() != null && !userQueryDTO.getUsername().isEmpty()) {
            queryWrapper.like(SysUser::getUsername, userQueryDTO.getUsername());
        }
        if (userQueryDTO.getStatus() != null && !userQueryDTO.getStatus().isEmpty()) {
            queryWrapper.eq(SysUser::getStatus, userQueryDTO.getStatus());
        }
        
        // 执行查询
        var userPage = sysUserService.page(userQueryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<SysUser> result = PageResult.fromMpPage(userPage);
        
        return ResponseResult.success("获取用户列表成功", result);
    }

    /**
     * 新增用户
     * @param sysUser 用户信息
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @PostMapping("/add")
    public ResponseResult<Void> addUser(@RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return ResponseResult.success("新增用户成功");
    }

    /**
     * 更新用户
     * @param sysUser 用户信息
     * @return 操作结果
     */
    @Operation(summary = "更新用户")
    @PutMapping("/update")
    public ResponseResult<Void> updateUser(@RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return ResponseResult.success("更新用户成功");
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return ResponseResult.success("删除用户成功");
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 操作结果
     */
    @Operation(summary = "更新用户状态")
    @PutMapping("/status/{id}")
    public ResponseResult<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        SysUser sysUser = new SysUser();
        sysUser.setUserId(id);
        sysUser.setStatus(status);
        sysUserService.updateById(sysUser);
        return ResponseResult.success("更新用户状态成功");
    }
    
    /**
     * 获取所有角色列表
     * @return 角色列表
     */
    @Operation(summary = "获取所有角色列表")
    @GetMapping("/roles")
    public ResponseResult<List<SysRole>> getAllRoles() {
        List<SysRole> roleList = sysRoleService.list();
        return ResponseResult.success("获取角色列表成功", roleList);
    }
    
    /**
     * 获取用户已关联的角色列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Operation(summary = "获取用户已关联的角色列表")
    @GetMapping("/roles/{userId}")
    public ResponseResult<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<Long> roleIds = sysUserRoleService.getRoleIdsByUserId(userId);
        return ResponseResult.success("获取用户角色成功", roleIds);
    }
    
    /**
     * 保存用户关联的角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @Operation(summary = "保存用户关联的角色")
    @PostMapping("/roles/{userId}")
    public ResponseResult<Void> saveUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserRoleService.assignRolesToUser(userId, roleIds);
        return ResponseResult.success("保存用户角色成功");
    }
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @Operation(summary = "重置用户密码")
    @PutMapping("/reset-password/{userId}")
    public ResponseResult<Void> resetPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        sysUserService.resetPassword(userId, newPassword);
        return ResponseResult.success("重置密码成功");
    }
}
