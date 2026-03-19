package com.electronic.archive.dto;

import lombok.Data;

/**
 * 挂接校验请求DTO
 * 用于接收挂接校验请求参数
 */
@Data
public class HookValidationRequestDTO {
    /**
     * 档案ID
     */
    private Long archiveId;

    /**
     * 业务单号
     */
    private String businessNo;

    /**
     * 责任人
     */
    private String responsiblePerson;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 档案分类
     */
    private String archiveType;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件MD5值
     */
    private String md5Value;

    /**
     * 操作人
     */
    private String operateBy;
}