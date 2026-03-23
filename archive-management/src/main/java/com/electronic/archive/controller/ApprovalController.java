package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.entity.ApprovalHistory;
import com.electronic.archive.entity.ApprovalApply;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.service.ApprovalApplyService;
import com.electronic.archive.service.ApprovalHistoryService;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private ApprovalHistoryService approvalHistoryService;
    
    @Autowired
    private ArchiveInfoService archiveInfoService;
    
    /**
     * 提交挂接申请
     * @param archiveId 档案ID
     * @return 操作结果
     */
    @Operation(summary = "提交挂接申请")
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ARCHIVE_OPER')")
    public ResponseResult<Map<String, Object>> submitApply(@RequestParam Long archiveId) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 提交申请
            Long applyId = approvalApplyService.submitApply(archiveId, username);
            
            return ResponseResult.success("提交挂接申请成功", Map.of("applyId", applyId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("提交挂接申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审批列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param archiveId 档案ID
     * @param status 审批状态
     * @return 审批列表
     */
    @Operation(summary = "获取审批列表")
    @GetMapping("/list")
    public ResponseResult<Page<ApprovalApply>> getApprovalList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "档案ID")
            @RequestParam(required = false) Long archiveId,
            @Parameter(description = "审批状态（0-待审批，1-部门审核通过，2-档案复核通过，3-已入库，4-驳回）")
            @RequestParam(required = false) Integer status) {
        try {
            LambdaQueryWrapper<ApprovalApply> queryWrapper = new LambdaQueryWrapper<>();
            
            // 按档案ID筛选
            if (archiveId != null) {
                queryWrapper.eq(ApprovalApply::getArchiveId, archiveId);
            }
            
            // 按状态筛选
            if (status != null) {
                queryWrapper.eq(ApprovalApply::getApplyStatus, status);
            }
            
            // 按申请时间倒序排序
            queryWrapper.orderByDesc(ApprovalApply::getApplyTime);
            
            // 使用带数据权限的分页查询
            Page<ApprovalApply> result = approvalApplyService.pageWithPermission(page, size, queryWrapper);
            return ResponseResult.success("获取审批列表成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("获取审批列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审批详情
     * @param applyId 申请ID
     * @return 审批详情
     */
    @Operation(summary = "获取审批详情")
    @GetMapping("/{applyId}")
    public ResponseResult<Map<String, Object>> getApprovalDetail(@PathVariable Long applyId) {
        try {
            // 获取审批申请详情
            ApprovalApply approvalApply = approvalApplyService.getById(applyId);
            if (approvalApply == null) {
                return ResponseResult.fail("审批申请不存在");
            }
            
            // 获取审批历史
            List<ApprovalHistory> historyList = approvalHistoryService.list(
                    new LambdaQueryWrapper<ApprovalHistory>()
                            .eq(ApprovalHistory::getApplyId, applyId)
                            .orderByAsc(ApprovalHistory::getOperationTime)
            );
            
            // 获取档案信息
            ArchiveInfo archiveInfo = archiveInfoService.getById(approvalApply.getArchiveId());
            
            // 组装返回结果
            Map<String, Object> result = Map.of(
                    "apply", approvalApply,
                    "history", historyList,
                    "archiveInfo", archiveInfo
            );
            
            return ResponseResult.success("获取审批详情成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("获取审批详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 部门审核
     * @param data 审核参数
     * @return 操作结果
     */
    @Operation(summary = "部门审核")
    @PostMapping("/dept-audit")
    @PreAuthorize("hasAnyRole('DEPT_LEADER')")
    public ResponseResult<Void> deptAudit(@RequestBody Map<String, Object> data) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 从Map中获取参数
            Long applyId = Long.valueOf(String.valueOf(data.get("applyId")));
            boolean pass = Boolean.parseBoolean(String.valueOf(data.get("pass")));
            String opinion = String.valueOf(data.get("opinion"));
            Long operatorId = Long.valueOf(String.valueOf(data.get("operatorId")));

            // 执行部门审核
            boolean success = approvalApplyService.deptAudit(applyId, operatorId, username, pass, opinion);
            
            if (success) {
                return ResponseResult.success("部门审核成功");
            } else {
                return ResponseResult.fail("部门审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("部门审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 档案复核
     * @param data 复核参数
     * @return 操作结果
     */
    @Operation(summary = "档案复核")
    @PostMapping("/archive-audit")
    @PreAuthorize("hasAnyRole('ARCHIVE_ADMIN')")
    public ResponseResult<Void> archiveAudit(@RequestBody Map<String, Object> data) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // 从Map中获取参数
            Long applyId = Long.valueOf(String.valueOf(data.get("applyId")));
            boolean pass = Boolean.parseBoolean(String.valueOf(data.get("pass")));
            String opinion = String.valueOf(data.get("opinion"));
            Long operatorId = Long.valueOf(String.valueOf(data.get("operatorId")));
            
            // 执行档案复核
            boolean success = approvalApplyService.archiveAudit(applyId, operatorId, username, pass, opinion);
            
            if (success) {
                return ResponseResult.success("档案复核成功");
            } else {
                return ResponseResult.fail("档案复核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("档案复核失败: " + e.getMessage());
        }
    }
    
    /**
     * 最终入库
     * @return 操作结果
     */
    @Operation(summary = "最终入库")
    @PostMapping("/final-archive")
    @PreAuthorize("hasAnyRole('ARCHIVE_ADMIN')")
    public ResponseResult<Void> finalArchive(@RequestBody Map<String, Object> data) {
        try {
            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // 执行最终入库
            boolean success = approvalApplyService.finalArchive(Long.valueOf(String.valueOf(data.get("applyId"))), username);
            
            if (success) {
                return ResponseResult.success("最终入库成功");
            } else {
                return ResponseResult.fail("最终入库失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("最终入库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审批历史记录
     * @param applyId 申请ID
     * @return 审批历史记录
     */
    @Operation(summary = "获取审批历史记录")
    @GetMapping("/{applyId}/history")
    public ResponseResult<List<ApprovalHistory>> getApprovalHistory(@PathVariable Long applyId) {
        try {
            List<ApprovalHistory> historyList = approvalHistoryService.list(
                    new LambdaQueryWrapper<ApprovalHistory>()
                            .eq(ApprovalHistory::getApplyId, applyId)
                            .orderByAsc(ApprovalHistory::getOperationTime)
            );
            
            return ResponseResult.success("获取审批历史记录成功", historyList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("获取审批历史记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取审批统计信息
     * @return 审批统计信息
     */
    @Operation(summary = "获取审批统计信息")
    @GetMapping("/statistics")
    public ResponseResult<Map<String, Object>> getApprovalStatistics() {
        try {
            // 统计各状态的申请数量
            long total = approvalApplyService.count();
            long pending = approvalApplyService.count(new LambdaQueryWrapper<ApprovalApply>().eq(ApprovalApply::getApplyStatus, 0));
            long deptApproved = approvalApplyService.count(new LambdaQueryWrapper<ApprovalApply>().eq(ApprovalApply::getApplyStatus, 1));
            long archiveApproved = approvalApplyService.count(new LambdaQueryWrapper<ApprovalApply>().eq(ApprovalApply::getApplyStatus, 2));
            long archived = approvalApplyService.count(new LambdaQueryWrapper<ApprovalApply>().eq(ApprovalApply::getApplyStatus, 3));
            long rejected = approvalApplyService.count(new LambdaQueryWrapper<ApprovalApply>().eq(ApprovalApply::getApplyStatus, 4));
            
            Map<String, Object> statistics = Map.of(
                    "total", total,
                    "pending", pending,
                    "deptApproved", deptApproved,
                    "archiveApproved", archiveApproved,
                    "archived", archived,
                    "rejected", rejected
            );
            
            return ResponseResult.success("获取审批统计信息成功", statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail("获取审批统计信息失败: " + e.getMessage());
        }
    }
}
