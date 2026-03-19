package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.Notification;
import com.electronic.archive.mapper.NotificationMapper;
import com.electronic.archive.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现类
 */
@Service
@Slf4j
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Override
    public boolean sendNotification(Notification notification) {
        try {
            // 设置默认值
            if (notification.getCreateTime() == null) {
                notification.setCreateTime(LocalDateTime.now());
            }
            if (notification.getStatus() == null) {
                notification.setStatus(0); // 默认未读
            }
            if (notification.getSendBy() == null) {
                notification.setSendBy("system"); // 默认系统发送
            }
            
            boolean result = baseMapper.insert(notification) > 0;
            if (result) {
                log.info("发送通知成功，接收人：{}，通知类型：{}", notification.getReceiveBy(), notification.getType());
            } else {
                log.error("发送通知失败，接收人：{}，通知类型：{}", notification.getReceiveBy(), notification.getType());
            }
            return result;
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return false;
        }
    }
    
    @Override
    public boolean sendBatchNotification(List<Notification> notifications) {
        try {
            // 设置默认值
            LocalDateTime now = LocalDateTime.now();
            for (Notification notification : notifications) {
                if (notification.getCreateTime() == null) {
                    notification.setCreateTime(now);
                }
                if (notification.getStatus() == null) {
                    notification.setStatus(0); // 默认未读
                }
                if (notification.getSendBy() == null) {
                    notification.setSendBy("system"); // 默认系统发送
                }
            }
            
            boolean result = true;
            for (Notification notification : notifications) {
                if (baseMapper.insert(notification) <= 0) {
                    result = false;
                    log.error("批量发送通知失败，接收人：{}，通知类型：{}", notification.getReceiveBy(), notification.getType());
                } else {
                    log.info("批量发送通知成功，接收人：{}，通知类型：{}", notification.getReceiveBy(), notification.getType());
                }
            }
            return result;
        } catch (Exception e) {
            log.error("批量发送通知失败", e);
            return false;
        }
    }
    
    @Override
    public Notification getById(Long id) {
        try {
            Notification notification = baseMapper.selectById(id);
            if (notification != null) {
                log.info("根据ID获取通知成功，通知ID：{}", id);
            } else {
                log.error("根据ID获取通知失败，通知ID：{}", id);
            }
            return notification;
        } catch (Exception e) {
            log.error("根据ID获取通知失败", e);
            return null;
        }
    }
    
    @Override
    public List<Notification> listByReceiveBy(String receiveBy) {
        try {
            // 使用LambdaQueryWrapper构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getReceiveBy, receiveBy)
                       .orderByDesc(Notification::getCreateTime);
            
            List<Notification> notifications = baseMapper.selectList(queryWrapper);
            log.info("根据接收人获取通知成功，接收人：{}，返回数量：{}", receiveBy, notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("根据接收人获取通知失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean markAsRead(Long id) {
        try {
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(1); // 已读
            
            boolean result = baseMapper.updateById(notification) > 0;
            if (result) {
                log.info("更新通知状态为已读成功，通知ID：{}", id);
            } else {
                log.error("更新通知状态为已读失败，通知ID：{}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("更新通知状态为已读失败", e);
            return false;
        }
    }
    
    @Override
    public boolean markBatchAsRead(List<Long> ids) {
        try {
            // 使用LambdaUpdateWrapper构建更新条件
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Notification> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.in(Notification::getId, ids)
                        .set(Notification::getStatus, 1); // 已读
            
            boolean result = baseMapper.update(null, updateWrapper) > 0;
            if (result) {
                log.info("批量更新通知状态为已读成功，通知数量：{}", ids.size());
            } else {
                log.error("批量更新通知状态为已读失败，通知数量：{}", ids.size());
            }
            return result;
        } catch (Exception e) {
            log.error("批量更新通知状态为已读失败", e);
            return false;
        }
    }
    
    @Override
    public boolean removeById(Long id) {
        try {
            boolean result = baseMapper.deleteById(id) > 0;
            if (result) {
                log.info("删除通知成功，通知ID：{}", id);
            } else {
                log.error("删除通知失败，通知ID：{}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除通知失败", e);
            return false;
        }
    }
}