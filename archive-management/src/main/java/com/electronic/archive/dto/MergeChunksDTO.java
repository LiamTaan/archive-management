package com.electronic.archive.dto;

import lombok.Data;

/**
 * 合并分片请求参数
 */
@Data
public class MergeChunksDTO {
    /**
     * 文件唯一标识符，用于标识同一个文件
     */
    private String fileMd5;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件总大小，单位：字节
     */
    private Long totalSize;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
    /**
     * 上传类型（manual/batch/external）
     */
    private String uploadType;
    
    /**
     * 档案类型
     */
    private Integer archiveType;
    
    /**
     * 元数据
     */
    private String metadata;
    
    /**
     * 操作人
     */
    private String operateBy;
}