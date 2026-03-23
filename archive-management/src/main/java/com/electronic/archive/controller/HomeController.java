package com.electronic.archive.controller;


import com.electronic.archive.entity.CollectionLog;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.entity.SystemLog;

import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.service.CollectionLogService;
import com.electronic.archive.service.HangOnLogService;
import com.electronic.archive.service.SystemLogService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 首页控制器
 */
@Tag(name = "首页")
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private ArchiveInfoService archiveInfoService;
    
    @Autowired
    private HangOnLogService hangOnLogService;
    
    @Autowired
    private SysUserService sysUserService;
    

    
    @Autowired
    private SystemLogService systemLogService;
    
    @Autowired
    private CollectionLogService collectionLogService;

    /**
     * 获取首页统计数据
     * @return 统计数据
     */
    @Operation(summary = "获取首页统计数据")
    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取档案总数
        long archiveCount = archiveInfoService.count();
        stats.put("archiveCount", archiveCount);
        
        // 获取挂接数量
        long hangOnCount = hangOnLogService.count();
        stats.put("hangOnCount", hangOnCount);
        
        // 获取系统用户数
        long userCount = sysUserService.count();
        stats.put("userCount", userCount);
        
        // 审批功能已移除，不再显示待处理审批数
        stats.put("pendingApprovals", 0L);
        
        return ResponseResult.success("获取统计数据成功", stats);
    }

    /**
     * 获取最近活动列表
     * @param limit 限制条数
     * @return 活动列表
     */
    @Operation(summary = "获取最近活动列表")
    @GetMapping("/activities")
    public ResponseResult<List<Map<String, Object>>> getRecentActivities(@RequestParam(defaultValue = "5") Integer limit) {
        List<Map<String, Object>> activities = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // 从系统日志中获取最近的记录（使用MyBatis Plus的基础方法）
        List<SystemLog> systemLogs = systemLogService.query()
                .orderByDesc("create_time")
                .last("LIMIT " + limit * 2)
                .list();
        for (SystemLog log : systemLogs) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("time", log.getCreateTime().format(formatter));
            activity.put("content", log.getContent());
            activity.put("createTime", log.getCreateTime());
            activities.add(activity);
        }
        
        // 从采集日志中获取最近的记录（使用MyBatis Plus的基础方法）
        List<CollectionLog> collectionLogs = collectionLogService.query()
                .orderByDesc("operate_time")
                .last("LIMIT " + limit * 2)
                .list();
        for (CollectionLog log : collectionLogs) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("time", log.getOperateTime().format(formatter));
            activity.put("content", String.format("%s%s了一份电子档案", log.getOperateBy(), log.getResult() == 1 ? "新增" : "尝试新增但失败"));
            activity.put("createTime", log.getOperateTime());
            activities.add(activity);
        }
        
        // 从挂接日志中获取最近的记录（使用MyBatis Plus的基础方法）
        List<HangOnLog> hangOnLogs = hangOnLogService.query()
                .orderByDesc("operate_time")
                .last("LIMIT " + limit * 2)
                .list();
        for (HangOnLog log : hangOnLogs) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("time", log.getCreateTime().format(formatter));
            String action = switch (log.getHangOnType()) {
                case 0 -> "完成了一份档案的挂接操作";
                case 1 -> "修改了一份档案的挂接信息";
                case 2 -> "解除了一份档案的挂接";
                default -> "进行了档案挂接相关操作";
            };
            activity.put("content", String.format("%s%s", log.getOperateBy(), action));
            activity.put("createTime", log.getCreateTime());
            activities.add(activity);
        }
        
        // 按时间倒序排序
        activities.sort((a, b) -> ((LocalDateTime) b.get("createTime")).compareTo((LocalDateTime) a.get("createTime")));
        
        // 移除createTime字段，只保留前端需要的字段
        activities.forEach(activity -> activity.remove("createTime"));
        
        // 限制返回数量
        if (activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        
        return ResponseResult.success("获取最近活动成功", activities);
    }
    
    /**
     * 获取待办事项列表
     * @return 待办事项列表
     */
    @Operation(summary = "获取待办事项列表")
    @GetMapping("/todos")
    public ResponseResult<List<Map<String, Object>>> getTodos() {
        // 审批功能已移除，返回空列表
        return ResponseResult.success("获取待办事项成功", new ArrayList<>());
    }
}