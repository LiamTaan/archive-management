package com.electronic.archive.service;

import com.electronic.archive.entity.SystemLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 系统日志服务接口
 */
public interface SystemLogService extends IService<SystemLog> {
    /**
     * 获取最近的日志记录
     * @param limit 限制条数
     * @return 最近的日志记录列表
     */
    List<SystemLog> getRecentLogs(Integer limit);
    
    /**
     * 保存系统日志
     * @param systemLog 系统日志对象
     * @return 是否保存成功
     */
    boolean saveSystemLog(SystemLog systemLog);
    
    /**
     * 保存系统日志（简化版）
     * @param logLevel 日志级别
     * @param logType 日志类型
     * @param title 日志标题
     * @param content 日志内容
     * @param operateBy 操作人
     * @param operateIp 操作IP
     * @return 是否保存成功
     */
    boolean saveSystemLog(Integer logLevel, Integer logType, String title, String content, String operateBy, String operateIp);
    
    /**
     * 保存异常日志
     * @param title 日志标题
     * @param content 日志内容
     * @param operateBy 操作人
     * @param operateIp 操作IP
     * @param exception 异常信息
     * @return 是否保存成功
     */
    boolean saveExceptionLog(String title, String content, String operateBy, String operateIp, Exception exception);
}