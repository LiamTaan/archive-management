package com.electronic.archive.service.impl;

import com.electronic.archive.dto.HookValidationRequestDTO;
import com.electronic.archive.service.HookValidationService;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.vo.HookValidationResultVO;
import com.electronic.archive.vo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 挂接校验服务实现类
 * 实现各种校验逻辑的方法
 */
@Service
public class HookValidationServiceImpl implements HookValidationService {

    @Autowired
    private ArchiveInfoService archiveInfoService;

    @Override
    public HookValidationResultVO validateSingleHook(HookValidationRequestDTO requestDTO) {
        HookValidationResultVO result = new HookValidationResultVO();
        List<HookValidationResultVO.ValidationItem> items = new ArrayList<>();
        boolean overallValid = true;

        // 1. 校验档案格式
        HookValidationResultVO.ValidationItem formatItem = new HookValidationResultVO.ValidationItem();
        formatItem.setName("档案格式校验");
        formatItem.setType("FORMAT");
        try {
            boolean formatValid = validateFileFormat(requestDTO.getFileType(), requestDTO.getFilePath());
            formatItem.setPassed(formatValid);
            if (!formatValid) {
                formatItem.setErrorMsg("档案格式不合法");
                overallValid = false;
            }
        } catch (Exception e) {
            formatItem.setPassed(false);
            formatItem.setErrorMsg("档案格式校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(formatItem);

        // 2. 校验文件完整性
        HookValidationResultVO.ValidationItem integrityItem = new HookValidationResultVO.ValidationItem();
        integrityItem.setName("文件完整性校验");
        integrityItem.setType("INTEGRITY");
        try {
            boolean integrityValid = validateFileIntegrity(requestDTO.getFilePath(), requestDTO.getMd5Value());
            integrityItem.setPassed(integrityValid);
            if (!integrityValid) {
                integrityItem.setErrorMsg("文件完整性校验失败，MD5值不匹配");
                overallValid = false;
            }
        } catch (Exception e) {
            integrityItem.setPassed(false);
            integrityItem.setErrorMsg("文件完整性校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(integrityItem);

        // 3. 校验档案大小
        HookValidationResultVO.ValidationItem sizeItem = new HookValidationResultVO.ValidationItem();
        sizeItem.setName("档案大小校验");
        sizeItem.setType("SIZE");
        try {
            boolean sizeValid = validateFileSize(requestDTO.getFilePath());
            sizeItem.setPassed(sizeValid);
            if (!sizeValid) {
                sizeItem.setErrorMsg("档案大小超过限制");
                overallValid = false;
            }
        } catch (Exception e) {
            sizeItem.setPassed(false);
            sizeItem.setErrorMsg("档案大小校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(sizeItem);

        // 4. 校验档案命名规则
        HookValidationResultVO.ValidationItem nameItem = new HookValidationResultVO.ValidationItem();
        nameItem.setName("档案命名规则校验");
        nameItem.setType("NAMING");
        try {
            boolean nameValid = validateFileName(requestDTO.getFilePath());
            nameItem.setPassed(nameValid);
            if (!nameValid) {
                nameItem.setErrorMsg("档案命名不符合规则");
                overallValid = false;
            }
        } catch (Exception e) {
            nameItem.setPassed(false);
            nameItem.setErrorMsg("档案命名规则校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(nameItem);

        // 5. 校验业务信息
        HookValidationResultVO.ValidationItem businessItem = new HookValidationResultVO.ValidationItem();
        businessItem.setName("业务信息校验");
        businessItem.setType("BUSINESS");
        try {
            boolean businessValid = validateBusinessInfo(requestDTO);
            businessItem.setPassed(businessValid);
            if (!businessValid) {
                businessItem.setErrorMsg("业务信息校验失败");
                overallValid = false;
            }
        } catch (Exception e) {
            businessItem.setPassed(false);
            businessItem.setErrorMsg("业务信息校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(businessItem);

        // 6. 校验档案分类与内容一致性
        HookValidationResultVO.ValidationItem categoryItem = new HookValidationResultVO.ValidationItem();
        categoryItem.setName("档案分类一致性校验");
        categoryItem.setType("CATEGORY_CONSISTENCY");
        try {
            boolean categoryValid = validateCategoryConsistency(requestDTO.getArchiveType(), requestDTO.getFileType());
            categoryItem.setPassed(categoryValid);
            if (!categoryValid) {
                categoryItem.setErrorMsg("档案分类与内容不一致");
                overallValid = false;
            }
        } catch (Exception e) {
            categoryItem.setPassed(false);
            categoryItem.setErrorMsg("档案分类一致性校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(categoryItem);

        // 7. 校验档案存储位置合规性
        HookValidationResultVO.ValidationItem storageItem = new HookValidationResultVO.ValidationItem();
        storageItem.setName("档案存储位置校验");
        storageItem.setType("STORAGE");
        try {
            boolean storageValid = validateStorageLocation(requestDTO.getFilePath(), requestDTO.getArchiveType());
            storageItem.setPassed(storageValid);
            if (!storageValid) {
                storageItem.setErrorMsg("档案存储位置不符合规则");
                overallValid = false;
            }
        } catch (Exception e) {
            storageItem.setPassed(false);
            storageItem.setErrorMsg("档案存储位置校验失败: " + e.getMessage());
            overallValid = false;
        }
        items.add(storageItem);

        result.setValid(overallValid);
        result.setItems(items);

        // 设置校验建议
        if (overallValid) {
            result.setSuggestion("校验通过，可以进行挂接操作");
        } else {
            StringBuilder suggestion = new StringBuilder("校验失败，建议：");
            for (HookValidationResultVO.ValidationItem item : items) {
                if (!item.isPassed()) {
                    suggestion.append("\n- ").append(item.getName()).append(": ").append(item.getErrorMsg());
                }
            }
            result.setSuggestion(suggestion.toString());
        }

        return result;
    }

    @Override
    public List<HookValidationResultVO> validateBatchHook(List<HookValidationRequestDTO> requestDTOList) {
        List<HookValidationResultVO> results = new ArrayList<>();
        for (HookValidationRequestDTO requestDTO : requestDTOList) {
            results.add(validateSingleHook(requestDTO));
        }
        return results;
    }

    @Override
    public boolean validateFileFormat(String fileType, String filePath) {
        // 允许的文件类型列表
        List<String> allowedTypes = List.of("pdf", "pptx" ,"doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "tif", "tiff");
        
        // 检查文件类型是否在允许列表中
        if (fileType == null || !allowedTypes.contains(fileType.toLowerCase())) {
            return false;
        }
        
        // 检查文件路径是否以正确的扩展名结尾
        if (filePath == null || !filePath.toLowerCase().endsWith("." + fileType.toLowerCase())) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean validateFileIntegrity(String filePath, String expectedMd5) {
        if (filePath == null || expectedMd5 == null) {
            return false;
        }
        
        try {
            String actualMd5 = calculateFileMd5(filePath);
            return expectedMd5.equalsIgnoreCase(actualMd5);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateBusinessInfo(HookValidationRequestDTO requestDTO) {
        // 简单的业务信息校验逻辑，实际项目中可以根据业务规则进行更复杂的校验
        if (requestDTO == null) {
            return false;
        }
        
        // 检查必填字段
        if (requestDTO.getBusinessNo() == null || requestDTO.getBusinessNo().isEmpty()) {
            return false;
        }
        
        if (requestDTO.getResponsiblePerson() == null || requestDTO.getResponsiblePerson().isEmpty()) {
            return false;
        }
        
        if (requestDTO.getDepartment() == null || requestDTO.getDepartment().isEmpty()) {
            return false;
        }
        
        if (requestDTO.getArchiveType() == null || requestDTO.getArchiveType().isEmpty()) {
            return false;
        }
        
        return true;
    }

    @Override
    public ResponseResult<?> manualCorrectHook(Long archiveId, HookValidationRequestDTO requestDTO) {
        // 手动修正逻辑，实际项目中可以根据需要进行实现
        // 这里只是一个简单的示例
        try {
            // 1. 重新校验修正后的信息
            HookValidationResultVO validationResult = validateSingleHook(requestDTO);
            if (!validationResult.isValid()) {
                return ResponseResult.fail("修正后的信息仍不符合校验规则");
            }
            
            // 2. 更新档案信息（实际项目中需要实现）
            // archiveInfoService.updateArchiveInfo(archiveId, requestDTO);
            
            return ResponseResult.success("手动修正成功");
        } catch (Exception e) {
            return ResponseResult.fail("手动修正失败: " + e.getMessage());
        }
    }

    /**
     * 校验档案大小
     * @param filePath 文件路径
     * @return 是否合规
     */
    public boolean validateFileSize(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        try {
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return false;
            }
            
            // 设置文件大小限制为100MB
            long maxSize = 100 * 1024 * 1024;
            return file.length() <= maxSize;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验档案命名规则
     * @param filePath 文件路径
     * @return 是否合规
     */
    public boolean validateFileName(String filePath) {
        if (filePath == null) {
            return false;
        }
        
        // 获取文件名
        String fileName = new File(filePath).getName();
        
        // 移除文件扩展名
        String nameWithoutExt = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
        
        // 定义命名规则：只能包含字母、数字、下划线、连字符和中文
        String regex = "^[a-zA-Z0-9_\u4e00-\u9fa5-]+$";
        Pattern pattern = Pattern.compile(regex);
        
        // 检查文件名长度（2-50个字符）
        if (nameWithoutExt.length() < 2 || nameWithoutExt.length() > 50) {
            return false;
        }
        
        return pattern.matcher(nameWithoutExt).matches();
    }

    /**
     * 校验档案分类与内容一致性
     * @param archiveType 档案分类
     * @param fileType 文件类型
     * @return 是否一致
     */
    public boolean validateCategoryConsistency(String archiveType, String fileType) {
        if (archiveType == null || fileType == null) {
            return false;
        }
        
        // 定义档案分类与允许的文件类型映射
        // 示例映射，实际项目中可以根据业务规则扩展
        Map<String, List<String>> categoryFileTypeMap = new HashMap<>();
        categoryFileTypeMap.put("合同", List.of("pdf", "doc", "docx"));
        categoryFileTypeMap.put("报表", List.of("xls", "xlsx", "pdf"));
        categoryFileTypeMap.put("图片", List.of("jpg", "jpeg", "png", "tif", "tiff"));
        categoryFileTypeMap.put("其他", List.of("pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "tif", "tiff"));
        
        // 检查档案分类是否在映射中
        if (!categoryFileTypeMap.containsKey(archiveType)) {
            return false;
        }
        
        // 检查文件类型是否在允许列表中
        List<String> allowedTypes = categoryFileTypeMap.get(archiveType);
        return allowedTypes.contains(fileType.toLowerCase());
    }

    /**
     * 校验档案存储位置合规性
     * @param filePath 文件路径
     * @param archiveType 档案分类
     * @return 是否合规
     */
    public boolean validateStorageLocation(String filePath, String archiveType) {
        if (filePath == null || archiveType == null) {
            return false;
        }
        
        // 定义存储位置规则：文件路径应包含对应的档案分类目录
        // 示例规则，实际项目中可以根据业务规则扩展
        String lowerFilePath = filePath.toLowerCase();
        String lowerArchiveType = archiveType.toLowerCase();
        
        // 检查文件是否存储在对应分类目录下
        return lowerFilePath.contains(lowerArchiveType);
    }

    /**
     * 计算文件的MD5值
     * @param filePath 文件路径
     * @return MD5值
     * @throws IOException IO异常
     */
    private String calculateFileMd5(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] md5Bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("计算文件MD5失败", e);
        }
    }

    @Override
    public Map<String, Object> generateValidationReport(List<HookValidationResultVO> validationResults) {
        Map<String, Object> report = new HashMap<>();
        
        // 计算总体统计信息
        int totalFiles = validationResults.size();
        int passedFiles = (int) validationResults.stream().filter(HookValidationResultVO::isValid).count();
        int failedFiles = totalFiles - passedFiles;
        double passRate = totalFiles > 0 ? (double) passedFiles / totalFiles * 100 : 0;
        
        // 计算各校验项的统计信息
        Map<String, Integer> itemPassCount = new HashMap<>();
        Map<String, Integer> itemTotalCount = new HashMap<>();
        
        for (HookValidationResultVO result : validationResults) {
            for (HookValidationResultVO.ValidationItem item : result.getItems()) {
                String type = item.getType();
                itemTotalCount.put(type, itemTotalCount.getOrDefault(type, 0) + 1);
                if (item.isPassed()) {
                    itemPassCount.put(type, itemPassCount.getOrDefault(type, 0) + 1);
                }
            }
        }
        
        // 构建各校验项的通过率
        Map<String, Double> itemPassRate = new HashMap<>();
        for (String type : itemTotalCount.keySet()) {
            int total = itemTotalCount.get(type);
            int passed = itemPassCount.getOrDefault(type, 0);
            itemPassRate.put(type, total > 0 ? (double) passed / total * 100 : 0);
        }
        
        // 构建失败详情
        List<Map<String, Object>> failureDetails = new ArrayList<>();
        for (int i = 0; i < validationResults.size(); i++) {
            HookValidationResultVO result = validationResults.get(i);
            if (!result.isValid()) {
                Map<String, Object> failureDetail = new HashMap<>();
                failureDetail.put("fileIndex", i + 1);
                List<Map<String, String>> failedItems = new ArrayList<>();
                for (HookValidationResultVO.ValidationItem item : result.getItems()) {
                    if (!item.isPassed()) {
                        Map<String, String> failedItem = new HashMap<>();
                        failedItem.put("name", item.getName());
                        failedItem.put("type", item.getType());
                        failedItem.put("errorMsg", item.getErrorMsg());
                        failedItems.add(failedItem);
                    }
                }
                failureDetail.put("failedItems", failedItems);
                failureDetails.add(failureDetail);
            }
        }
        
        // 构建报告内容
        report.put("totalFiles", totalFiles);
        report.put("passedFiles", passedFiles);
        report.put("failedFiles", failedFiles);
        report.put("passRate", String.format("%.2f%%", passRate));
        report.put("itemPassRate", itemPassRate);
        report.put("itemPassCount", itemPassCount);
        report.put("itemTotalCount", itemTotalCount);
        report.put("failureDetails", failureDetails);
        report.put("generatedAt", new java.util.Date());
        report.put("reportType", "批量校验报告");
        
        return report;
    }

    @Override
    public Map<String, Object> generateSingleValidationReport(HookValidationResultVO validationResult) {
        Map<String, Object> report = new HashMap<>();
        
        // 构建基本信息
        report.put("valid", validationResult.isValid());
        report.put("suggestion", validationResult.getSuggestion());
        report.put("generatedAt", new java.util.Date());
        report.put("reportType", "单文件校验报告");
        
        // 构建校验项详情
        List<Map<String, Object>> validationDetails = new ArrayList<>();
        for (HookValidationResultVO.ValidationItem item : validationResult.getItems()) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("name", item.getName());
            detail.put("type", item.getType());
            detail.put("passed", item.isPassed());
            detail.put("errorMsg", item.getErrorMsg());
            validationDetails.add(detail);
        }
        
        report.put("validationDetails", validationDetails);
        
        return report;
    }
}