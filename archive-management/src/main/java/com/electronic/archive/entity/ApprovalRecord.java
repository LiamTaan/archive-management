package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批记录表
 */
@Data
@TableName("approval_record")
public class ApprovalRecord {
    /**
     * 记录表ID
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;
    
    /**
     * 申请表ID
     */
    private Long applyId;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人名称
     */
    private String approverName;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审批结果(1-通过，2-拒绝)
     */
    private Integer result;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}