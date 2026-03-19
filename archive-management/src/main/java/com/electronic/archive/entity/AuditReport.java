package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计报表实体类
 */
@Data
@TableName("audit_report")
public class AuditReport {
    /**
     * 报表ID，主键，自增
     */
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long id;

    /**
     * 报表名称
     */
    @TableField("report_name")
    private String reportName;

    /**
     * 报表类型
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 统计周期类型：day, week, month, quarter, year
     */
    @TableField("period_type")
    private String periodType;

    /**
     * 统计开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 报表数据，JSON格式
     */
    @TableField("report_data")
    private String reportData;

    /**
     * 生成时间
     */
    @TableField("generate_time")
    private LocalDateTime generateTime;

    /**
     * 生成人
     */
    @TableField("generate_by")
    private String generateBy;

    /**
     * 状态：0-生成中，1-生成成功，2-生成失败
     */
    private Integer status;
}