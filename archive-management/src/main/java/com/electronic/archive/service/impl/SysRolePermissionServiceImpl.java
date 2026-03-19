package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysRolePermission;
import com.electronic.archive.mapper.SysRolePermissionMapper;
import com.electronic.archive.service.SysRolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色权限关联Service实现
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return sysRolePermissionMapper.getPermissionIdsByRoleId(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveRolePermissions(Long roleId, List<Long> permissionIds) {
        // 删除角色原有的所有权限关联
        sysRolePermissionMapper.deleteByRoleId(roleId);
        
        // 如果权限ID列表不为空，则添加新的关联关系
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissions.add(rolePermission);
            }
            // 批量保存
            return saveBatch(rolePermissions);
        }
        
        return true;
    }
}