package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.ApprovalProcessConfig;
import com.electronic.archive.mapper.ApprovalProcessConfigMapper;
import com.electronic.archive.service.ApprovalProcessConfigService;
import org.springframework.stereotype.Service;

/**
 * 审批流程配置Service实现
 */
@Service
public class ApprovalProcessConfigServiceImpl extends ServiceImpl<ApprovalProcessConfigMapper, ApprovalProcessConfig> implements ApprovalProcessConfigService {
}
