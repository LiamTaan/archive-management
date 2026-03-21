package com.electronic.archive.dto;

import com.electronic.archive.util.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案查询DTO
 * 用于接收档案查询请求参数
 */
@Data
public class ArchiveQueryDTO extends PageRequest {
    /**
     * 档案ID
     */
    private Long id;

    /**
     * 文件名（支持模糊查询）
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 档案分类
     */
    private String archiveType;

    /**
     * 业务单号
     */
    private String businessNo;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 责任人
     */
    private String responsiblePerson;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 档案状态（0-未挂接，1-已挂接，2-挂接失败）
     */
    private Integer status;

    /**
     * 挂接方式（0-自动，1-手动）
     */
    private Integer hangOnType;

    /**
     * 开始时间（创建时间）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（创建时间）
     */
    private LocalDateTime endTime;

    /**
     * 最小文件大小（字节）
     */
    private Long minFileSize;

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;
}