package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.ApprovalApply;

import java.util.List;

/**
 * 审批申请服务接口
 */
public interface ApprovalApplyService extends IService<ApprovalApply> {
    /**
     * 获取待审批列表
     * @return 待审批列表
     */
    List<ApprovalApply> getPendingApprovals();

    /**
     * 获取已审批列表
     * @return 已审批列表
     */
    List<ApprovalApply> getApprovedApprovals();

    /**
     * 获取当前用户的待办审批列表
     * @param userId 当前用户ID
     * @return 待办审批列表
     */
    List<ApprovalApply> getMyPendingApprovals(Long userId);

    /**
     * 获取当前用户的已办审批列表
     * @param userId 当前用户ID
     * @return 已办审批列表
     */
    List<ApprovalApply> getMyApprovedApprovals(Long userId);
    
    /**
     * 获取待审批数量
     * @return 待审批数量
     */
    long getPendingApprovalsCount();

    /**
     * 提交审批申请
     * @param approvalApply 审批申请信息
     */
    void submitApproval(ApprovalApply approvalApply);

    /**
     * 提交审批结果
     * @param approvalId 审批ID
     * @param result 审批结果
     * @param comment 审批意见
     * @param approverId 审批人ID
     * @param approverName 审批人名称
     */
    void submitApprovalResult(Long approvalId, String result, String comment, Long approverId, String approverName);
}