package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.CollectionProgress;
import com.electronic.archive.mapper.CollectionProgressMapper;
import com.electronic.archive.service.CollectionProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 档案采集进度服务实现类
 */
@Service
@Slf4j
public class CollectionProgressServiceImpl extends ServiceImpl<CollectionProgressMapper, CollectionProgress> implements CollectionProgressService {
    
    @Override
    public CollectionProgress getByTaskId(String taskId) {
        return baseMapper.selectByTaskId(taskId);
    }
    
    @Override
    public CollectionProgress createProgress(String taskId, Integer collectionType, Integer totalCount) {
        CollectionProgress progress = new CollectionProgress();
        progress.setTaskId(taskId);
        progress.setCollectionType(collectionType);
        progress.setProgress(0);
        progress.setProcessedCount(0);
        progress.setTotalCount(totalCount);
        progress.setStatus(0);
        progress.setDescription("采集任务开始");
        progress.setCreateTime(LocalDateTime.now());
        progress.setUpdateTime(LocalDateTime.now());
        
        save(progress);
        return progress;
    }
    
    @Override
    public void updateProgress(String taskId, Integer processedCount, Integer status, String description) {
        CollectionProgress progress = getByTaskId(taskId);
        if (progress != null) {
            int totalCount = progress.getTotalCount();
            int newProgress = totalCount > 0 ? (int) Math.round((double) processedCount / totalCount * 100) : 0;
            
            progress.setProgress(newProgress);
            progress.setProcessedCount(processedCount);
            progress.setStatus(status);
            progress.setDescription(description);
            progress.setUpdateTime(LocalDateTime.now());
            
            updateById(progress);
        }
    }
    
    @Override
    public void completeProgress(String taskId, Integer successCount, Integer totalCount) {
        CollectionProgress progress = getByTaskId(taskId);
        if (progress != null) {
            progress.setProgress(100);
            progress.setProcessedCount(totalCount);
            progress.setStatus(1);
            progress.setDescription(String.format("采集完成，成功：%d，失败：%d", successCount, totalCount - successCount));
            progress.setUpdateTime(LocalDateTime.now());
            
            updateById(progress);
        }
    }
    
    @Override
    public void failProgress(String taskId, String errorMessage) {
        CollectionProgress progress = getByTaskId(taskId);
        if (progress != null) {
            progress.setStatus(2);
            progress.setDescription(String.format("采集失败：%s", errorMessage));
            progress.setUpdateTime(LocalDateTime.now());
            
            updateById(progress);
        }
    }
}
