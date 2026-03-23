package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统部门实体类
 */
@Data
@ToString(exclude = "children")
@TableName("sys_dept")
public class SysDept {
    /**
     * 部门ID
     */
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    /**
     * 父部门ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 部门名称
     */
    @TableField("dept_name")
    private String deptName;

    /**
     * 部门编码
     */
    @TableField("dept_code")
    private String deptCode;

    /**
     * 部门描述
     */
    @TableField("dept_desc")
    private String deptDesc;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 树路径，用于快速查询上级部门，格式如：/1/2/3/
     */
    @TableField("tree_path")
    private String treePath;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    /**
     * 子部门列表（非数据库字段，用于构建树结构）
     */
    @TableField(exist = false)
    private List<SysDept> children;
}