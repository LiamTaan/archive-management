package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SysDept;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.mapper.SysDeptMapper;
import com.electronic.archive.service.SysDeptService;
import com.electronic.archive.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {
    
    @Autowired
    @org.springframework.context.annotation.Lazy
    private SysUserService sysUserService;
    

    /**
     * 获取部门树结构
     * @return 部门树列表
     */
    @Override
    public List<SysDept> getDeptTree() {
        // 查询所有启用的部门
        List<SysDept> allDepts = this.list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSort));
        
        // 构建部门树
        return buildDeptTree(allDepts, 0L);
    }

    /**
     * 获取当前部门及其所有子部门ID列表
     * @param deptId 当前部门ID
     * @return 部门ID列表
     */
    @Override
    public List<Long> getDeptAndChildrenIds(Long deptId) {
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(deptId);
        
        // 查询当前部门及其子部门
        List<SysDept> allDepts = this.list();
        getChildrenIds(allDepts, deptId, deptIds);
        
        return deptIds;
    }

    /**
     * 获取所有部门ID列表
     * @return 部门ID列表
     */
    @Override
    public List<Long> getAllDeptIds() {
        return this.list().stream()
                .map(SysDept::getDeptId)
                .collect(Collectors.toList());
    }

    /**
     * 构建部门树
     * @param depts 部门列表
     * @param parentId 父部门ID
     * @return 部门树列表
     */
    private List<SysDept> buildDeptTree(List<SysDept> depts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        
        for (SysDept dept : depts) {
            if (parentId.equals(dept.getParentId())) {
                List<SysDept> children = buildDeptTree(depts, dept.getDeptId());
                dept.setChildren(children);
                tree.add(dept);
            }
        }
        
        return tree;
    }

    /**
     * 递归获取子部门ID
     * @param depts 部门列表
     * @param parentId 父部门ID
     * @param deptIds 部门ID列表
     */
    private void getChildrenIds(List<SysDept> depts, Long parentId, List<Long> deptIds) {
        for (SysDept dept : depts) {
            // 添加null检查，避免NullPointerException
            if ((parentId == null && dept.getParentId() == null) || 
                (parentId != null && parentId.equals(dept.getParentId()))) {
                deptIds.add(dept.getDeptId());
                getChildrenIds(depts, dept.getDeptId(), deptIds);
            }
        }
    }

    /**
     * 获取用户部门的所有上级部门
     * @param userId 用户ID
     * @return 上级部门列表
     */
    @Override
    public List<SysDept> getUpperDeptsByUserId(Long userId) {
        // 获取用户信息
        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getDeptId() == null) {
            return new ArrayList<>();
        }
        
        // 获取用户部门
        SysDept userDept = this.getById(user.getDeptId());
        if (userDept == null || userDept.getTreePath() == null) {
            return new ArrayList<>();
        }
        
        // 获取所有上级部门ID
        List<Long> upperDeptIds = getUpperDeptIdsByTreePath(userDept.getTreePath());
        if (upperDeptIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 移除当前部门ID，只保留真正的上级部门
        upperDeptIds.remove(user.getDeptId());
        if (upperDeptIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询上级部门信息
        return this.list(new LambdaQueryWrapper<SysDept>()
                .in(SysDept::getDeptId, upperDeptIds)
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSort));
    }

    /**
     * 验证部门是否为另一部门的上级部门
     * @param deptId 当前部门ID
     * @param parentDeptId 待验证的上级部门ID
     * @return 是否为上级部门
     */
    @Override
    public boolean isUpperDept(Long deptId, Long parentDeptId) {
        // 获取当前部门
        SysDept dept = this.getById(deptId);
        if (dept == null || dept.getTreePath() == null) {
            return false;
        }
        
        // 检查树路径中是否包含上级部门ID
        return dept.getTreePath().contains("/" + parentDeptId + "/");
    }

    /**
     * 根据树路径获取所有上级部门ID
     * @param treePath 树路径
     * @return 上级部门ID列表
     */
    @Override
    public List<Long> getUpperDeptIdsByTreePath(String treePath) {
        List<Long> deptIds = new ArrayList<>();
        
        if (treePath == null || !treePath.startsWith("/") || !treePath.endsWith("/")) {
            return deptIds;
        }
        
        // 解析树路径，提取部门ID
        String[] parts = treePath.split("/");
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    Long deptId = Long.parseLong(part);
                    deptIds.add(deptId);
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        
        return deptIds;
    }

    /**
     * 保存部门时自动设置树路径
     * @param dept 部门信息
     * @return 是否保存成功
     */
    @Override
    public boolean save(SysDept dept) {
        // 保存部门
        boolean result = super.save(dept);
        
        // 保存成功后设置树路径
        if (result) {
            // 重新获取保存后的部门，确保有ID
            SysDept savedDept = this.getById(dept.getDeptId());
            if (savedDept != null) {
                // 设置树路径
                setTreePath(savedDept);
                // 更新树路径
                super.updateById(savedDept);
            }
        }
        
        return result;
    }

    /**
     * 更新部门时自动更新树路径
     * @param dept 部门信息
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysDept dept) {
        // 获取原始部门信息
        SysDept originalDept = this.getById(dept.getDeptId());
        if (originalDept == null) {
            return false;
        }
        
        // 如果父部门发生变化，需要更新树路径
        if (!originalDept.getParentId().equals(dept.getParentId())) {
            // 更新当前部门的树路径
            setTreePath(dept);
            
            // 更新子部门的树路径
            updateChildrenTreePath(dept);
        }
        
        // 更新部门
        return super.updateById(dept);
    }

    /**
     * 设置部门树路径
     * @param dept 部门信息
     */
    private void setTreePath(SysDept dept) {
        if (dept == null) {
            return;
        }
        
        // 如果是根部门
        if (dept.getParentId() == null || dept.getParentId() == 0) {
            dept.setTreePath("/" + dept.getDeptId() + "/");
        } else {
            // 获取父部门
            SysDept parentDept = this.getById(dept.getParentId());
            if (parentDept != null) {
                // 父部门的树路径 + 当前部门ID
                String parentTreePath = parentDept.getTreePath() != null ? parentDept.getTreePath() : "/";
                dept.setTreePath(parentTreePath + dept.getDeptId() + "/");
            }
        }
    }

    /**
     * 更新子部门的树路径
     * @param dept 父部门信息
     */
    private void updateChildrenTreePath(SysDept dept) {
        if (dept == null || dept.getTreePath() == null) {
            return;
        }
        
        // 获取所有子部门
        List<SysDept> allDepts = this.list();
        List<SysDept> childrenDepts = new ArrayList<>();
        getChildrenDepts(allDepts, dept.getDeptId(), childrenDepts);
        
        // 更新子部门的树路径
        for (SysDept childDept : childrenDepts) {
            // 设置新的树路径
            setTreePath(childDept);
            // 更新子部门
            super.updateById(childDept);
        }
    }

    /**
     * 递归获取子部门
     * @param allDepts 所有部门列表
     * @param parentId 父部门ID
     * @param childrenDepts 子部门列表
     */
    private void getChildrenDepts(List<SysDept> allDepts, Long parentId, List<SysDept> childrenDepts) {
        for (SysDept dept : allDepts) {
            if (parentId.equals(dept.getParentId())) {
                childrenDepts.add(dept);
                getChildrenDepts(allDepts, dept.getDeptId(), childrenDepts);
            }
        }
    }
}