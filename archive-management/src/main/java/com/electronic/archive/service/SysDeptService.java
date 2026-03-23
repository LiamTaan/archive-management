package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 获取部门树结构
     * @return 部门树列表
     */
    List<SysDept> getDeptTree();

    /**
     * 获取当前部门及其所有子部门ID列表
     * @param deptId 当前部门ID
     * @return 部门ID列表
     */
    List<Long> getDeptAndChildrenIds(Long deptId);

    /**
     * 获取所有部门ID列表
     * @return 部门ID列表
     */
    List<Long> getAllDeptIds();
    
    /**
     * 获取用户部门的所有上级部门
     * @param userId 用户ID
     * @return 上级部门列表
     */
    List<SysDept> getUpperDeptsByUserId(Long userId);
    
    /**
     * 验证部门是否为另一部门的上级部门
     * @param deptId 当前部门ID
     * @param parentDeptId 待验证的上级部门ID
     * @return 是否为上级部门
     */
    boolean isUpperDept(Long deptId, Long parentDeptId);
    
    /**
     * 根据树路径获取所有上级部门ID
     * @param treePath 树路径
     * @return 上级部门ID列表
     */
    List<Long> getUpperDeptIdsByTreePath(String treePath);
}