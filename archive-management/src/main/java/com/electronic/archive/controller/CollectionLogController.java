package com.electronic.archive.controller;

import com.electronic.archive.dto.CollectionLogQueryDTO;
import com.electronic.archive.entity.CollectionLog;
import com.electronic.archive.service.CollectionLogService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 采集日志控制器
 */
@Tag(name = "采集日志")
@RestController
@RequestMapping("/collection/log")
public class CollectionLogController {
    @Autowired
    private CollectionLogService collectionLogService;

    @Operation(summary = "查询采集日志")
    @GetMapping("/query")
    public ResponseResult<PageResult<CollectionLog>> queryCollectionLogs(CollectionLogQueryDTO queryDTO) {
        
        // 创建查询条件
        LambdaQueryWrapper<CollectionLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 档案ID查询
        if (queryDTO.getArchiveId() != null) {
            queryWrapper.eq(CollectionLog::getArchiveId, queryDTO.getArchiveId());
        }
        
        // 采集类型查询
        if (queryDTO.getCollectionType() != null) {
            queryWrapper.eq(CollectionLog::getCollectionType, queryDTO.getCollectionType());
        }
        
        // 操作人查询
        if (queryDTO.getOperateBy() != null && !queryDTO.getOperateBy().isEmpty()) {
            queryWrapper.eq(CollectionLog::getOperateBy, queryDTO.getOperateBy());
        }
        
        // 采集结果查询
        if (queryDTO.getResult() != null) {
            queryWrapper.eq(CollectionLog::getResult, queryDTO.getResult());
        }
        
        // 按操作时间倒序排序
        queryWrapper.orderByDesc(CollectionLog::getOperateTime);
        
        // 查询采集日志
        var logPage = collectionLogService.page(queryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<CollectionLog> result = PageResult.fromMpPage(logPage);
        
        return ResponseResult.success("查询采集日志成功", result);
    }

    @Operation(summary = "根据ID查询采集日志详情")
    @GetMapping("/{logId}")
    public ResponseResult<CollectionLog> getCollectionLogDetail(@PathVariable Long logId) {
        CollectionLog log = collectionLogService.getById(logId);
        if (log != null) {
            return ResponseResult.success("查询采集日志详情成功", log);
        } else {
            return ResponseResult.fail("采集日志不存在");
        }
    }
}