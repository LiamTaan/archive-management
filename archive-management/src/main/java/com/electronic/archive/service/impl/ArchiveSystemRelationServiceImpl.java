package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.ArchiveSystemRelation;
import com.electronic.archive.mapper.ArchiveSystemRelationMapper;
import com.electronic.archive.service.ArchiveSystemRelationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案与目标系统关联关系Service实现类
 */
@Service
public class ArchiveSystemRelationServiceImpl extends ServiceImpl<ArchiveSystemRelationMapper, ArchiveSystemRelation> implements ArchiveSystemRelationService {

    @Override
    public List<ArchiveSystemRelation> getByArchiveId(Long archiveId) {
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getArchiveId, archiveId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ArchiveSystemRelation> getBySystemCode(String systemCode) {
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getSystemCode, systemCode);
        return this.list(queryWrapper);
    }

    @Override
    public ArchiveSystemRelation createRelation(Long archiveId, String systemCode, String systemName, Integer hangOnStatus, Integer hangOnType, String operateBy) {
        // 检查是否已存在关联关系
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getArchiveId, archiveId)
                   .eq(ArchiveSystemRelation::getSystemCode, systemCode);
        ArchiveSystemRelation existingRelation = this.getOne(queryWrapper);
        
        if (existingRelation != null) {
            // 更新现有关联关系
            existingRelation.setHangOnStatus(hangOnStatus);
            existingRelation.setHangOnTime(hangOnStatus == 1 ? LocalDateTime.now() : null);
            existingRelation.setUpdateTime(LocalDateTime.now());
            this.updateById(existingRelation);
            return existingRelation;
        } else {
            // 创建新的关联关系
            ArchiveSystemRelation relation = new ArchiveSystemRelation();
            relation.setArchiveId(archiveId);
            relation.setSystemCode(systemCode);
            relation.setSystemName(systemName);
            relation.setHangOnStatus(hangOnStatus);
            relation.setHangOnTime(hangOnStatus == 1 ? LocalDateTime.now() : null);
            relation.setHangOnType(hangOnType);
            relation.setOperateBy(operateBy);
            relation.setCreateTime(LocalDateTime.now());
            relation.setUpdateTime(LocalDateTime.now());
            this.save(relation);
            return relation;
        }
    }

    @Override
    public boolean updateRelationStatus(Long archiveId, String systemCode, Integer hangOnStatus) {
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getArchiveId, archiveId)
                   .eq(ArchiveSystemRelation::getSystemCode, systemCode);
        
        ArchiveSystemRelation relation = this.getOne(queryWrapper);
        if (relation != null) {
            relation.setHangOnStatus(hangOnStatus);
            relation.setHangOnTime(hangOnStatus == 1 ? LocalDateTime.now() : null);
            relation.setUpdateTime(LocalDateTime.now());
            return this.updateById(relation);
        }
        return false;
    }

    @Override
    public boolean deleteRelation(Long archiveId, String systemCode) {
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getArchiveId, archiveId)
                   .eq(ArchiveSystemRelation::getSystemCode, systemCode);
        return this.remove(queryWrapper);
    }

    @Override
    public List<ArchiveSystemRelation> getUnarchivedRelations(Long archiveId) {
        LambdaQueryWrapper<ArchiveSystemRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArchiveSystemRelation::getArchiveId, archiveId)
                   .ne(ArchiveSystemRelation::getHangOnStatus, 1); // 1表示已挂接，这里查询未挂接的
        return this.list(queryWrapper);
    }
}
