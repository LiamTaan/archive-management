package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.ApprovalNode;
import com.electronic.archive.mapper.ApprovalNodeMapper;
import com.electronic.archive.service.ApprovalNodeService;
import org.springframework.stereotype.Service;

/**
 * 审批节点Service实现
 */
@Service
public class ApprovalNodeServiceImpl extends ServiceImpl<ApprovalNodeMapper, ApprovalNode> implements ApprovalNodeService {
}
