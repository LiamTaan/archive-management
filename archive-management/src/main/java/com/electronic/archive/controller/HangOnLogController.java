package com.electronic.archive.controller;

import com.electronic.archive.dto.HangOnLogQueryDTO;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.service.HangOnLogService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 挂接日志控制器
 */
@Tag(name = "挂接日志")
@RestController
@RequestMapping("/hang-on/log")
public class HangOnLogController {
    @Autowired
    private HangOnLogService hangOnLogService;

    @Operation(summary = "查询挂接日志")
    @GetMapping("/query")
    public ResponseResult<PageResult<HangOnLog>> queryHangOnLogs(HangOnLogQueryDTO queryDTO) {
        
        // 创建查询条件
        LambdaQueryWrapper<HangOnLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 档案ID查询
        if (queryDTO.getArchiveId() != null) {
            queryWrapper.eq(HangOnLog::getArchiveId, queryDTO.getArchiveId());
        }
        
        // 操作人查询
        if (queryDTO.getOperateBy() != null && !queryDTO.getOperateBy().isEmpty()) {
            queryWrapper.eq(HangOnLog::getOperateBy, queryDTO.getOperateBy());
        }
        
        // 操作类型查询
        if (queryDTO.getHangOnType() != null) {
            queryWrapper.eq(HangOnLog::getHangOnType, queryDTO.getHangOnType());
        }
        
        // 挂接结果查询
        if (queryDTO.getResult() != null) {
            queryWrapper.eq(HangOnLog::getResult, queryDTO.getResult());
        }
        
        // 按操作时间倒序排序
        queryWrapper.orderByDesc(HangOnLog::getCreateTime);
        
        // 查询挂接日志
        var logPage = hangOnLogService.page(queryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<HangOnLog> result = PageResult.fromMpPage(logPage);
        
        return ResponseResult.success("查询挂接日志成功", result);
    }

    @Operation(summary = "根据ID查询挂接日志详情")
    @GetMapping("/{logId}")
    public ResponseResult<HangOnLog> getHangOnLogDetail(@PathVariable Long logId) {
        HangOnLog log = hangOnLogService.getById(logId);
        if (log != null) {
            return ResponseResult.success("查询挂接日志详情成功", log);
        } else {
            return ResponseResult.fail("挂接日志不存在");
        }
    }
}
