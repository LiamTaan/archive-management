package com.electronic.archive.controller;

import com.electronic.archive.dto.HookValidationRequestDTO;
import com.electronic.archive.service.HookValidationService;
import com.electronic.archive.vo.HookValidationResultVO;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 挂接校验控制器
 * 提供校验API接口
 */
@RestController
@RequestMapping("/validation")
@Tag(name = "挂接校验管理", description = "提供档案挂接校验相关的API接口")
public class HookValidationController {

    @Autowired
    private HookValidationService hookValidationService;

    /**
     * 单文件挂接校验
     * @param requestDTO 校验请求DTO
     * @return 校验结果
     */
    @PostMapping("/single")
    @Operation(summary = "单文件挂接校验", description = "对单个文件进行挂接校验，包括格式、完整性和业务信息校验")
    public ResponseResult<HookValidationResultVO> validateSingleHook(@RequestBody HookValidationRequestDTO requestDTO) {
        try {
            HookValidationResultVO result = hookValidationService.validateSingleHook(requestDTO);
            return ResponseResult.success("单文件挂接校验成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("单文件挂接校验失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件挂接校验
     * @param requestDTOList 校验请求DTO列表
     * @return 批量校验结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量文件挂接校验", description = "对多个文件进行批量挂接校验")
    public ResponseResult<List<HookValidationResultVO>> validateBatchHook(@RequestBody List<HookValidationRequestDTO> requestDTOList) {
        try {
            List<HookValidationResultVO> results = hookValidationService.validateBatchHook(requestDTOList);
            return ResponseResult.success("批量文件挂接校验成功", results);
        } catch (Exception e) {
            return ResponseResult.fail("批量文件挂接校验失败: " + e.getMessage());
        }
    }

    /**
     * 手动修正校验失败的挂接
     * @param archiveId 档案ID
     * @param requestDTO 修正后的校验请求DTO
     * @return 修正结果
     */
    @PutMapping("/correct/{archiveId}")
    @Operation(summary = "手动修正校验失败的挂接", description = "对校验失败的挂接进行手动修正")
    public ResponseResult<?> manualCorrectHook(@PathVariable Long archiveId, @RequestBody HookValidationRequestDTO requestDTO) {
        try {
            return hookValidationService.manualCorrectHook(archiveId, requestDTO);
        } catch (Exception e) {
            return ResponseResult.fail("手动修正挂接失败: " + e.getMessage());
        }
    }

    /**
     * 仅校验档案格式
     * @param fileType 文件类型
     * @param filePath 文件路径
     * @return 校验结果
     */
    @GetMapping("/format")
    @Operation(summary = "仅校验档案格式", description = "仅对档案格式进行校验")
    public ResponseResult<Boolean> validateFileFormat(@RequestParam String fileType, @RequestParam String filePath) {
        try {
            boolean result = hookValidationService.validateFileFormat(fileType, filePath);
            return ResponseResult.success("档案格式校验成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("档案格式校验失败: " + e.getMessage());
        }
    }

    /**
     * 仅校验文件完整性
     * @param filePath 文件路径
     * @param expectedMd5 预期MD5值
     * @return 校验结果
     */
    @GetMapping("/integrity")
    @Operation(summary = "仅校验文件完整性", description = "仅对文件完整性进行MD5校验")
    public ResponseResult<Boolean> validateFileIntegrity(@RequestParam String filePath, @RequestParam String expectedMd5) {
        try {
            boolean result = hookValidationService.validateFileIntegrity(filePath, expectedMd5);
            return ResponseResult.success("文件完整性校验成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("文件完整性校验失败: " + e.getMessage());
        }
    }

    /**
     * 生成批量校验报告
     * @param validationResults 校验结果列表
     * @return 校验报告
     */
    @PostMapping("/report/batch")
    @Operation(summary = "生成批量校验报告", description = "根据批量校验结果生成校验报告")
    public ResponseResult<Map<String, Object>> generateBatchReport(@RequestBody List<HookValidationResultVO> validationResults) {
        try {
            Map<String, Object> report = hookValidationService.generateValidationReport(validationResults);
            return ResponseResult.success("生成批量校验报告成功", report);
        } catch (Exception e) {
            return ResponseResult.fail("生成批量校验报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成单文件校验报告
     * @param validationResult 校验结果
     * @return 校验报告
     */
    @PostMapping("/report/single")
    @Operation(summary = "生成单文件校验报告", description = "根据单文件校验结果生成校验报告")
    public ResponseResult<Map<String, Object>> generateSingleReport(@RequestBody HookValidationResultVO validationResult) {
        try {
            Map<String, Object> report = hookValidationService.generateSingleValidationReport(validationResult);
            return ResponseResult.success("生成单文件校验报告成功", report);
        } catch (Exception e) {
            return ResponseResult.fail("生成单文件校验报告失败: " + e.getMessage());
        }
    }
}