package com.electronic.archive.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.dto.ArchiveCombinationQueryDTO;
import com.electronic.archive.entity.ArchiveCombination;

/**
 * 档案组合服务接口
 */
public interface ArchiveCombinationService extends IService<ArchiveCombination> {
    /**
     * 分页查询档案组合
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<ArchiveCombination> queryArchiveCombinationByPage(ArchiveCombinationQueryDTO queryDTO);
}