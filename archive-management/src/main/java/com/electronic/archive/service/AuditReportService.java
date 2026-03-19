package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.AuditReport;

/**
 * 审计报表服务接口
 */
public interface AuditReportService extends IService<AuditReport> {
    /**
     * 生成审计报表
     * @param report 报表基本信息
     */
    void generateAuditReport(AuditReport report);
    
    /**
     * 获取报表数据
     * @param id 报表ID
     * @return 报表数据
     */
    String getReportData(Long id);
}
