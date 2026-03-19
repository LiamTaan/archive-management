package com.electronic.archive.service;

import com.electronic.archive.entity.ArchiveCombinationRelation;

import java.util.List;

/**
 * 档案组合关系服务接口
 */
public interface ArchiveCombinationRelationService {
    /**
     * 保存档案组合关系
     * @param relation 档案组合关系实体
     * @return 是否保存成功
     */
    boolean save(ArchiveCombinationRelation relation);

    /**
     * 批量保存档案组合关系
     * @param relations 档案组合关系列表
     * @return 是否保存成功
     */
    boolean saveBatch(List<ArchiveCombinationRelation> relations);

    /**
     * 根据ID获取档案组合关系
     * @param id 关系ID
     * @return 档案组合关系实体
     */
    ArchiveCombinationRelation getById(Long id);

    /**
     * 根据组合ID获取所有档案组合关系
     * @param combinationId 组合ID
     * @return 档案组合关系列表
     */
    List<ArchiveCombinationRelation> listByCombinationId(Long combinationId);

    /**
     * 根据档案ID获取所有档案组合关系
     * @param archiveId 档案ID
     * @return 档案组合关系列表
     */
    List<ArchiveCombinationRelation> listByArchiveId(Long archiveId);

    /**
     * 删除档案组合关系
     * @param id 关系ID
     * @return 是否删除成功
     */
    boolean removeById(Long id);

    /**
     * 根据组合ID删除所有档案组合关系
     * @param combinationId 组合ID
     * @return 是否删除成功
     */
    boolean removeByCombinationId(Long combinationId);
}