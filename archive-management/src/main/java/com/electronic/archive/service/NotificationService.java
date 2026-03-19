package com.electronic.archive.service;

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
     * 根据接收人获取通知列表
     * @param receiveBy 接收人
     * @return 通知列表
     */
    List<Notification> listByReceiveBy(String receiveBy);
    
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
}