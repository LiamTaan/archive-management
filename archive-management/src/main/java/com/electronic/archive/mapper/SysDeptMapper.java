package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.SysDept;

import java.util.List;

/**
 * 系统部门Mapper
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {
    
    /**
     * 查询部门列表
     * @return 部门列表
     */
    List<SysDept> selectDeptList();
    
    /**
     * 查询部门树结构
     * @return 部门树
     */
    List<SysDept> selectDeptTree();
    
    /**
     * 根据父部门ID查询子部门列表
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<SysDept> selectChildrenDeptById(Long parentId);
}
