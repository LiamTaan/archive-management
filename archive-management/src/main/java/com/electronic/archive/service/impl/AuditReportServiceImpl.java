package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.AuditReport;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.mapper.AuditReportMapper;
import com.electronic.archive.mapper.ArchiveInfoMapper;
import com.electronic.archive.mapper.HangOnLogMapper;
import com.electronic.archive.service.AuditReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计报表服务实现类
 */
@Service
@Slf4j
public class AuditReportServiceImpl extends ServiceImpl<AuditReportMapper, AuditReport> implements AuditReportService {

    @Autowired
    private AuditReportMapper auditReportMapper;
    
    @Autowired
    private HangOnLogMapper hangOnLogMapper;
    
    @Autowired
    private ArchiveInfoMapper archiveInfoMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 生成审计报表
     * @param report 报表基本信息
     */
    @Override
    @Async
    public void generateAuditReport(AuditReport report) {
        // 设置初始状态
        report.setStatus(0); // 生成中
        
        // 保存初始报表记录
        boolean saved = save(report);
        if (!saved) {
            return;
        }
        
        try {
            // 根据报表类型生成不同的报表数据
            String reportData = "";
            switch (report.getReportType()) {
                case "HANG_ON_AUDIT":
                    reportData = generateHangOnAuditReport(report);
                    break;
                case "COLLECTION_AUDIT":
                    reportData = generateCollectionAuditReport(report);
                    break;
                case "PERMISSION_AUDIT":
                    reportData = generatePermissionAuditReport(report);
                    break;
                case "OPERATION_AUDIT":
                    reportData = generateOperationAuditReport(report);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的报表类型: " + report.getReportType());
            }
            
            // 更新报表数据和状态
            report.setReportData(reportData);
            report.setStatus(1); // 生成成功
            report.setGenerateTime(LocalDateTime.now());
            updateById(report);
        } catch (Exception e) {
            // 更新报表状态为生成失败
            report.setStatus(2); // 生成失败
            updateById(report);
            log.error("生成审计报表失败，报表ID: {}", report.getId(), e);
        }
    }
    
    /**
     * 获取报表数据
     * @param id 报表ID
     * @return 报表数据
     */
    @Override
    public String getReportData(Long id) {
        AuditReport report = getById(id);
        return report != null ? report.getReportData() : "";
    }
    
    /**
     * 生成挂接审计报表
     * @param report 报表基本信息
     * @return 报表数据JSON字符串
     */
    private String generateHangOnAuditReport(AuditReport report) throws Exception {
        // 构建查询条件
        LambdaQueryWrapper<HangOnLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(HangOnLog::getCreateTime, report.getStartTime(), report.getEndTime());
        queryWrapper.eq(HangOnLog::getHangOnType, 0); // 只查询挂接操作
        
        // 查询挂接日志
        List<HangOnLog> hangOnLogs = hangOnLogMapper.selectList(queryWrapper);
        
        // 构建报表数据
        Map<String, Object> reportData = new HashMap<>();
        
        // 统计总数
        int total = hangOnLogs.size();
        int success = (int) hangOnLogs.stream().filter(log -> log.getResult() == 1).count();
        int fail = total - success;
        
        reportData.put("total", total);
        reportData.put("success", success);
        reportData.put("fail", fail);
        
        // 按挂接方式统计
        Map<String, Integer> hangOnTypeMap = new HashMap<>();
        Map<String, Integer> hangOnTypeSuccessMap = new HashMap<>();
        
        for (HangOnLog log : hangOnLogs) {
            // 这里需要从档案信息获取挂接方式，简化处理
            String hangOnType = "手动";
            hangOnTypeMap.put(hangOnType, hangOnTypeMap.getOrDefault(hangOnType, 0) + 1);
            if (log.getResult() == 1) {
                hangOnTypeSuccessMap.put(hangOnType, hangOnTypeSuccessMap.getOrDefault(hangOnType, 0) + 1);
            }
        }
        
        List<Map<String, Object>> byHangOnType = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : hangOnTypeMap.entrySet()) {
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("type", entry.getKey());
            typeStat.put("count", entry.getValue());
            int successCount = hangOnTypeSuccessMap.getOrDefault(entry.getKey(), 0);
            double successRate = entry.getValue() > 0 ? (double) successCount / entry.getValue() * 100 : 0;
            typeStat.put("successRate", Math.round(successRate));
            byHangOnType.add(typeStat);
        }
        reportData.put("byHangOnType", byHangOnType);
        
        // 挂接失败原因分析
        Map<String, Integer> failReasonMap = new HashMap<>();
        for (HangOnLog log : hangOnLogs) {
            if (log.getResult() == 0) {
                String reason = log.getErrorInfo() != null ? log.getErrorInfo() : "未知原因";
                failReasonMap.put(reason, failReasonMap.getOrDefault(reason, 0) + 1);
            }
        }
        
