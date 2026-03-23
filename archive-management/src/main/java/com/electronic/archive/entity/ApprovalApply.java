package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批申请表
 */
@Data
@TableName("approval_apply")
public class ApprovalApply {
    
    /**
     * 申请ID
     */
    @TableId(type = IdType.AUTO)
    private Long applyId;
    
    /**
     * 档案ID
     */
    private Long archiveId;
    
    /**
     * 所属部门ID
     */
    private Long deptId;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 申请类型（1-挂接申请）
     */
    private Integer applyType;
    
    /**
     * 申请状态（0-待审批，1-部门审核通过，2-档案复核通过，3-已入库，4-驳回）
     */
    private Integer applyStatus;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 申请人
     */
    private String applyBy;
    
    /**
     * 部门负责人ID
     */
    private Long deptLeaderId;
    
    /**
     * 部门审核时间
     */
    private LocalDateTime deptAuditTime;
    
    /**
     * 部门审核意见
     */
    private String deptAuditOpinion;
    
    /**
     * 档案管理员ID
     */
    private Long archiveAdminId;
    
    /**
     * 档案复核时间
     */
    private LocalDateTime archiveAuditTime;
    
    /**
     * 档案复核意见
     */
    private String archiveAuditOpinion;
    
    /**
     * 最终确认时间
     */
    private LocalDateTime finalConfirmTime;
    
    /**
     * 最终确认人
     */
    private String finalConfirmBy;
    
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
