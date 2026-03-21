package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.CollectionProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 档案采集进度Mapper接口
 */
@Mapper
public interface CollectionProgressMapper extends BaseMapper<CollectionProgress> {
    
    /**
     * 根据任务ID查询进度信息
     * @param taskId 任务ID
     * @return 进度信息
     */
    CollectionProgress selectByTaskId(String taskId);
}
