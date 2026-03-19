package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.dto.ArchiveCombinationQueryDTO;
import com.electronic.archive.entity.ArchiveCombination;
import com.electronic.archive.mapper.ArchiveCombinationMapper;
import com.electronic.archive.service.ArchiveCombinationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 档案组合服务实现类
 */
@Service
@Slf4j
public class ArchiveCombinationServiceImpl extends ServiceImpl<ArchiveCombinationMapper, ArchiveCombination> implements ArchiveCombinationService {

    @Override
    public IPage<ArchiveCombination> queryArchiveCombinationByPage(ArchiveCombinationQueryDTO queryDTO) {
        // 创建分页对象
        Page<ArchiveCombination> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<ArchiveCombination> queryWrapper = new LambdaQueryWrapper<>();
        
        // 组合名称模糊查询
        if (queryDTO.getCombinationName() != null && !queryDTO.getCombinationName().isEmpty()) {
            queryWrapper.like(ArchiveCombination::getCombinationName, queryDTO.getCombinationName());
        }
        
        // 组合状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(ArchiveCombination::getStatus, queryDTO.getStatus());
        }
        
        // 组合类型精确查询
        if (queryDTO.getCombinationType() != null && !queryDTO.getCombinationType().isEmpty()) {
            queryWrapper.eq(ArchiveCombination::getCombinationType, queryDTO.getCombinationType());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ArchiveCombination::getCreateTime);
        
        // 执行查询
        return this.page(page, queryWrapper);
    }
}