package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.CollectionLog;

/**
 * 采集日志服务接口
 */
public interface CollectionLogService extends IService<CollectionLog> {
    /**
     * 保存采集日志
     * @param collectionLog 采集日志对象
     * @return 是否保存成功
     */
    boolean saveCollectionLog(CollectionLog collectionLog);
    
    /**
     * 保存采集日志（简化版）
     * @param archiveId 档案ID
     * @param collectionType 采集方式
     * @param operateBy 操作人
     * @param description 操作内容
     * @param result 采集结果
     * @param errorInfo 失败原因
     * @return 是否保存成功
     */
    boolean saveCollectionLog(Long archiveId, Integer collectionType, String operateBy, String description, Integer result, String errorInfo);
    
    /**
     * 保存采集日志（完整版，支持统计字段）
     * @param archiveId 档案ID
     * @param collectionType 采集方式
     * @param operateBy 操作人
     * @param description 操作内容
     * @param result 采集结果
     * @param errorInfo 失败原因
     * @param totalCount 采集总数
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @return 是否保存成功
     */
    boolean saveCollectionLog(Long archiveId, Integer collectionType, String operateBy, String description, Integer result, String errorInfo, 
                             Integer totalCount, Integer successCount, Integer failCount);
}