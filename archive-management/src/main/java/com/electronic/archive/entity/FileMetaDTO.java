package com.electronic.archive.entity;

import lombok.Data;

import java.util.List;

/**
 * 文件元信息DTO
 */
@Data
public class FileMetaDTO {
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 业务单号
     */
    private String businessNo;
    
    /**
     * 文件总MD5值
     */
    private String totalMd5;
    
    /**
     * 分片总数
     */
    private Integer shardCount;
    
    /**
     * 分片大小（字节）
     */
    private Integer shardSize;
    
    /**
     * 分片URL列表
     */
    private List<String> shardUrls;
    
    /**
     * 分片MD5列表
     */
    private List<String> shardMd5s;
    
    /**
     * 所属部门
     */
    private String department;

    /**
     * 部门ID
     */
    private Long deptId;
}