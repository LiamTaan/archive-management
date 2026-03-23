package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.ArchiveSystemRelation;

import java.util.List;

/**
 * 档案与目标系统关联关系Service接口
 */
public interface ArchiveSystemRelationService extends IService<ArchiveSystemRelation> {

    /**
     * 根据档案ID获取关联的目标系统列表
     * @param archiveId 档案ID
     * @return 目标系统列表
     */
    List<ArchiveSystemRelation> getByArchiveId(Long archiveId);

    /**
     * 根据目标系统代码获取关联的档案列表
     * @param systemCode 目标系统代码
     * @return 档案列表
     */
    List<ArchiveSystemRelation> getBySystemCode(String systemCode);

    /**
     * 建立档案与目标系统的关联关系
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param systemName 目标系统名称
     * @param hangOnStatus 挂接状态
     * @param hangOnType 挂接方式
     * @param operateBy 操作人
     * @return 关联关系实体
     */
    ArchiveSystemRelation createRelation(Long archiveId, String systemCode, String systemName, Integer hangOnStatus, Integer hangOnType, String operateBy);

    /**
     * 更新档案与目标系统的关联关系状态
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param hangOnStatus 新的挂接状态
     * @return 更新结果
     */
    boolean updateRelationStatus(Long archiveId, String systemCode, Integer hangOnStatus);

    /**
     * 解除档案与目标系统的关联关系
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @return 删除结果
     */
    boolean deleteRelation(Long archiveId, String systemCode);

    /**
     * 获取档案关联的未入库目标系统列表
     * @param archiveId 档案ID
     * @return 未入库目标系统列表
     */
    List<ArchiveSystemRelation> getUnarchivedRelations(Long archiveId);
}
