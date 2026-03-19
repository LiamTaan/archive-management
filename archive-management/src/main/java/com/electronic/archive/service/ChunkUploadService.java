package com.electronic.archive.service;

import com.electronic.archive.dto.ChunkUploadDTO;
import com.electronic.archive.dto.MergeChunksDTO;
import com.electronic.archive.vo.ResponseResult;

/**
 * 分片上传服务接口
 */
public interface ChunkUploadService {
    
    /**
     * 上传分片
     * @param chunkUploadDTO 分片上传请求参数
     * @return 上传结果
     */
    ResponseResult<Void> uploadChunk(ChunkUploadDTO chunkUploadDTO);
    
    /**
     * 合并分片
     * @param mergeChunksDTO 合并分片请求参数
     * @return 合并结果
     */
    ResponseResult<Void> mergeChunks(MergeChunksDTO mergeChunksDTO);
    
    /**
     * 检查分片是否已上传
     * @param fileMd5 文件MD5
     * @param chunkIndex 分片索引
     * @return 检查结果
     */
    ResponseResult<Boolean> checkChunk(String fileMd5, Integer chunkIndex);
    
    /**
     * 检查文件是否已上传完成
     * @param fileMd5 文件MD5
     * @return 检查结果
     */
    ResponseResult<Boolean> checkFile(String fileMd5);
}