package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.ArchiveCombinationRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 档案组合关系Mapper接口
 */
@Mapper
public interface ArchiveCombinationRelationMapper extends BaseMapper<ArchiveCombinationRelation> {
    
    /**
     * 根据组合ID查询所有档案组合关系
     * @param combinationId 组合ID
     * @return 档案组合关系列表
     */
    List<ArchiveCombinationRelation> selectByCombinationId(@Param("combinationId") Long combinationId);
    
    /**
     * 根据档案ID查询所有档案组合关系
     * @param archiveId 档案ID
     * @return 档案组合关系列表
     */
    List<ArchiveCombinationRelation> selectByArchiveId(@Param("archiveId") Long archiveId);
    
    /**
     * 根据组合ID删除所有档案组合关系
     * @param combinationId 组合ID
     * @return 删除数量
     */
    int deleteByCombinationId(@Param("combinationId") Long combinationId);
}