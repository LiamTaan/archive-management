package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知表
 */
@Data
@TableName("notification")
public class Notification {
    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知类型(0-系统 通知，1-挂接失败提醒, 2-挂接完成提醒, 3-审批提醒，4-解除挂接通知)
     */
    @TableField("notification_type")
    private Integer type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联的档案ID
     */
    @TableField("archive_id")
    private Long archiveId;

    /**
     * 关联的操作ID
     */
    @TableField("operation_id")
    private Long operationId;

    /**
     * 接收人ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 接收人名称
     */
    @TableField("receiver_name")
    private String receiverName;

    /**
     * 发送人ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送人名称
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 通知状态(0-未读, 1-已读, 2-已处理)
     */
    private Integer status;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /**
     * 读取时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 备注
     */
    private String remark;
}