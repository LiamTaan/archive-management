package com.electronic.archive.controller;

import com.electronic.archive.service.SystemParamService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统参数控制器
 */
@Tag(name = "系统参数")
@RestController
@RequestMapping("/system/param")
public class SystemParamController {
    @Autowired
    private SystemParamService systemParamService;

    /**
     * 获取所有系统参数
     * @return 系统参数Map
     */
    @Operation(summary = "获取所有系统参数")
    @GetMapping("/all")
    public ResponseResult<Map<String, String>> getAllParams() {
        Map<String, String> params = systemParamService.getAllParams();
        return ResponseResult.success("获取系统参数成功", params);
    }

    /**
     * 根据参数键名获取参数值
     * @param paramKey 参数键名
     * @return 参数值
     */
    @Operation(summary = "根据参数键名获取参数值")
    @GetMapping("/get")
    public ResponseResult<String> getParamByKey(@RequestParam String paramKey) {
        String paramValue = systemParamService.getParamByKey(paramKey);
        return ResponseResult.success("获取系统参数成功", paramValue);
    }

    /**
     * 保存系统参数
     * @param paramMap 系统参数Map
     * @return 是否保存成功
     */
    @Operation(summary = "保存系统参数")
    @PostMapping("/save")
    public ResponseResult<Boolean> saveParams(@RequestBody Map<String, String> paramMap) {
        boolean success = systemParamService.saveParams(paramMap);
        if (success) {
            return ResponseResult.success("保存系统参数成功", true);
        } else {
            return ResponseResult.fail("保存系统参数失败");
        }
    }
}