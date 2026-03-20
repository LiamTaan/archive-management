package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.CollectionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采集日志Mapper接口
 */
@Mapper
public interface CollectionLogMapper extends BaseMapper<CollectionLog> {
}
