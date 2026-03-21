package com.electronic.archive.dto;

import lombok.Data;

import java.util.List;

/**
 * 文件预览基础信息DTO
 * 用于获取文件预览的基本信息
 */
@Data
public class FilePreviewDTO {
    /**
     * 文件唯一标识
     */
    private String fileId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件总大小（字节）
     */
    private long fileSize;
    
    /**
     * 预览类型（pdf, video, image, office等）
     */
    private String previewType;
    
    /**
     * 是否需要转换（例如Office文件需要转换为PDF）
     */
    private boolean needConvert;
    
    /**
     * 转换状态（当needConvert为true时有效）
     * 0: 未转换, 1: 转换中, 2: 转换成功, 3: 转换失败
     */
    private int convertStatus;
    
    /**
     * 预览相关参数（例如PDF的总页数）
     */
    private PreviewParams params;
    
    /**
     * 预览参数内部类
     */
    @Data
    public static class PreviewParams {
        /**
         * PDF总页数
         */
        private int totalPages;
        
        /**
         * 视频时长（秒）
         */
        private long duration;
        
        /**
         * 视频分辨率
         */
        private String resolution;
        
        /**
         * 支持的视频格式
         */
        private List<String> supportedFormats;
    }
}