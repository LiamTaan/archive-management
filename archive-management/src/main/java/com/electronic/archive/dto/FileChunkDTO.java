package com.electronic.archive.dto;

import lombok.Data;

/**
 * 文件分片信息DTO
 * 用于大文件下载时返回分片信息
 */
@Data
public class FileChunkDTO {
    /**
     * 文件唯一标识
     */
    private String fileId;
    
    /**
     * 文件总大小（字节）
     */
    private long totalSize;
    
    /**
     * 分片大小（字节）
     */
    private long chunkSize;
    
    /**
     * 总分片数量
     */
    private int totalChunks;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件MD5值（用于完整性校验）
     */
    private String md5;
}