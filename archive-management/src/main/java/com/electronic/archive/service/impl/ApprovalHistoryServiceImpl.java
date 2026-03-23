package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.ApprovalHistory;
import com.electronic.archive.mapper.ApprovalHistoryMapper;
import com.electronic.archive.service.ApprovalHistoryService;
import org.springframework.stereotype.Service;

/**
 * 审批历史Service实现
 */
@Service
public class ApprovalHistoryServiceImpl extends ServiceImpl<ApprovalHistoryMapper, ApprovalHistory> implements ApprovalHistoryService {
}
