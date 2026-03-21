package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.CollectionProgress;

/**
 * 档案采集进度服务接口
 */
public interface CollectionProgressService extends IService<CollectionProgress> {
    
    /**
     * 根据任务ID获取进度信息
     * @param taskId 任务ID
     * @return 进度信息
     */
    CollectionProgress getByTaskId(String taskId);
    
    /**
     * 创建新的采集进度记录
     * @param taskId 任务ID
     * @param collectionType 采集类型
     * @param totalCount 总数量
     * @return 进度信息
     */
    CollectionProgress createProgress(String taskId, Integer collectionType, Integer totalCount);
    
    /**
     * 更新采集进度
     * @param taskId 任务ID
     * @param processedCount 已处理数量
     * @param status 状态
     * @param description 描述信息
     */
    void updateProgress(String taskId, Integer processedCount, Integer status, String description);
    
    /**
     * 完成采集进度
     * @param taskId 任务ID
     * @param successCount 成功数量
     * @param totalCount 总数量
     */
    void completeProgress(String taskId, Integer successCount, Integer totalCount);
    
    /**
     * 失败采集进度
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    void failProgress(String taskId, String errorMessage);
}
