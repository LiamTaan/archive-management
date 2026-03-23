package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 采集请求参数
 */
@Data
public class CollectionRequestDTO {
    /**
     * 档案类型
     */
    private Integer archiveType;

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
     * 部门ID
     */
    private Long deptId;

    /**
     * 档案路径列表
     */
    private List<String> filePaths;

    /**
     * 元数据
     */
    private String metadata;

    /**
     * 操作人
     */
    private String operateBy;

    /**
     * 备注
     */
    private String remark;
}