package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysRole;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.entity.SysUserRole;
import com.electronic.archive.mapper.SysRoleMapper;
import com.electronic.archive.mapper.SysUserRoleMapper;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色关联ServiceImpl
 */
@Service
@Slf4j
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    
    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.getRoleIdsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        // 删除用户原有的所有角色
        sysUserRoleMapper.deleteByUserId(userId);
        
        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoles.add(userRole);
            }
            return saveBatch(userRoles);
        }
        
        return true;
    }

    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        // 获取用户的角色ID列表
        List<Long> roleIds = getRoleIdsByUserId(userId);
        
        // 获取角色名称列表
        if (roleIds != null && !roleIds.isEmpty()) {
            LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysRole::getRoleId, roleIds);
            List<SysRole> roles = sysRoleMapper.selectList(queryWrapper);
            List<String> roleNames = new ArrayList<>();
            for (SysRole role : roles) {
                roleNames.add(role.getRoleCode());
            }
            return roleNames;
        }
        
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysRole::getRoleId, roleIds);
            List<SysRole> roles = sysRoleMapper.selectList(queryWrapper);
            List<String> roleCodes = new ArrayList<>();
            for (SysRole role : roles) {
                roleCodes.add(role.getRoleCode());
            }
            return roleCodes;
        }
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getUsersByRoleCode(String roleCode) {
        List<String> usernames = new ArrayList<>();
        try {
            // 1. 根据角色编码查询角色信息
            LambdaQueryWrapper<SysRole> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(SysRole::getRoleCode, roleCode);
            SysRole role = sysRoleMapper.selectOne(roleQueryWrapper);
            
            if (role != null) {
                // 2. 根据角色ID查询用户ID列表
                List<Long> userIds = sysUserRoleMapper.getUserIdsByRoleId(role.getRoleId());
                
                if (userIds != null && !userIds.isEmpty()) {
                    // 3. 根据用户ID列表查询用户信息
                    LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                    userQueryWrapper.in(SysUser::getUserId, userIds);
                    List<SysUser> users = sysUserService.list(userQueryWrapper);
                    
                    // 4. 提取用户名列表
                    for (SysUser user : users) {
                        usernames.add(user.getUsername());
                    }
                }
            }
        } catch (Exception e) {
            log.error("根据角色编码获取用户列表失败", e);
        }
        return usernames;
    }
}
