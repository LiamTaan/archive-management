package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.dto.ArchiveQueryDTO;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.mapper.ArchiveInfoMapper;
import com.electronic.archive.service.ArchiveInfoService;
import org.springframework.stereotype.Service;

/**
 * 档案信息服务实现类
 */
@Service
public class ArchiveInfoServiceImpl extends ServiceImpl<ArchiveInfoMapper, ArchiveInfo> implements ArchiveInfoService {

    @Override
    @DataPermission(department = "current_and_children")
    public Page<ArchiveInfo> queryArchiveByPage(ArchiveQueryDTO queryDTO) {
        // 创建分页对象
        Page<ArchiveInfo> page = queryDTO.toMpPage();
        
        // 创建查询条件
        LambdaQueryWrapper<ArchiveInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 不需要明确指定查询字段，MyBatis Plus会查询所有字段，确保所有属性都能正确返回
        
        // 档案ID查询
        if (queryDTO.getId() != null) {
            queryWrapper.eq(ArchiveInfo::getId, queryDTO.getId());
        }
        
        // 文件名模糊查询
        if (queryDTO.getFileName() != null && !queryDTO.getFileName().isEmpty()) {
            queryWrapper.like(ArchiveInfo::getFileName, queryDTO.getFileName());
        }
        
        // 文件类型查询
        if (queryDTO.getFileType() != null && !queryDTO.getFileType().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getFileType, queryDTO.getFileType());
        }
        
        // 档案分类查询
        if (queryDTO.getArchiveType() != null && !queryDTO.getArchiveType().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getArchiveType, queryDTO.getArchiveType());
        }
        
        // 业务单号查询
        if (queryDTO.getBusinessNo() != null && !queryDTO.getBusinessNo().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getBusinessNo, queryDTO.getBusinessNo());
        }
        
        // 业务类型查询
        if (queryDTO.getBusinessType() != null && !queryDTO.getBusinessType().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getBusinessType, queryDTO.getBusinessType());
        }
        
        // 责任人查询
        if (queryDTO.getResponsiblePerson() != null && !queryDTO.getResponsiblePerson().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getResponsiblePerson, queryDTO.getResponsiblePerson());
        }
        
        // 所属部门查询
        if (queryDTO.getDepartment() != null && !queryDTO.getDepartment().isEmpty()) {
            queryWrapper.eq(ArchiveInfo::getDepartment, queryDTO.getDepartment());
        }
        
        // 所属部门ID查询
        if (queryDTO.getDeptId() != null) {
            queryWrapper.eq(ArchiveInfo::getDeptId, queryDTO.getDeptId());
        }
        
        // 所属部门ID列表查询
        if (queryDTO.getDeptIds() != null && !queryDTO.getDeptIds().isEmpty()) {
            queryWrapper.in(ArchiveInfo::getDeptId, queryDTO.getDeptIds());
        }
        
        // 档案状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(ArchiveInfo::getStatus, queryDTO.getStatus());
        }
        
        // 挂接方式查询
        if (queryDTO.getHangOnType() != null) {
            queryWrapper.eq(ArchiveInfo::getHangOnType, queryDTO.getHangOnType());
        }
        
        // 创建时间范围查询
        if (queryDTO.getStartTime() != null && queryDTO.getEndTime() != null) {
            queryWrapper.between(ArchiveInfo::getCreateTime, queryDTO.getStartTime(), queryDTO.getEndTime());
        } else if (queryDTO.getStartTime() != null) {
            queryWrapper.ge(ArchiveInfo::getCreateTime, queryDTO.getStartTime());
        } else if (queryDTO.getEndTime() != null) {
            queryWrapper.le(ArchiveInfo::getCreateTime, queryDTO.getEndTime());
        }
        
        // 文件大小范围查询
        if (queryDTO.getMinFileSize() != null && queryDTO.getMaxFileSize() != null) {
            queryWrapper.between(ArchiveInfo::getFileSize, queryDTO.getMinFileSize(), queryDTO.getMaxFileSize());
        } else if (queryDTO.getMinFileSize() != null) {
            queryWrapper.ge(ArchiveInfo::getFileSize, queryDTO.getMinFileSize());
        } else if (queryDTO.getMaxFileSize() != null) {
            queryWrapper.le(ArchiveInfo::getFileSize, queryDTO.getMaxFileSize());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ArchiveInfo::getCreateTime);
        
        // 执行查询
        return page(page, queryWrapper);
    }
}