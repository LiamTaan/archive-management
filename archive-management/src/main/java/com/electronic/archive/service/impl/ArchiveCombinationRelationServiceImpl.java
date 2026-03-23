package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.entity.ArchiveCombinationRelation;
import com.electronic.archive.mapper.ArchiveCombinationRelationMapper;
import com.electronic.archive.service.ArchiveCombinationRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 档案组合关系服务实现类
 */
@Service
@Slf4j
public class ArchiveCombinationRelationServiceImpl extends ServiceImpl<ArchiveCombinationRelationMapper, ArchiveCombinationRelation> implements ArchiveCombinationRelationService {
    @Override
    public boolean save(ArchiveCombinationRelation relation) {
        try {
            boolean result = baseMapper.insert(relation) > 0;
            if (result) {
                log.info("保存档案组合关系成功，组合ID：{}，档案ID：{}", relation.getCombinationId(), relation.getArchiveId());
            } else {
                log.error("保存档案组合关系失败，组合ID：{}，档案ID：{}", relation.getCombinationId(), relation.getArchiveId());
            }
            return result;
        } catch (Exception e) {
            log.error("保存档案组合关系失败", e);
            return false;
        }
    }

    @Override
    public boolean saveBatch(List<ArchiveCombinationRelation> relations) {
        try {
            boolean result = true;
            for (ArchiveCombinationRelation relation : relations) {
                if (baseMapper.insert(relation) <= 0) {
                    result = false;
                    log.error("批量保存档案组合关系失败，组合ID：{}，档案ID：{}", relation.getCombinationId(), relation.getArchiveId());
                } else {
                    log.info("批量保存档案组合关系，组合ID：{}，档案ID：{}", relation.getCombinationId(), relation.getArchiveId());
                }
            }
            if (result) {
                log.info("批量保存档案组合关系成功，共保存：{}条", relations.size());
            }
            return result;
        } catch (Exception e) {
            log.error("批量保存档案组合关系失败", e);
            return false;
        }
    }

    @Override
    public ArchiveCombinationRelation getById(Long id) {
        try {
            ArchiveCombinationRelation relation = baseMapper.selectById(id);
            if (relation != null) {
                log.info("根据ID获取档案组合关系成功，关系ID：{}", id);
            } else {
                log.error("根据ID获取档案组合关系失败，关系ID：{}", id);
            }
            return relation;
        } catch (Exception e) {
            log.error("根据ID获取档案组合关系失败", e);
            return null;
        }
    }

    @Override
    @DataPermission(department = "current_and_children")
    public List<ArchiveCombinationRelation> listByCombinationId(Long combinationId) {
        try {
            List<ArchiveCombinationRelation> relations = baseMapper.selectByCombinationId(combinationId);
            log.info("根据组合ID获取档案组合关系成功，组合ID：{}，返回数量：{}", combinationId, relations.size());
            return relations;
        } catch (Exception e) {
            log.error("根据组合ID获取档案组合关系失败", e);
            return List.of();
        }
    }

    @Override
    @DataPermission(department = "current_and_children")
    public List<ArchiveCombinationRelation> listByArchiveId(Long archiveId) {
        try {
            List<ArchiveCombinationRelation> relations = baseMapper.selectByArchiveId(archiveId);
            log.info("根据档案ID获取档案组合关系成功，档案ID：{}，返回数量：{}", archiveId, relations.size());
            return relations;
        } catch (Exception e) {
            log.error("根据档案ID获取档案组合关系失败", e);
            return List.of();
        }
    }

    @Override
    public boolean removeById(Long id) {
        try {
            boolean result = baseMapper.deleteById(id) > 0;
            if (result) {
                log.info("删除档案组合关系成功，关系ID：{}", id);
            } else {
                log.error("删除档案组合关系失败，关系ID：{}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除档案组合关系失败", e);
            return false;
        }
    }

    @Override
    public boolean removeByCombinationId(Long combinationId) {
        try {
            int deleteCount = baseMapper.deleteByCombinationId(combinationId);
            log.info("根据组合ID删除档案组合关系成功，组合ID：{}，删除数量：{}", combinationId, deleteCount);
            return deleteCount >= 0;
        } catch (Exception e) {
            log.error("根据组合ID删除档案组合关系失败", e);
            return false;
        }
    }
}