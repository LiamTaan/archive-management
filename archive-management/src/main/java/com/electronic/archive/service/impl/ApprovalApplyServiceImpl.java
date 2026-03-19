package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.ApprovalApply;
import com.electronic.archive.entity.Notification;
import com.electronic.archive.mapper.ApprovalApplyMapper;
import com.electronic.archive.service.ApprovalApplyService;
import com.electronic.archive.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批申请服务实现类
 */
@Service
public class ApprovalApplyServiceImpl extends ServiceImpl<ApprovalApplyMapper, ApprovalApply> implements ApprovalApplyService {

    @Autowired
    private ApprovalApplyMapper approvalApplyMapper;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    public List<ApprovalApply> getPendingApprovals() {
        // 查询待审批列表，状态为1
        return approvalApplyMapper.selectList(new LambdaQueryWrapper<ApprovalApply>()
                .eq(ApprovalApply::getStatus, 1)
                .orderByDesc(ApprovalApply::getApplyTime));
    }

    @Override
    public List<ApprovalApply> getApprovedApprovals() {
        // 查询已审批列表，状态为2或3
        return approvalApplyMapper.selectList(new LambdaQueryWrapper<ApprovalApply>()
                .in(ApprovalApply::getStatus, 2, 3)
                .orderByDesc(ApprovalApply::getUpdateTime));
    }

    @Override
    public void submitApproval(ApprovalApply approvalApply) {
        // 设置申请时间和状态
        approvalApply.setApplyTime(LocalDateTime.now());
        approvalApply.setStatus(1); // 待审批
        // 临时设置申请人ID为1，实际项目中应该从当前登录用户获取
        approvalApply.setApplicantId(1L);
        approvalApply.setApplicantName("admin");
        approvalApply.setCreateTime(LocalDateTime.now());
        approvalApply.setUpdateTime(LocalDateTime.now());
        approvalApplyMapper.insert(approvalApply);
        
        // 发送审批申请通知
        Notification notification = new Notification();
        notification.setTitle("审批申请通知");
        notification.setContent("您有新的审批申请需要处理");
        notification.setType(2); // 2-审批通知
        notification.setReceiveBy("admin"); // 默认发送给管理员，实际应根据审批流程设置
        notification.setBusinessId(approvalApply.getApplyId());
        notification.setBusinessType("approval_apply");
        notificationService.sendNotification(notification);
    }

    @Override
    public List<ApprovalApply> getMyPendingApprovals(Long userId) {
        // 查询当前用户的待审批列表
        // 根据审批流程配置，查询该用户需要审批的申请
        return approvalApplyMapper.selectList(new LambdaQueryWrapper<ApprovalApply>()
                .eq(ApprovalApply::getStatus, 1)
                // 这里假设审批流程中只有一个审批人，实际项目中应该根据审批流程配置查询
                .eq(ApprovalApply::getApplicantId, userId)
                .orderByDesc(ApprovalApply::getApplyTime));
    }

    @Override
    public List<ApprovalApply> getMyApprovedApprovals(Long userId) {
        // 查询当前用户的已审批列表
        // 根据审批记录，查询该用户已审批的申请
        return approvalApplyMapper.selectList(new LambdaQueryWrapper<ApprovalApply>()
                .in(ApprovalApply::getStatus, 2, 3)
                // 这里假设审批流程中只有一个审批人，实际项目中应该根据审批记录查询
                .eq(ApprovalApply::getApplicantId, userId)
                .orderByDesc(ApprovalApply::getUpdateTime));
    }
    
    @Override
    public long getPendingApprovalsCount() {
        // 查询待审批数量，状态为1
        return approvalApplyMapper.selectCount(new LambdaQueryWrapper<ApprovalApply>()
                .eq(ApprovalApply::getStatus, 1));
    }

    @Override
    public void submitApprovalResult(Long approvalId, String result, String comment, Long approverId, String approverName) {
        // 根据审批ID查询审批申请
        ApprovalApply approvalApply = approvalApplyMapper.selectById(approvalId);
        if (approvalApply != null) {
            // 更新审批状态
            if ("approve".equals(result)) {
                approvalApply.setStatus(2); // 已通过
            } else if ("reject".equals(result)) {
                approvalApply.setStatus(3); // 已拒绝
            }
            // 更新备注
            if (comment != null) {
                approvalApply.setRemark(comment);
            }
            // 更新时间
            approvalApply.setUpdateTime(LocalDateTime.now());
            approvalApplyMapper.updateById(approvalApply);
            
            // 发送审批结果通知给申请人
            Notification notification = new Notification();
            notification.setTitle("审批结果通知");
            notification.setContent("approve".equals(result) ? "您的审批申请已通过" : "您的审批申请已拒绝");
            notification.setType(2); // 2-审批通知
            notification.setReceiveBy(approvalApply.getApplicantName()); // 发送给申请人
            notification.setBusinessId(approvalApply.getApplyId());
            notification.setBusinessType("approval_result");
            notificationService.sendNotification(notification);
        }
    }
}