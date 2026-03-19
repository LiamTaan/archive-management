package com.electronic.archive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.electronic.archive.entity.ArchiveRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 档案关联关系Mapper接口
 */
@Mapper
public interface ArchiveRelationMapper extends BaseMapper<ArchiveRelation> {
    /**
     * 根据档案ID查询所有关联关系
     * @param archiveId 档案ID
     * @return 关联关系列表
     */
    List<ArchiveRelation> selectByArchiveId(@Param("archiveId") Long archiveId);

    /**
     * 根据主档案ID和关联档案ID查询关联关系
     * @param mainArchiveId 主档案ID
     * @param relatedArchiveId 关联档案ID
     * @return 关联关系
     */
    ArchiveRelation selectByMainAndRelatedId(@Param("mainArchiveId") Long mainArchiveId, @Param("relatedArchiveId") Long relatedArchiveId);

    /**
     * 删除档案的所有关联关系
     * @param archiveId 档案ID
     * @return 删除数量
     */
    int deleteByArchiveId(@Param("archiveId") Long archiveId);
}