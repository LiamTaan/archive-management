package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批历史表
 */
@Data
@TableName("approval_history")
public class ApprovalHistory {
    
    /**
     * 历史ID
     */
    @TableId(type = IdType.AUTO)
    private Long historyId;
    
    /**
     * 申请ID
     */
    private Long applyId;
    
    /**
     * 节点ID
     */
    private Long nodeId;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人名称
     */
    private String operatorName;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 操作类型（1-通过，2-驳回）
     */
    private Integer operationType;
    
    /**
     * 操作意见
     */
    private String operationOpinion;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
