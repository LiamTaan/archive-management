package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批申请表
 */
@Data
@TableName("approval_apply")
public class ApprovalApply {
    /**
     * 申请表ID
     */
    @TableId(type = IdType.AUTO)
    private Long applyId;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人名称
     */
    private String applicantName;
    
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 操作类型(1-修改挂接，2-解除挂接)
     */
    private Integer operationType;
    
    /**
     * 操作对象ID(档案ID)
     */
    private Long objectId;
    
    /**
     * 操作内容(JSON格式)
     */
    private String operationContent;
    
    /**
     * 审批状态(1-待审批，2-已通过，3-已拒绝)
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}