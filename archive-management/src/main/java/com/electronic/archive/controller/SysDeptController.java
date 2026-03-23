package com.electronic.archive.controller;

import com.electronic.archive.entity.SysDept;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.SysDeptService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;
    
    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取部门树结构
     * @return 部门树列表
     */
    @Operation(summary = "获取部门树结构")
    @GetMapping("/tree")
    public ResponseResult<List<SysDept>> getDeptTree() {
        List<SysDept> deptTree = sysDeptService.getDeptTree();
        return ResponseResult.success("获取部门树结构成功", deptTree);
    }

    /**
     * 新增部门
     * @param dept 部门信息
     * @return 操作结果
     */
    @Operation(summary = "新增部门")
    @PostMapping("/add")
    public ResponseResult<Void> addDept(@RequestBody SysDept dept) {
        sysDeptService.save(dept);
        return ResponseResult.success("新增部门成功");
    }

    /**
     * 更新部门
     * @param dept 部门信息
     * @return 操作结果
     */
    @Operation(summary = "更新部门")
    @PutMapping("/update")
    public ResponseResult<Void> updateDept(@RequestBody SysDept dept) {
        sysDeptService.updateById(dept);
        return ResponseResult.success("更新部门成功");
    }

    /**
     * 删除部门
     * @param deptId 部门ID
     * @return 操作结果
     */
    @Operation(summary = "删除部门")
    @DeleteMapping("/{deptId}")
    public ResponseResult<Void> deleteDept(@PathVariable Long deptId) {
        sysDeptService.removeById(deptId);
        return ResponseResult.success("删除部门成功");
    }


}