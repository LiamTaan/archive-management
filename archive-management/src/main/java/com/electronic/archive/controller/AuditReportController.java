package com.electronic.archive.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.dto.AuditReportQueryDTO;
import com.electronic.archive.entity.AuditReport;
import com.electronic.archive.service.AuditReportService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 审计报表控制器
 */
@Tag(name = "审计报表管理")
@RestController
@RequestMapping("/audit-report")
public class AuditReportController {

    @Autowired
    private AuditReportService auditReportService;

    /**
     * 获取审计报表列表
     * @param currentPage 当前页码
     * @param pageSize 每页条数
     * @return 审计报表列表
     */
    @Operation(summary = "获取审计报表列表")
    @GetMapping("/list")
    public ResponseResult<PageResult<AuditReport>> getAuditReports(AuditReportQueryDTO queryDTO) {
        
        // 创建查询条件
        LambdaQueryWrapper<AuditReport> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按生成时间倒序排序
        queryWrapper.orderByDesc(AuditReport::getGenerateTime);
        
        // 查询审计报表
        var reportPage = auditReportService.page(queryDTO.toMpPage(), queryWrapper);
        
        // 转换为统一的分页响应格式
        PageResult<AuditReport> result = PageResult.fromMpPage(reportPage);
        
        return ResponseResult.success("获取审计报表列表成功", result);
    }

    /**
     * 获取单个审计报表详情
     * @param id 报表ID
     * @return 审计报表详情
     */
    @Operation(summary = "获取单个审计报表详情")
    @GetMapping("/detail/{id}")
    public ResponseResult<AuditReport> getAuditReportDetail(@PathVariable Long id) {
        AuditReport auditReport = auditReportService.getById(id);
        if (auditReport == null) {
            return ResponseResult.fail("审计报表不存在");
        }
        return ResponseResult.success("获取审计报表详情成功", auditReport);
    }

    /**
     * 生成新的审计报表
     * @param auditReport 审计报表信息
     * @return 生成结果
     */
    @Operation(summary = "生成新的审计报表")
    @PostMapping("/generate")
    public ResponseResult<AuditReport> generateAuditReport(@RequestBody AuditReport auditReport) {
        // 设置默认值
        if (auditReport.getReportData() == null) {
            auditReport.setReportData("{}"); // 初始化为空JSON对象
        }
        if (auditReport.getStatus() == null) {
            auditReport.setStatus(0); // 初始状态：生成中
        }
        
        // 调用报表生成服务（异步执行）
        auditReportService.generateAuditReport(auditReport);
        
        return ResponseResult.success("审计报表生成请求已提交", auditReport);
    }

    /**
     * 删除审计报表
     * @param id 报表ID
     * @return 删除结果
     */
    @Operation(summary = "删除审计报表")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteAuditReport(@PathVariable Long id) {
        boolean deleted = auditReportService.removeById(id);
        if (deleted) {
            return ResponseResult.success("审计报表删除成功");
        } else {
            return ResponseResult.fail("审计报表删除失败");
        }
    }
    
    /**
     * 下载审计报表
     * @param id 报表ID
     * @param response 响应对象
     */
    @Operation(summary = "下载审计报表")
    @GetMapping("/download/{id}")
    public void downloadAuditReport(@PathVariable Long id, HttpServletResponse response) {
        try {
            // 1. 校验报表是否存在
            AuditReport report = auditReportService.getById(id);
            if (report == null) {
                response.sendError(404, "审计报表不存在");
                return;
            }

            // 2. 统一设置响应头（XLSX格式）
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode(report.getReportName() + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            // 3. 解析报表数据
            String reportDataJson = report.getReportData();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> reportData = objectMapper.readValue(reportDataJson, Map.class);

            // 4. 准备Excel数据
            List<List<Object>> data = new ArrayList<>();
            data.add(Arrays.asList("指标", "数值")); // 标题行
            addDataRow(data, "总操作数", reportData.get("total"));
            addDataRow(data, "成功数", reportData.get("success"));
            addDataRow(data, "失败数", reportData.get("fail"));
            addDataRow(data, "角色数", reportData.get("roleCount"));
            addDataRow(data, "权限数", reportData.get("permissionCount"));
            addDataRow(data, "用户数", reportData.get("userCount"));

            // 5. 写入Excel（关键：不指定class，不手动关闭流）
            OutputStream outputStream = response.getOutputStream();
            EasyExcel.write(outputStream)
                    .excelType(ExcelTypeEnum.XLSX) // 明确XLSX
                    .sheet("统计数据")
                    .doWrite(data);
            outputStream.flush(); // 只刷新，不关闭

        } catch (Exception e) {
            e.printStackTrace();
            // 异常时不要再次操作response，避免覆盖流
        }
    }

    private void addDataRow(List<List<Object>> data, String key, Object value) {
        if (value != null) {
            data.add(Arrays.asList(key, value));
        }
    }
}
