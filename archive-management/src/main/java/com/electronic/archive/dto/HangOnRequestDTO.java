package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 挂接请求参数
 */
@Data
public class HangOnRequestDTO {
    /**
     * 档案ID列表
     */
    private List<Long> archiveIds;

    /**
     * 目标系统代码列表
     */
    private List<String> systemCode;

    /**
     * 操作人
     */
    private String operateBy;

    /**
     * 挂接方式
     */
    private String hangOnMethod;
    
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
}