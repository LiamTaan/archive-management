package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 档案组合挂接请求参数
 */
@Data
public class CombinationHangOnRequestDTO {
    /**
     * 组合ID
     */
    private Long combinationId;

    /**
     * 组合名称
     */
    private String combinationName;

    /**
     * 组合类型
     */
    private String combinationType;

    /**
     * 档案ID列表
     */
    private List<Long> archiveIds;

    /**
     * 目标系统代码
     */
    private String systemCode;

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
    
    /**
     * 备注
     */
    private String remark;
}