        List<Map<String, Object>> failReasons = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : failReasonMap.entrySet()) {
            Map<String, Object> reasonStat = new HashMap<>();
            reasonStat.put("reason", entry.getKey());
            reasonStat.put("count", entry.getValue());
            failReasons.add(reasonStat);
        }
        reportData.put("failReasons", failReasons);
        
        return objectMapper.writeValueAsString(reportData);
    }
    
    /**
     * 生成采集审计报表
     * @param report 报表基本信息
     * @return 报表数据JSON字符串
     */
    private String generateCollectionAuditReport(AuditReport report) throws Exception {
        // 构建查询条件
        LambdaQueryWrapper<ArchiveInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(ArchiveInfo::getCreateTime, report.getStartTime(), report.getEndTime());
        
        // 查询档案信息
        List<ArchiveInfo> archiveInfos = archiveInfoMapper.selectList(queryWrapper);
        
        // 构建报表数据
        Map<String, Object> reportData = new HashMap<>();
        
        // 统计总数
        int total = archiveInfos.size();
        reportData.put("total", total);
        
        // 按档案类型统计
        Map<String, Integer> archiveTypeMap = new HashMap<>();
        for (ArchiveInfo info : archiveInfos) {
            String archiveType = info.getArchiveType();
            archiveTypeMap.put(archiveType, archiveTypeMap.getOrDefault(archiveType, 0) + 1);
        }
        
        List<Map<String, Object>> byArchiveType = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : archiveTypeMap.entrySet()) {
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("type", entry.getKey());
            typeStat.put("count", entry.getValue());
            byArchiveType.add(typeStat);
        }
        reportData.put("byArchiveType", byArchiveType);
        
        // 按挂接状态统计
        Map<String, Integer> statusMap = new HashMap<>();
        for (ArchiveInfo info : archiveInfos) {
            String status = "";
            switch (info.getStatus()) {
                case 0:
                    status = "未挂接";
                    break;
                case 1:
                    status = "已挂接";
                    break;
                case 2:
                    status = "挂接失败";
                    break;
                default:
                    status = "未知状态";
            }
            statusMap.put(status, statusMap.getOrDefault(status, 0) + 1);
        }
        
        List<Map<String, Object>> byStatus = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            Map<String, Object> statusStat = new HashMap<>();
            statusStat.put("status", entry.getKey());
            statusStat.put("count", entry.getValue());
            byStatus.add(statusStat);
        }
        reportData.put("byStatus", byStatus);
        
        return objectMapper.writeValueAsString(reportData);
    }
    
    /**
     * 生成权限审计报表
     * @param report 报表基本信息
     * @return 报表数据JSON字符串
     */
    private String generatePermissionAuditReport(AuditReport report) throws Exception {
        // 简化实现，实际应查询权限相关表
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("roleCount", 3); // 示例数据
        reportData.put("permissionCount", 15); // 示例数据
        reportData.put("userCount", 1); // 示例数据
        
        // 角色权限分配情况
        List<Map<String, Object>> rolePermissions = new ArrayList<>();
        Map<String, Object> role1 = new HashMap<>();
        role1.put("roleName", "超级管理员");
        role1.put("permissionCount", 15);
        rolePermissions.add(role1);
        
        Map<String, Object> role2 = new HashMap<>();
        role2.put("roleName", "档案管理员");
        role2.put("permissionCount", 8);
        rolePermissions.add(role2);
        
        Map<String, Object> role3 = new HashMap<>();
        role3.put("roleName", "普通用户");
        role3.put("permissionCount", 5);
        rolePermissions.add(role3);
        
        reportData.put("rolePermissions", rolePermissions);
        
        return objectMapper.writeValueAsString(reportData);
    }
    
    /**
     * 生成操作审计报表
     * @param report 报表基本信息
     * @return 报表数据JSON字符串
     */
    private String generateOperationAuditReport(AuditReport report) throws Exception {
        // 构建查询条件
        LambdaQueryWrapper<HangOnLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(HangOnLog::getCreateTime, report.getStartTime(), report.getEndTime());
        
        // 查询操作日志
        List<HangOnLog> operationLogs = hangOnLogMapper.selectList(queryWrapper);
        
        // 构建报表数据
        Map<String, Object> reportData = new HashMap<>();
        
        // 统计总数
        int total = operationLogs.size();
        reportData.put("total", total);
        
        // 按操作类型统计
        Map<String, Integer> operationTypeMap = new HashMap<>();
        for (HangOnLog log : operationLogs) {
            String operationType = "";
            switch (log.getHangOnType()) {
                case 0:
                    operationType = "挂接";
                    break;
                case 1:
                    operationType = "修改";
                    break;
                case 2:
                    operationType = "解除挂接";
                    break;
                default:
                    operationType = "未知操作";
            }
            operationTypeMap.put(operationType, operationTypeMap.getOrDefault(operationType, 0) + 1);
        }
        
        List<Map<String, Object>> byOperationType = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : operationTypeMap.entrySet()) {
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("type", entry.getKey());
            typeStat.put("count", entry.getValue());
            byOperationType.add(typeStat);
        }
        reportData.put("byOperationType", byOperationType);
        
        // 按操作人统计
        Map<String, Integer> operatorMap = new HashMap<>();
        for (HangOnLog log : operationLogs) {
            String operator = log.getOperateBy();
            operatorMap.put(operator, operatorMap.getOrDefault(operator, 0) + 1);
        }
        
        List<Map<String, Object>> byOperator = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : operatorMap.entrySet()) {
            Map<String, Object> operatorStat = new HashMap<>();
            operatorStat.put("operator", entry.getKey());
            operatorStat.put("count", entry.getValue());
            byOperator.add(operatorStat);
        }
        reportData.put("byOperator", byOperator);
        
        return objectMapper.writeValueAsString(reportData);
    }
}
