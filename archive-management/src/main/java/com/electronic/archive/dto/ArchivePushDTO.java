package com.electronic.archive.dto;

import lombok.Data;

/**
 * 档案推送DTO
 * 用于接收业务系统推送的档案信息
 */
@Data
public class ArchivePushDTO {
    /**
     * 业务系统标识
     */
    private String systemCode;

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
     * 办理时间
     */
    private String handleTime;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件MD5值
     */
    private String md5Value;

    /**
     * 档案分类
     */
    private String archiveType;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 备注
     */
    private String remark;
}