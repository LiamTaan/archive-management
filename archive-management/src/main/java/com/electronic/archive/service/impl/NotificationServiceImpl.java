package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.dto.NotificationQueryDTO;
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
            if (notification.getSendTime() == null) {
                notification.setSendTime(LocalDateTime.now());
            }
            if (notification.getStatus() == null) {
                notification.setStatus(0); // 默认未读
            }
            if (notification.getSenderId() == null) {
                notification.setSenderId(0L); // 默认系统发送
            }
            if (notification.getSenderName() == null) {
                notification.setSenderName("system"); // 默认系统发送
            }
            
            boolean result = baseMapper.insert(notification) > 0;
            if (result) {
                log.info("发送通知成功，接收人：{}，通知类型：{}", notification.getReceiverName(), notification.getType());
            } else {
                log.error("发送通知失败，接收人：{}，通知类型：{}", notification.getReceiverName(), notification.getType());
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
                if (notification.getSendTime() == null) {
                    notification.setSendTime(now);
                }
                if (notification.getStatus() == null) {
                    notification.setStatus(0); // 默认未读
                }
                if (notification.getSenderId() == null) {
                    notification.setSenderId(0L); // 默认系统发送
                }
                if (notification.getSenderName() == null) {
                    notification.setSenderName("system"); // 默认系统发送
                }
            }
            
            boolean result = true;
            for (Notification notification : notifications) {
                if (baseMapper.insert(notification) <= 0) {
                    result = false;
                    log.error("批量发送通知失败，接收人：{}，通知类型：{}", notification.getReceiverName(), notification.getType());
                } else {
                    log.info("批量发送通知成功，接收人：{}，通知类型：{}", notification.getReceiverName(), notification.getType());
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
    public List<Notification> listByReceiverId(Long receiverId) {
        try {
            // 使用LambdaQueryWrapper构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getReceiverId, receiverId)
                       .orderByDesc(Notification::getSendTime);
            
            List<Notification> notifications = baseMapper.selectList(queryWrapper);
            log.info("根据接收人ID获取通知成功，接收人ID：{}，返回数量：{}", receiverId, notifications.size());
            return notifications;
        } catch (Exception e) {
            log.error("根据接收人ID获取通知失败", e);
            return List.of();
        }
    }
    
    @Override
    public boolean markAsRead(Long id) {
        try {
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(1); // 已读
            notification.setReadTime(LocalDateTime.now());
            
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
                        .set(Notification::getStatus, 1) // 已读
                        .set(Notification::getReadTime, LocalDateTime.now());
            
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
    
    @Override
    public boolean markAsProcessed(Long id) {
        try {
            Notification notification = new Notification();
            notification.setId(id);
            notification.setStatus(2); // 已处理
            notification.setProcessTime(LocalDateTime.now());
            
            boolean result = baseMapper.updateById(notification) > 0;
            if (result) {
                log.info("更新通知状态为已处理成功，通知ID：{}", id);
            } else {
                log.error("更新通知状态为已处理失败，通知ID：{}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("更新通知状态为已处理失败", e);
            return false;
        }
    }

    @Override
    public Page<Notification> queryNotificationByPage(NotificationQueryDTO queryDTO) {
        try {
            // 转换为MyBatis-Plus的Page对象
            Page<Notification> page = queryDTO.toMpPage();

            // 构建查询条件
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();

            // 通知类型
            if (queryDTO.getType() != null) {
                if (queryDTO.getType() == 1) {
                    // 业务通知，不包含系统通知
                    queryWrapper.notIn(Notification::getType, 0);
                }else {
                    queryWrapper.eq(Notification::getType, queryDTO.getType());
                }
            }

            // 通知状态
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(Notification::getStatus, queryDTO.getStatus());
            }

            // 接收人ID
            if (queryDTO.getReceiverId() != null) {
                // 或者接收人ID是null
                queryWrapper.eq(Notification::getReceiverId, queryDTO.getReceiverId())
                        .or().isNull(Notification::getReceiverId);
            }

            // 接收人名称
            if (queryDTO.getReceiverName() != null && !queryDTO.getReceiverName().isEmpty()) {
                queryWrapper.like(Notification::getReceiverName, queryDTO.getReceiverName());
            }

            // 发送人ID
            if (queryDTO.getSenderId() != null) {
                queryWrapper.eq(Notification::getSenderId, queryDTO.getSenderId());
            }

            // 发送人名称
            if (queryDTO.getSenderName() != null && !queryDTO.getSenderName().isEmpty()) {
                queryWrapper.like(Notification::getSenderName, queryDTO.getSenderName());
            }

            // 关联的档案ID
            if (queryDTO.getArchiveId() != null) {
                queryWrapper.eq(Notification::getArchiveId, queryDTO.getArchiveId());
            }

            // 关联的操作ID
            if (queryDTO.getOperationId() != null) {
                queryWrapper.eq(Notification::getOperationId, queryDTO.getOperationId());
            }

            // 默认按发送时间倒序
            if (queryDTO.getSortField() == null || queryDTO.getSortField().isEmpty()) {
                queryWrapper.orderByDesc(Notification::getSendTime);
            }

            // 执行分页查询
            Page<Notification> resultPage = baseMapper.selectPage(page, queryWrapper);

            log.info("分页查询通知列表成功，接收人ID：{}，返回数量：{}", queryDTO.getReceiverId(), resultPage.getRecords().size());
            return resultPage;
        } catch (Exception e) {
            log.error("分页查询通知列表失败", e);
            return queryDTO.toMpPage();
        }
    }
}