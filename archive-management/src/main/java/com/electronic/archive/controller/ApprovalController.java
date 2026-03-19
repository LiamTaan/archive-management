package com.electronic.archive.controller;

import com.electronic.archive.entity.ApprovalApply;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.ApprovalApplyService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批管理控制器
 */
@Tag(name = "审批管理")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalApplyService approvalApplyService;
    
    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取待审批列表
     * @return 待审批列表
     */
    @Operation(summary = "获取待审批列表")
    @GetMapping("/pending")
    public ResponseResult<List<ApprovalApply>> getPendingApprovals() {
        List<ApprovalApply> pendingApprovals = approvalApplyService.getPendingApprovals();
        return ResponseResult.success("获取待审批列表成功", pendingApprovals);
    }

    /**
     * 获取已审批列表
     * @return 已审批列表
     */
    @Operation(summary = "获取已审批列表")
    @GetMapping("/approved")
    public ResponseResult<List<ApprovalApply>> getApprovedApprovals() {
        List<ApprovalApply> approvedApprovals = approvalApplyService.getApprovedApprovals();
        return ResponseResult.success("获取已审批列表成功", approvedApprovals);
    }

    /**
     * 获取当前用户的待办审批列表
     * @return 待办审批列表
     */
    @Operation(summary = "获取当前用户的待办审批列表")
    @GetMapping("/my/pending")
    public ResponseResult<List<ApprovalApply>> getMyPendingApprovals() {
        // 从SecurityContext中获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.fail("用户未登录");
        }
        String username = authentication.getName();
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }
        List<ApprovalApply> pendingApprovals = approvalApplyService.getMyPendingApprovals(user.getUserId());
        return ResponseResult.success("获取当前用户待办审批列表成功", pendingApprovals);
    }

    /**
     * 获取当前用户的已办审批列表
     * @return 已办审批列表
     */
    @Operation(summary = "获取当前用户的已办审批列表")
    @GetMapping("/my/approved")
    public ResponseResult<List<ApprovalApply>> getMyApprovedApprovals() {
        // 从SecurityContext中获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseResult.fail("用户未登录");
        }
        String username = authentication.getName();
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }
        List<ApprovalApply> approvedApprovals = approvalApplyService.getMyApprovedApprovals(user.getUserId());
        return ResponseResult.success("获取当前用户已办审批列表成功", approvedApprovals);
    }

    /**
     * 提交审批申请
     * @param approvalApply 审批申请信息
     * @return 操作结果
     */
    @Operation(summary = "提交审批申请")
    @PostMapping("/submit")
    public ResponseResult<Void> submitApproval(@RequestBody ApprovalApply approvalApply) {
        approvalApplyService.submitApproval(approvalApply);
        return ResponseResult.success("提交审批申请成功");
    }

    /**
     * 提交审批结果
     * @param approvalId 审批ID
     * @param requestBody 请求体，包含审批结果和意见
     * @return 操作结果
     */
    @Operation(summary = "提交审批结果")
    @PostMapping("/result/{approvalId}")
    public ResponseResult<Void> submitApprovalResult(
            @PathVariable Long approvalId,
            @RequestBody java.util.Map<String, Object> requestBody) {
        String result = (String) requestBody.get("result");
        String comment = (String) requestBody.get("comment");
        // 简化实现，实际应该从SecurityContext中获取当前用户ID和名称
        Long approverId = 1L;
        String approverName = "admin";
        approvalApplyService.submitApprovalResult(approvalId, result, comment, approverId, approverName);
        return ResponseResult.success("提交审批结果成功");
    }
}