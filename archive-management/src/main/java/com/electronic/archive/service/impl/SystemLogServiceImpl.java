package com.electronic.archive.service.impl;

import com.electronic.archive.entity.SystemLog;
import com.electronic.archive.mapper.SystemLogMapper;
import com.electronic.archive.service.SystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统日志服务实现类
 */
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    public List<SystemLog> getRecentLogs(Integer limit) {
        // 获取最近的日志记录
        return this.lambdaQuery()
                .orderByDesc(SystemLog::getCreateTime)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public boolean saveSystemLog(SystemLog systemLog) {
        // 设置创建时间
        if (systemLog.getCreateTime() == null) {
            systemLog.setCreateTime(LocalDateTime.now());
        }
        // 保存日志
        return this.save(systemLog);
    }

    @Override
    public boolean saveSystemLog(Integer logLevel, Integer logType, String title, String content, String operateBy, String operateIp) {
        // 创建系统日志对象
        SystemLog systemLog = new SystemLog();
        systemLog.setLogLevel(logLevel);
        systemLog.setLogType(logType);
        systemLog.setTitle(title);
        systemLog.setContent(content);
        systemLog.setOperateBy(operateBy);
        systemLog.setOperateIp(operateIp);
        systemLog.setCreateTime(LocalDateTime.now());
        // 保存日志
        return this.save(systemLog);
    }

    @Override
    public boolean saveExceptionLog(String title, String content, String operateBy, String operateIp, Exception exception) {
        // 创建系统日志对象
        SystemLog systemLog = new SystemLog();
        systemLog.setLogLevel(2); // ERROR级别
        systemLog.setLogType(3); // 异常记录类型
        systemLog.setTitle(title);
        systemLog.setContent(content);
        systemLog.setOperateBy(operateBy);
        systemLog.setOperateIp(operateIp);
        systemLog.setCreateTime(LocalDateTime.now());
        
        // 转换异常信息为字符串
        if (exception != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            systemLog.setExceptionInfo(sw.toString());
        }
        
        // 保存日志
        return this.save(systemLog);
    }
}