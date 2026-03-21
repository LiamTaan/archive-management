package com.electronic.archive.controller;

import com.electronic.archive.dto.CollectionRequestDTO;
import com.electronic.archive.entity.CollectionProgress;
import com.electronic.archive.service.ArchiveCollectionService;
import com.electronic.archive.service.CollectionProgressService;
import com.electronic.archive.vo.CollectionProgressVO;
import com.electronic.archive.vo.CollectionResultVO;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 档案采集控制器
 */
@Tag(name = "档案采集")
@RestController
@RequestMapping("/collection")
public class ArchiveCollectionController {
    @Autowired
    private ArchiveCollectionService archiveCollectionService;
    
    @Autowired
    private CollectionProgressService collectionProgressService;

    @Operation(summary = "自动接口采集")
    @PostMapping("/auto/{interfaceId}")
    public ResponseResult<CollectionResultVO> autoCollect(@PathVariable Long interfaceId) {
        return archiveCollectionService.autoCollect(interfaceId);
    }

    @Operation(summary = "手动上传采集")
    @PostMapping(value = "/manual", consumes = "multipart/form-data")
    public ResponseResult<CollectionResultVO> manualUpload(
            @RequestParam Integer archiveType,
            @RequestParam String metadata,
            @RequestParam String operateBy,
            @RequestPart List<MultipartFile> files) {
        CollectionRequestDTO requestDTO = new CollectionRequestDTO();
        requestDTO.setArchiveType(archiveType);
        requestDTO.setMetadata(metadata);
        requestDTO.setOperateBy(operateBy);
        return archiveCollectionService.manualUpload(requestDTO, files);
    }

    @Operation(summary = "批量上传采集")
    @PostMapping(value = "/batch", consumes = "multipart/form-data")
    public ResponseResult<CollectionResultVO> batchUpload(
            @RequestParam Integer archiveType,
            @RequestParam String metadata,
            @RequestParam String operateBy,
            @RequestPart List<MultipartFile> files) {
        CollectionRequestDTO requestDTO = new CollectionRequestDTO();
        requestDTO.setArchiveType(archiveType);
        requestDTO.setMetadata(metadata);
        requestDTO.setOperateBy(operateBy);
        return archiveCollectionService.batchUpload(requestDTO, files);
    }

    @Operation(summary = "外部导入采集")
    @PostMapping(value = "/external", consumes = "multipart/form-data")
    public ResponseResult<CollectionResultVO> externalImport(
            @RequestParam Integer archiveType,
            @RequestParam String metadata,
            @RequestParam String operateBy,
            @RequestPart List<MultipartFile> files) {
        CollectionRequestDTO requestDTO = new CollectionRequestDTO();
        requestDTO.setArchiveType(archiveType);
        requestDTO.setMetadata(metadata);
        requestDTO.setOperateBy(operateBy);
        return archiveCollectionService.externalImport(requestDTO, files);
    }
    
    @Operation(summary = "查询采集进度")
    @GetMapping("/progress/{taskId}")
    public ResponseResult<CollectionProgressVO> getProgress(@PathVariable String taskId) {
        // 获取进度信息
        CollectionProgress progress = collectionProgressService.getByTaskId(taskId);
        if (progress == null) {
            return ResponseResult.fail("未找到进度信息");
        }
        
        // 转换为VO
        CollectionProgressVO progressVO = new CollectionProgressVO();
        progressVO.setTaskId(progress.getTaskId());
        progressVO.setCollectionType(progress.getCollectionType());
        progressVO.setProgress(progress.getProgress());
        progressVO.setProcessedCount(progress.getProcessedCount());
        progressVO.setTotalCount(progress.getTotalCount());
        progressVO.setStatus(progress.getStatus());
        progressVO.setDescription(progress.getDescription());
        progressVO.setUpdateTime(progress.getUpdateTime());
        
        return ResponseResult.success("获取进度成功", progressVO);
    }
}