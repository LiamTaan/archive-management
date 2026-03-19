package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.dto.AuditReportQueryDTO;
import com.electronic.archive.entity.AuditReport;
import com.electronic.archive.service.AuditReportService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.OutputStream;
import jakarta.servlet.http.HttpServletResponse;
import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            // 获取报表数据
            AuditReport report = auditReportService.getById(id);
            if (report == null) {
                response.sendError(404, "审计报表不存在");
                return;
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = new String((report.getReportName() + ".xlsx").getBytes("UTF-8"), "ISO-8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            
            // 获取报表数据
            String reportDataJson = report.getReportData();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> reportData = objectMapper.readValue(reportDataJson, Map.class);
            
            // 创建输出流
            OutputStream outputStream = response.getOutputStream();
            
            // 使用EasyExcel生成Excel文件
            EasyExcel.write(outputStream)                
                // 添加第一个工作表：统计数据
                .sheet("统计数据")
                .doWrite(() -> {
                    List<List<Object>> data = new ArrayList<>();
                    // 添加标题行
                    List<Object> titleRow = new ArrayList<>();
                    titleRow.add("指标");
                    titleRow.add("数值");
                    data.add(titleRow);
                    
                    // 添加统计数据
                    if (reportData.containsKey("total")) {
                        List<Object> row = new ArrayList<>();
                        row.add("总操作数");
                        row.add(reportData.get("total"));
                        data.add(row);
                    }
                    if (reportData.containsKey("success")) {
                        List<Object> row = new ArrayList<>();
                        row.add("成功数");
                        row.add(reportData.get("success"));
                        data.add(row);
                    }
                    if (reportData.containsKey("fail")) {
                        List<Object> row = new ArrayList<>();
                        row.add("失败数");
                        row.add(reportData.get("fail"));
                        data.add(row);
                    }
                    if (reportData.containsKey("roleCount")) {
                        List<Object> row = new ArrayList<>();
                        row.add("角色数");
                        row.add(reportData.get("roleCount"));
                        data.add(row);
                    }
                    if (reportData.containsKey("permissionCount")) {
                        List<Object> row = new ArrayList<>();
                        row.add("权限数");
                        row.add(reportData.get("permissionCount"));
                        data.add(row);
                    }
                    if (reportData.containsKey("userCount")) {
                        List<Object> row = new ArrayList<>();
                        row.add("用户数");
                        row.add(reportData.get("userCount"));
                        data.add(row);
                    }
                    return data;
                });
            
            // 关闭输出流
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(500, "下载失败：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
