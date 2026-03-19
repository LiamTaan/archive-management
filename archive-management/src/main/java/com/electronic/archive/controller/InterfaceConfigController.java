package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electronic.archive.dto.InterfaceConfigQueryDTO;
import com.electronic.archive.entity.InterfaceConfig;
import com.electronic.archive.service.InterfaceConfigService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口配置控制器
 */
@Tag(name = "接口配置")
@RestController
@RequestMapping("/interface-config")
public class InterfaceConfigController {
    @Autowired
    private InterfaceConfigService interfaceConfigService;

    @Operation(summary = "获取接口配置列表")
    @GetMapping("/list")
    public ResponseResult<PageResult<InterfaceConfig>> getInterfaceConfigs(InterfaceConfigQueryDTO queryDTO) {
        
        QueryWrapper<InterfaceConfig> queryWrapper = new QueryWrapper<>();
        
        if (queryDTO.getInterfaceName() != null && !queryDTO.getInterfaceName().isEmpty()) {
            queryWrapper.like("interface_name", queryDTO.getInterfaceName());
        }
        
        if (queryDTO.getInterfaceType() != null) {
            queryWrapper.eq("interface_type", queryDTO.getInterfaceType());
        }
        
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        
        queryWrapper.orderByDesc("create_time");
        
        var result = interfaceConfigService.page(queryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<InterfaceConfig> pageResult = PageResult.fromMpPage(result);
        
        return ResponseResult.success("获取接口配置列表成功", pageResult);
    }

    @Operation(summary = "获取接口配置详情")
    @GetMapping("/{id}")
    public ResponseResult<InterfaceConfig> getInterfaceConfig(@PathVariable Long id) {
        InterfaceConfig interfaceConfig = interfaceConfigService.getById(id);
        if (interfaceConfig != null) {
            return ResponseResult.success("获取接口配置详情成功", interfaceConfig);
        } else {
            return ResponseResult.fail("接口配置不存在");
        }
    }

    @Operation(summary = "新增接口配置")
    @PostMapping
    public ResponseResult<InterfaceConfig> addInterfaceConfig(@RequestBody InterfaceConfig interfaceConfig) {
        boolean result = interfaceConfigService.save(interfaceConfig);
        if (result) {
            return ResponseResult.success("新增接口配置成功", interfaceConfig);
        } else {
            return ResponseResult.fail("新增接口配置失败");
        }
    }

    @Operation(summary = "更新接口配置")
    @PutMapping("/{id}")
    public ResponseResult<InterfaceConfig> updateInterfaceConfig(@PathVariable Long id, @RequestBody InterfaceConfig interfaceConfig) {
        interfaceConfig.setId(id);
        boolean result = interfaceConfigService.updateById(interfaceConfig);
        if (result) {
            return ResponseResult.success("更新接口配置成功", interfaceConfig);
        } else {
            return ResponseResult.fail("更新接口配置失败");
        }
    }

    @Operation(summary = "删除接口配置")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteInterfaceConfig(@PathVariable Long id) {
        boolean result = interfaceConfigService.removeById(id);
        if (result) {
            return ResponseResult.success("删除接口配置成功");
        } else {
            return ResponseResult.fail("删除接口配置失败");
        }
    }

    @Operation(summary = "更新接口配置状态")
    @PutMapping("/{id}/status")
    public ResponseResult<Void> updateInterfaceConfigStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        InterfaceConfig interfaceConfig = new InterfaceConfig();
        interfaceConfig.setId(id);
        interfaceConfig.setStatus(status);
        boolean result = interfaceConfigService.updateById(interfaceConfig);
        if (result) {
            return ResponseResult.success("更新接口配置状态成功");
        } else {
            return ResponseResult.fail("更新接口配置状态失败");
        }
    }
}
