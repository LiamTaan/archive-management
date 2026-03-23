package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electronic.archive.dto.NotificationQueryDTO;
import com.electronic.archive.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    /**
     * 发送通知
     * @param notification 通知实体
     * @return 是否发送成功
     */
    boolean sendNotification(Notification notification);
    
    /**
     * 批量发送通知
     * @param notifications 通知列表
     * @return 是否发送成功
     */
    boolean sendBatchNotification(List<Notification> notifications);
    
    /**
     * 根据ID获取通知
     * @param id 通知ID
     * @return 通知实体
     */
    Notification getById(Long id);
    
    /**
     * 根据接收人ID获取通知列表
     * @param receiverId 接收人ID
     * @return 通知列表
     */
    List<Notification> listByReceiverId(Long receiverId);
    
    /**
     * 分页查询通知列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Notification> queryNotificationByPage(NotificationQueryDTO queryDTO);
    
    /**
     * 更新通知状态为已读
     * @param id 通知ID
     * @return 是否更新成功
     */
    boolean markAsRead(Long id);
    
    /**
     * 批量更新通知状态为已读
     * @param ids 通知ID列表
     * @return 是否更新成功
     */
    boolean markBatchAsRead(List<Long> ids);
    
    /**
     * 删除通知
     * @param id 通知ID
     * @return 是否删除成功
     */
    boolean removeById(Long id);
    
    /**
     * 更新通知状态为已处理
     * @param id 通知ID
     * @return 是否更新成功
     */
    boolean markAsProcessed(Long id);
}