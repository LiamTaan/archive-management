package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案组合实体类
 */
@Data
@TableName("archive_combination")
public class ArchiveCombination {
    /**
     * 组合ID
     */
    @TableId(value = "combination_id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long combinationId;
    
    /**
     * 组合名称
     */
    private String combinationName;

    /**
     * 组合类型
     */
    private String combinationType;

    /**
     * 组合状态(0-未挂接, 1-已挂接, 2-挂接失败)
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 所属部门ID
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
    
    /**
     * 为了兼容前端，添加id字段，使用@TableField注解指定不映射到数据库字段
     */
    @TableField(exist = false)
    private Long id;
    
    /**
     * 重写setter方法，确保id和combinationId保持一致
     */
    public void setCombinationId(Long combinationId) {
        this.combinationId = combinationId;
        this.id = combinationId;
    }
    
    /**
     * 重写setter方法，确保id和combinationId保持一致
     */
    public void setId(Long id) {
        this.id = id;
        this.combinationId = id;
    }
    
    /**
     * 确保getter方法返回正确的值
     */
    public Long getId() {
        return combinationId;
    }
}