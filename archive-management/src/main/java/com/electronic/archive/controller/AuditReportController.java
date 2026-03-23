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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
    @ResponseBody
    public void downloadAuditReport(@PathVariable Long id, HttpServletResponse response) {
        // 先重置响应，确保响应头干净
        response.reset();
        boolean hasStreamError = false;
        
        try {
            // 1. 校验报表是否存在
            AuditReport report = auditReportService.getById(id);
            if (report == null) {
                // 返回JSON格式的404错误
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(404);
                response.getWriter().write("{\"code\":404,\"message\":\"审计报表不存在\"}");
                response.getWriter().flush();
                return;
            }

            // 2. 解析报表数据
            String reportDataJson = report.getReportData();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> reportData = objectMapper.readValue(reportDataJson, Map.class);

            // 3. 准备数据列表
            List<List<Object>> dataList = new ArrayList<>();
            
            // 总操作数
            if (reportData.get("total") != null) {
                dataList.add(Arrays.asList("总操作数", reportData.get("total")));
            }
            
            // 按档案类型统计
            if (reportData.get("byArchiveType") != null) {
                dataList.add(Arrays.asList("档案类型统计", ""));
                List<Map<String, Object>> byArchiveType = (List<Map<String, Object>>) reportData.get("byArchiveType");
                for (Map<String, Object> item : byArchiveType) {
                    dataList.add(Arrays.asList("  " + item.get("type"), item.get("count")));
                }
            }
            
            // 按状态统计
            if (reportData.get("byStatus") != null) {
                dataList.add(Arrays.asList("状态统计", ""));
                List<Map<String, Object>> byStatus = (List<Map<String, Object>>) reportData.get("byStatus");
                for (Map<String, Object> item : byStatus) {
                    dataList.add(Arrays.asList("  " + item.get("status"), item.get("count")));
                }
            }

            // 4. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(report.getReportName() + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 5. 获取模板文件URL，处理中文路径问题
            URL templateUrl = this.getClass().getClassLoader().getResource("excel/审计报告.xlsx");
            if (templateUrl == null) {
                // 模板文件不存在，返回JSON错误
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(500);
                response.getWriter().write("{\"code\":500,\"message\":\"Excel模板文件不存在\"}");
                response.getWriter().flush();
                return;
            }
            
            // 6. 处理URL，解决中文路径问题
            String templatePath = null;
            try {
                // 将URL转换为URI，处理中文路径
                templatePath = templateUrl.toURI().getPath();
            } catch (URISyntaxException e) {
                // URI转换失败，尝试使用URL.getPath()
                templatePath = templateUrl.getPath();
                // 手动处理中文路径
                templatePath = URLDecoder.decode(templatePath, "UTF-8");
            }
            
            // 7. 检查模板文件是否存在
            File templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                // 模板文件不存在，返回JSON错误
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(500);
                response.getWriter().write("{\"code\":500,\"message\":\"Excel模板文件不存在\"}");
                response.getWriter().flush();
                return;
            }
            
            // 8. 使用EasyExcel模板写入数据，使用正确的withTemplate方法
            EasyExcel.write(response.getOutputStream())
                    .withTemplate(templatePath)
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet("统计数据")
                    .doWrite(dataList);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // 模板文件找不到，返回JSON错误
            if (!hasStreamError) {
                try {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(500);
                    response.getWriter().write("{\"code\":500,\"message\":\"Excel模板文件不存在\"}");
                    response.getWriter().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    hasStreamError = true;
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            // 流冲突错误，已经无法返回JSON，直接忽略
            hasStreamError = true;
        } catch (Exception e) {
            e.printStackTrace();
            // 其他错误，尝试返回JSON
            if (!hasStreamError) {
                try {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(500);
                    response.getWriter().write("{\"code\":500,\"message\":\"Excel文件生成失败\"}");
                    response.getWriter().flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    hasStreamError = true;
                }
            }
        }
    }

    private void addDataRow(List<List<Object>> data, String key, Object value) {
        if (value != null) {
            data.add(Arrays.asList(key, value));
        }
    }
}
