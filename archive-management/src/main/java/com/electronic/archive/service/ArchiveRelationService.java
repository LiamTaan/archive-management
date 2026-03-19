package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.ArchiveRelation;
import com.electronic.archive.vo.ResponseResult;

import java.util.List;

/**
 * 档案关联关系服务接口
 */
public interface ArchiveRelationService extends IService<ArchiveRelation> {
    /**
     * 关联两个档案
     * @param mainArchiveId 主档案ID
     * @param relatedArchiveId 关联档案ID
     * @param relationType 关联类型
     * @param description 关联描述
     * @param createBy 创建人
     * @return 是否成功
     */
    boolean relateArchives(Long mainArchiveId, Long relatedArchiveId, String relationType, String description, String createBy);

    /**
     * 批量关联档案
     * @param mainArchiveId 主档案ID
     * @param relatedArchiveIds 关联档案ID列表
     * @param relationType 关联类型
     * @param description 关联描述
     * @param createBy 创建人
     * @return 关联结果，包含成功和失败的档案ID
     */
    ResponseResult<Object> batchRelateArchives(Long mainArchiveId, List<Long> relatedArchiveIds, String relationType, String description, String createBy);

    /**
     * 获取档案的所有关联关系
     * @param archiveId 档案ID
     * @return 关联关系列表
     */
    List<ArchiveRelation> getArchiveRelations(Long archiveId);

    /**
     * 删除档案关联关系
     * @param relationId 关联ID
     * @return 是否成功
     */
    boolean deleteRelation(Long relationId);

    /**
     * 删除档案的所有关联关系
     * @param archiveId 档案ID
     * @return 是否成功
     */
    boolean deleteAllRelations(Long archiveId);
}