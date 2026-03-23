package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批节点表
 */
@Data
@TableName("approval_node")
public class ApprovalNode {
    
    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    private Long nodeId;
    
    /**
     * 流程配置ID
     */
    private Long configId;
    
    /**
     * 节点名称
     */
    private String nodeName;
    
    /**
     * 节点类型（1-部门审核，2-档案复核）
     */
    private Integer nodeType;
    
    /**
     * 节点顺序
     */
    private Integer nodeOrder;
    
    /**
     * 所需角色
     */
    private String requiredRole;
    
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
