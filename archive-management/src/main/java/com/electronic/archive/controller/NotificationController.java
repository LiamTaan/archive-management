package com.electronic.archive.controller;

import com.electronic.archive.entity.Notification;
import com.electronic.archive.service.NotificationService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * 根据接收人获取通知列表
     * @param receiverId 接收人ID
     * @param type 通知类型（可选）
     * @param status 通知状态（可选）
     * @return 通知列表
     */
    @Operation(summary = "根据接收人获取通知列表")
    @GetMapping("/list")
    public ResponseResult<List<Notification>> getNotificationsByReceiver(
            @RequestParam(required = false) String receiveBy) {
        List<Notification> notifications = notificationService.listByReceiveBy(receiveBy);
        return ResponseResult.success("获取通知列表成功", notifications);
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
