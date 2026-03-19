package com.electronic.archive.service;

import com.electronic.archive.dto.HookValidationRequestDTO;
import com.electronic.archive.vo.HookValidationResultVO;
import com.electronic.archive.vo.ResponseResult;

import java.util.List;
import java.util.Map;

/**
 * 挂接校验服务接口
 * 定义各种校验逻辑的方法
 */
public interface HookValidationService {

    /**
     * 单文件挂接校验
     * @param requestDTO 校验请求DTO
     * @return 校验结果
     */
    HookValidationResultVO validateSingleHook(HookValidationRequestDTO requestDTO);

    /**
     * 批量文件挂接校验
     * @param requestDTOList 校验请求DTO列表
     * @return 批量校验结果
     */
    List<HookValidationResultVO> validateBatchHook(List<HookValidationRequestDTO> requestDTOList);

    /**
     * 校验档案格式
     * @param fileType 文件类型
     * @param filePath 文件路径
     * @return 校验结果
     */
    boolean validateFileFormat(String fileType, String filePath);

    /**
     * 校验文件完整性
     * @param filePath 文件路径
     * @param expectedMd5 预期MD5值
     * @return 校验结果
     */
    boolean validateFileIntegrity(String filePath, String expectedMd5);

    /**
     * 校验业务信息
     * @param requestDTO 校验请求DTO
     * @return 校验结果
     */
    boolean validateBusinessInfo(HookValidationRequestDTO requestDTO);

    /**
     * 手动修正校验失败的挂接
     * @param archiveId 档案ID
     * @param requestDTO 修正后的校验请求DTO
     * @return 修正结果
     */
    ResponseResult<?> manualCorrectHook(Long archiveId, HookValidationRequestDTO requestDTO);

    /**
     * 生成校验报告
     * @param validationResults 校验结果列表
     * @return 校验报告
     */
    Map<String, Object> generateValidationReport(List<HookValidationResultVO> validationResults);

    /**
     * 生成单文件校验报告
     * @param validationResult 校验结果
     * @return 校验报告
     */
    Map<String, Object> generateSingleValidationReport(HookValidationResultVO validationResult);
}