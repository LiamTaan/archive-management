package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.CollectionLog;
import com.electronic.archive.mapper.CollectionLogMapper;
import com.electronic.archive.service.CollectionLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 采集日志服务实现类
 */
@Service
public class CollectionLogServiceImpl extends ServiceImpl<CollectionLogMapper, CollectionLog> implements CollectionLogService {

    @Override
    public boolean saveCollectionLog(CollectionLog collectionLog) {
        // 设置操作时间
        if (collectionLog.getOperateTime() == null) {
            collectionLog.setOperateTime(LocalDateTime.now());
        }
        // 保存日志
        return this.save(collectionLog);
    }

    @Override
    public boolean saveCollectionLog(Long archiveId, Integer collectionType, String operateBy, String description, Integer result, String errorInfo) {
        // 创建采集日志对象
        CollectionLog collectionLog = new CollectionLog();
        collectionLog.setArchiveId(archiveId);
        collectionLog.setCollectionType(collectionType);
        collectionLog.setOperateBy(operateBy);
        collectionLog.setDescription(description);
        collectionLog.setResult(result);
        collectionLog.setErrorInfo(errorInfo);
        collectionLog.setOperateTime(LocalDateTime.now());
        // 保存日志
        return this.save(collectionLog);
    }

    @Override
    public boolean saveCollectionLog(Long archiveId, Integer collectionType, String operateBy, String description, Integer result, String errorInfo, 
                                    Integer totalCount, Integer successCount, Integer failCount) {
        // 创建采集日志对象
        CollectionLog collectionLog = new CollectionLog();
        collectionLog.setArchiveId(archiveId);
        collectionLog.setCollectionType(collectionType);
        collectionLog.setOperateBy(operateBy);
        collectionLog.setDescription(description);
        collectionLog.setResult(result);
        collectionLog.setErrorInfo(errorInfo);
        collectionLog.setOperateTime(LocalDateTime.now());
        // 保存日志
        return this.save(collectionLog);
    }
}