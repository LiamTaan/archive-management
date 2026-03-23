package com.electronic.archive.controller;

import com.electronic.archive.dto.NotificationQueryDTO;
import com.electronic.archive.entity.Notification;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.NotificationService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询通知列表
     * @param queryDTO 查询条件
     * @return 通知列表
     */
    @Operation(summary = "分页查询通知列表")
    @PostMapping("/list")
    public ResponseResult<PageResult<Notification>> getNotificationsByReceiver(@RequestBody NotificationQueryDTO queryDTO) {
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SysUser user = sysUserService.getByUsername(username);
            
            // 将当前登录用户的ID设置为接收人ID
            queryDTO.setReceiverId(user.getUserId());
            
            var pageResult = notificationService.queryNotificationByPage(queryDTO);
            
            // 转换为统一的分页响应格式
            PageResult<Notification> result = PageResult.fromMpPage(pageResult);
            
            return ResponseResult.success("获取通知列表成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据接收人ID获取通知列表（兼容旧接口）
     * @param queryDTO 查询条件
     * @return 通知列表
     */
    @Operation(summary = "根据接收人ID获取通知列表（兼容旧接口）")
    @GetMapping("/list")
    public ResponseResult<PageResult<Notification>> getNotificationsByReceiverOld(NotificationQueryDTO queryDTO) {
        try {
            // 获取当前登录用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            SysUser user = sysUserService.getByUsername(username);
            
            // 将当前登录用户的ID设置为接收人ID
            queryDTO.setReceiverId(user.getUserId());
            
            var pageResult = notificationService.queryNotificationByPage(queryDTO);
            
            // 转换为统一的分页响应格式
            PageResult<Notification> result = PageResult.fromMpPage(pageResult);
            
            return ResponseResult.success("获取通知列表成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取通知详情
     * @param id 通知ID
     * @return 通知详情
     */
    @Operation(summary = "根据ID获取通知详情")
    @GetMapping("/{id}")
    public ResponseResult<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getById(id);
        return ResponseResult.success("获取通知详情成功", notification);
    }

    /**
     * 标记通知为已读
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public ResponseResult<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseResult.success("标记已读成功");
    }

    /**
     * 批量标记通知为已读
     * @param ids 通知ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量标记通知为已读")
    @PutMapping("/batch/read")
    public ResponseResult<Void> batchMarkAsRead(@RequestBody List<Long> ids) {
        notificationService.markBatchAsRead(ids);
        return ResponseResult.success("批量标记已读成功");
    }

    /**
     * 标记通知为已处理
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "标记通知为已处理")
    @PutMapping("/{id}/process")
    public ResponseResult<Void> markAsProcessed(@PathVariable Long id) {
        notificationService.markAsProcessed(id);
        return ResponseResult.success("标记已处理成功");
    }

    /**
     * 删除通知
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteNotification(@PathVariable Long id) {
        notificationService.removeById(id);
        return ResponseResult.success("删除通知成功");
    }

    /**
     * 批量删除通知
     * @param ids 通知ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除通知")
    @DeleteMapping("/batch")
    public ResponseResult<Void> batchDeleteNotifications(@RequestBody List<Long> ids) {
        // 由于Service中没有批量删除方法，这里使用循环删除
        for (Long id : ids) {
            notificationService.removeById(id);
        }
        return ResponseResult.success("批量删除通知成功");
    }
}
