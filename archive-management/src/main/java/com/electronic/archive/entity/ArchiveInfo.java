package com.electronic.archive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 档案信息表
 */
@Data
@TableName("archive_info")
public class ArchiveInfo {
    /**
     * 档案ID，主键，自增
     */
    @TableId(value = "archive_id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件MD5值
     */
    @TableField("md5_value")
    private String md5Value;

    /**
     * 档案分类
     */
    @TableField("archive_type")
    private String archiveType;

    /**
     * 业务单号
     */
    @TableField("business_no")
    private String businessNo;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 责任人
     */
    @TableField("responsible_person")
    private String responsiblePerson;

    /**
     * 所属部门
     */
    @TableField("department")
    private String department;

    /**
     * 挂接时间
     */
    @TableField("hang_on_time")
    private LocalDateTime hangOnTime;

    /**
     * 挂接方式(0-自动，1-手动)
     */
    @TableField("hang_on_type")
    private Integer hangOnType;

    /**
     * 档案状态(0-未挂接，1-已挂接，2-挂接失败)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}