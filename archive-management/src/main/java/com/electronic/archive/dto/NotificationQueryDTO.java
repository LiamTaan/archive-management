package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

/**
 * 通知查询DTO
 * 用于接收通知查询请求参数
 */
@Data
public class NotificationQueryDTO extends PageRequest {
    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 通知状态
     */
    private Integer status;

    /**
     * 接收人ID
     */
    private Long receiverId;

    /**
     * 接收人名称
     */
    private String receiverName;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 发送人名称
     */
    private String senderName;

    /**
     * 关联的档案ID
     */
    private Long archiveId;

    /**
     * 关联的操作ID
     */
    private Long operationId;
}