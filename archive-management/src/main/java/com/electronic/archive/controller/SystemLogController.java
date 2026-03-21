package com.electronic.archive.controller;

import com.electronic.archive.dto.SystemLogQueryDTO;
import com.electronic.archive.entity.SystemLog;
import com.electronic.archive.service.SystemLogService;
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
 * 系统日志控制器
 */
@Tag(name = "系统日志")
@RestController
@RequestMapping("/system/log")
public class SystemLogController {
    @Autowired
    private SystemLogService systemLogService;

    @Operation(summary = "查询系统日志")
    @GetMapping("/query")
    public ResponseResult<PageResult<SystemLog>> querySystemLogs(SystemLogQueryDTO systemLogQueryDTO) {
        
        // 创建查询条件
        LambdaQueryWrapper<SystemLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 日志级别查询
        if (systemLogQueryDTO.getLogLevel() != null) {
            queryWrapper.eq(SystemLog::getLogLevel, systemLogQueryDTO.getLogLevel());
        }
        
        // 日志类型查询
        if (systemLogQueryDTO.getLogType() != null) {
            queryWrapper.eq(SystemLog::getLogType, systemLogQueryDTO.getLogType());
        }
        
        // 操作人查询
        if (systemLogQueryDTO.getOperateBy() != null && !systemLogQueryDTO.getOperateBy().isEmpty()) {
            queryWrapper.eq(SystemLog::getOperateBy, systemLogQueryDTO.getOperateBy());
        }
        
        // 操作IP查询
        if (systemLogQueryDTO.getOperateIp() != null && !systemLogQueryDTO.getOperateIp().isEmpty()) {
            queryWrapper.eq(SystemLog::getOperateIp, systemLogQueryDTO.getOperateIp());
        }
        
        // 按操作时间倒序排序
        queryWrapper.orderByDesc(SystemLog::getCreateTime);
        
        // 查询系统日志
        var logPage = systemLogService.page(systemLogQueryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<SystemLog> result = PageResult.fromMpPage(logPage);
        
        return ResponseResult.success("查询系统日志成功", result);
    }


}