package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.dto.ArchiveQueryDTO;
import com.electronic.archive.entity.ArchiveInfo;

/**
 * 档案信息服务接口
 */
public interface ArchiveInfoService extends IService<ArchiveInfo> {

    /**
     * 分页查询档案信息
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<ArchiveInfo> queryArchiveByPage(ArchiveQueryDTO queryDTO);
}