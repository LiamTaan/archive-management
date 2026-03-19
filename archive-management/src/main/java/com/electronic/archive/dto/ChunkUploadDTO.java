package com.electronic.archive.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 分片上传请求参数
 */
@Data
public class ChunkUploadDTO {
    /**
     * 文件唯一标识符，用于标识同一个文件
     */
    private String fileMd5;
    
    /**
     * 当前分片索引，从0开始
     */
    private Integer chunkIndex;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
    /**
     * 分片大小，单位：字节
     */
    private Long chunkSize;
    
    /**
     * 当前分片大小，单位：字节
     */
    private Long currentChunkSize;
    
    /**
     * 文件总大小，单位：字节
     */
    private Long totalSize;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
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
    
    /**
     * 分片文件
     */
    private MultipartFile chunk;
}