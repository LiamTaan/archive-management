package com.electronic.archive.controller;

import com.electronic.archive.task.AutoHangOnTask;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 自动挂接控制器
 * 提供手动触发自动挂接的接口
 */
@RestController
@RequestMapping("/api/auto-hang-on")
@Tag(name = "自动挂接管理", description = "自动挂接相关接口")
@Slf4j
public class AutoHangOnController {

    @Autowired
    private AutoHangOnTask autoHangOnTask;

    /**
     * 手动触发自动挂接任务
     * 用于手动执行自动挂接逻辑
     * @param interfaceId 接口ID，可以为null表示执行所有启用的接口
     * @return 响应结果
     */
    @Operation(summary = "手动触发自动挂接", description = "手动执行自动挂接逻辑，可指定接口ID或执行所有启用的接口")
    @PreAuthorize("hasAnyRole('ARCHIVE_OPER', 'ARCHIVE_ADMIN')")
    @PostMapping("/trigger")
    public ResponseResult<String> triggerAutoHangOn(@RequestParam(required = false) Long interfaceId) {
        try {
            log.info("接收到手动触发自动挂接请求，接口ID：{}", interfaceId);
            
            // 调用自动挂接任务类的手动触发方法
            autoHangOnTask.manualTriggerAutoHangOn(interfaceId);
            
            String message = interfaceId != null ? 
                    String.format("已触发接口ID为%s的自动挂接任务", interfaceId) : 
                    "已触发所有启用接口的自动挂接任务";
            
            return ResponseResult.success(message);
        } catch (Exception e) {
            log.error("手动触发自动挂接失败", e);
            return ResponseResult.fail("手动触发自动挂接失败：" + e.getMessage());
        }
    }
}