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
     * 通知ID，主键，自增
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型(0-系统通知，1-挂接通知，2-审批通知)
     */
    private Integer type;

    /**
     * 接收人
     */
    @TableField("receiver")
    private String receiveBy;

    /**
     * 发送人
     */
    @TableField("sender")
    private String sendBy;

    /**
     * 通知状态(0-未读，1-已读)
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 关联业务ID
     */
    private Long businessId;

    /**
     * 关联业务类型
     */
    private String businessType;
}