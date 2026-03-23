package com.electronic.archive.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.ApprovalApply;

/**
 * 审批申请Service
 */
public interface ApprovalApplyService extends IService<ApprovalApply> {
    
    /**
     * 分页查询审批申请列表（带数据权限）
     * @param page 页码
     * @param size 每页大小
     * @param queryWrapper 查询条件
     * @return 审批申请列表
     */
    Page<ApprovalApply> pageWithPermission(int page, int size, LambdaQueryWrapper<ApprovalApply> queryWrapper);
    
    /**
     * 提交挂接申请
     * @param archiveId 档案ID
     * @param applyBy 申请人
     * @return 申请ID
     */
    Long submitApply(Long archiveId, String applyBy);
    
    /**
     * 部门审核
     * @param applyId 申请ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param pass 是否通过
     * @param opinion 审核意见
     * @return 是否成功
     */
    boolean deptAudit(Long applyId, Long operatorId, String operatorName, boolean pass, String opinion);
    
    /**
     * 档案复核
     * @param applyId 申请ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param pass 是否通过
     * @param opinion 复核意见
     * @return 是否成功
     */
    boolean archiveAudit(Long applyId, Long operatorId, String operatorName, boolean pass, String opinion);
    
    /**
     * 最终入库
     * @param applyId 申请ID
     * @param operatorName 操作人名称
     * @return 是否成功
     */
    boolean finalArchive(Long applyId, String operatorName);
}
