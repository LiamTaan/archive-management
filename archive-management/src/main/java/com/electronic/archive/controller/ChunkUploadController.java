package com.electronic.archive.controller;

import com.electronic.archive.dto.ChunkUploadDTO;
import com.electronic.archive.dto.MergeChunksDTO;
import com.electronic.archive.service.ChunkUploadService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分片上传控制器
 */
@Tag(name = "分片上传")
@RestController
@RequestMapping("/chunk")
public class ChunkUploadController {
    
    @Autowired
    private ChunkUploadService chunkUploadService;
    
    /**
     * 上传分片
     * @param chunkUploadDTO 分片上传请求参数
     * @return 上传结果
     */
    @Operation(summary = "上传分片")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseResult<Void> uploadChunk(ChunkUploadDTO chunkUploadDTO) {
        return chunkUploadService.uploadChunk(chunkUploadDTO);
    }
    
    /**
     * 合并分片
     * @param mergeChunksDTO 合并分片请求参数
     * @return 合并结果
     */
    @Operation(summary = "合并分片")
    @PostMapping("/merge")
    public ResponseResult<Void> mergeChunks(@RequestBody MergeChunksDTO mergeChunksDTO) {
        return chunkUploadService.mergeChunks(mergeChunksDTO);
    }
    
    /**
     * 检查分片是否已上传
     * @param fileMd5 文件MD5
     * @param chunkIndex 分片索引
     * @return 检查结果
     */
    @Operation(summary = "检查分片是否已上传")
    @GetMapping("/check")
    public ResponseResult<Boolean> checkChunk(@RequestParam String fileMd5, @RequestParam Integer chunkIndex) {
        return chunkUploadService.checkChunk(fileMd5, chunkIndex);
    }
    
    /**
     * 检查文件是否已上传完成
     * @param fileMd5 文件MD5
     * @return 检查结果
     */
    @Operation(summary = "检查文件是否已上传完成")
    @GetMapping("/checkFile")
    public ResponseResult<Boolean> checkFile(@RequestParam String fileMd5) {
        return chunkUploadService.checkFile(fileMd5);
    }
}