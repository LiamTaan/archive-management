package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批流程配置表
 */
@Data
@TableName("approval_process_config")
public class ApprovalProcessConfig {
    
    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long configId;
    
    /**
     * 流程名称
     */
    private String processName;
    
    /**
     * 流程类型（1-挂接申请）
     */
    private Integer processType;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
