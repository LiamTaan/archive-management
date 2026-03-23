package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.constants.RoleConstants;
import com.electronic.archive.entity.*;
import com.electronic.archive.mapper.ApprovalApplyMapper;
import com.electronic.archive.service.ApprovalApplyService;
import com.electronic.archive.service.ApprovalHistoryService;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.service.ArchiveSystemRelationService;
import com.electronic.archive.service.HangOnLogService;
import com.electronic.archive.service.InterfaceConfigService;
import com.electronic.archive.service.NotificationService;
import com.electronic.archive.service.SysUserRoleService;
import com.electronic.archive.service.SysUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批申请Service实现
 */
@Service
@Slf4j
public class ApprovalApplyServiceImpl extends ServiceImpl<ApprovalApplyMapper, ApprovalApply> implements ApprovalApplyService {

    @Autowired
    private ApprovalHistoryService approvalHistoryService;
    
    @Autowired
    private ArchiveInfoService archiveInfoService;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private InterfaceConfigService interfaceConfigService;
    
    @Autowired
    private HangOnLogService hangOnLogService;
    
    @Autowired
    private ArchiveSystemRelationService archiveSystemRelationService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApply(Long archiveId, String applyBy) {
        // 创建审批申请
        ApprovalApply apply = new ApprovalApply();
        apply.setArchiveId(archiveId);
        apply.setApplyType(1); // 挂接申请
        apply.setApplyStatus(0); // 待审批
        apply.setApplyTime(LocalDateTime.now());
        apply.setApplyBy(applyBy);
        apply.setCreateBy(applyBy); // 设置创建人
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        
        // 获取档案信息，设置部门负责人ID和部门ID
        ArchiveInfo archiveInfo = archiveInfoService.getById(archiveId);
        if (archiveInfo != null) {
            // 设置审批申请的部门ID
            apply.setDeptId(archiveInfo.getDeptId());
            // 更新档案状态为待审批
            archiveInfo.setApprovalStatus(0);
            archiveInfoService.updateById(archiveInfo);
            
            // 根据部门ID查找部门负责人
            Long deptId = archiveInfo.getDeptId();
            if (deptId != null) {
                // 查询该部门所有用户
                List<SysUser> users = sysUserService.list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, deptId));
                for (SysUser user : users) {
                    // 检查用户是否为部门负责人角色
                    List<String> roleCodes = sysUserRoleService.getRoleCodesByUserId(user.getUserId());
                    for (String roleCode : roleCodes) {
                        // 部门负责人角色编码DEPT_LEADER
                        if (RoleConstants.DEPT_LEADER.getRoleCode().equalsIgnoreCase(roleCode)) {
                            apply.setDeptLeaderId(user.getUserId());
                            break;
                        }
                    }
                    if (apply.getDeptLeaderId() != null) {
                        break;
                    }
                }
            }
        }
        
        // 保存申请
        this.save(apply);
        
        return apply.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deptAudit(Long applyId, Long operatorId, String operatorName, boolean pass, String opinion) {
        // 查询申请
        ApprovalApply apply = this.getById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 更新申请状态
        LocalDateTime now = LocalDateTime.now();
        if (pass) {
            apply.setApplyStatus(1); // 部门审核通过
        } else {
            apply.setApplyStatus(4); // 驳回
        }
        apply.setDeptLeaderId(operatorId);
        apply.setDeptAuditTime(now);
        apply.setDeptAuditOpinion(opinion);
        
        // 保存申请
        this.updateById(apply);
        
        // 更新档案状态
        ArchiveInfo archiveInfo = archiveInfoService.getById(apply.getArchiveId());
        if (archiveInfo != null) {
            archiveInfo.setApprovalStatus(apply.getApplyStatus());
            archiveInfoService.updateById(archiveInfo);
        }
        
        // 记录审批历史
        ApprovalHistory history = new ApprovalHistory();
        history.setApplyId(applyId);
        history.setNodeId(1L); // 部门审核节点
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperationTime(now);
        history.setOperationType(pass ? 1 : 2); // 1-通过，2-驳回
        history.setOperationOpinion(opinion);
        history.setCreateTime(LocalDateTime.now());
        approvalHistoryService.save(history);

        // 通知档案管理员进行审核
        try {
            // 获取当前操作人的信息
            SysUser currentUser = sysUserService.getById(operatorId);
            if (currentUser != null && currentUser.getDeptId() != null) {
                // 获取当前部门的部门负责人列表
                LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                userQueryWrapper.eq(SysUser::getDeptId, currentUser.getDeptId());
                userQueryWrapper.eq(SysUser::getStatus, 1); // 只获取启用状态的用户
                List<SysUser> deptLeaders = sysUserService.list(userQueryWrapper);

                // 3. 筛选出具有部门负责人角色的用户
                for (SysUser user : deptLeaders) {
                    List<String> userRoleCodes = sysUserRoleService.getRoleCodesByUserId(user.getUserId());
                    if (userRoleCodes.contains(RoleConstants.ARCHIVE_ADMIN.getRoleCode())) {
                        // 发送挂接审批通知
                        Notification notification = new Notification();
                        notification.setTitle("档案挂接审批通知");
                        notification.setContent("您有一份新的档案挂接申请需要审批");
                        notification.setType(3); // 3-审批提醒
                        notification.setReceiverId(user.getUserId());
                        notification.setReceiverName(user.getNickname());
                        notification.setSenderId(1L); // 系统发送
                        notification.setSenderName("系统管理员");
                        notification.setArchiveId(apply.getArchiveId());
                        notification.setRemark("档案挂接审批通知");
                        notification.setStatus(0);
                        notificationService.sendNotification(notification);
                    }
                }
            }
        } catch (Exception e) {
            log.error("发送挂接通知失败", e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archiveAudit(Long applyId, Long operatorId, String operatorName, boolean pass, String opinion) {
        // 查询申请
        ApprovalApply apply = this.getById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 更新申请状态
        LocalDateTime now = LocalDateTime.now();
        if (pass) {
            apply.setApplyStatus(2); // 档案复核通过
        } else {
            apply.setApplyStatus(4); // 驳回
        }
        apply.setArchiveAdminId(operatorId);
        apply.setArchiveAuditTime(now);
        apply.setArchiveAuditOpinion(opinion);
        
        // 保存申请
        this.updateById(apply);
        
        // 更新档案状态
        ArchiveInfo archiveInfo = archiveInfoService.getById(apply.getArchiveId());
        if (archiveInfo != null) {
            archiveInfo.setApprovalStatus(apply.getApplyStatus());
            archiveInfoService.updateById(archiveInfo);
        }
        
        // 记录审批历史
        ApprovalHistory history = new ApprovalHistory();
        history.setApplyId(applyId);
        history.setNodeId(2L); // 档案复核节点
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setOperationTime(now);
        history.setOperationType(pass ? 1 : 2); // 1-通过，2-驳回
        history.setOperationOpinion(opinion);
        history.setCreateTime(LocalDateTime.now());
        approvalHistoryService.save(history);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finalArchive(Long applyId, String operatorName) {
        // 查询申请
        ApprovalApply apply = this.getById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 获取档案信息
        ArchiveInfo archiveInfo = archiveInfoService.getById(apply.getArchiveId());
        if (archiveInfo == null) {
            return false;
        }
        
        // 调用目标系统API进行实际挂接
        boolean hookResult = false;
        String systemCode = null;
        InterfaceConfig interfaceConfig = null;
        
        try {
            // 通过档案目标系统关联表获取目标系统配置
            List<ArchiveSystemRelation> relations = archiveSystemRelationService.getByArchiveId(apply.getArchiveId());
            if (relations != null && !relations.isEmpty()) {
                // 假设一个档案只关联一个目标系统，实际项目中可能需要处理多个系统
                ArchiveSystemRelation relation = relations.get(0);
                systemCode = relation.getSystemCode();
                // 根据系统代码获取接口配置
                interfaceConfig = interfaceConfigService.getByInterfaceCode(systemCode);
            }

            if (interfaceConfig != null) {
                // 调用目标系统的挂接API
                String apiUrl;
                // 根据传输模式选择不同的接口URL
                if ("DIRECT".equals(interfaceConfig.getTransferMode())) {
                    // 直传模式，使用主接口URL
                    apiUrl = interfaceConfig.getInterfaceUrl();
                } else {
                    // 分片模式或其他模式，使用元数据接口URL
                    apiUrl = interfaceConfig.getMetadataUrl();
                }
                Result result = getResult(interfaceConfig, archiveInfo, apiUrl);

                // 8. 获取响应结果
                hookResult = result.response().getBody() != null ? result.response().getBody() : false;
                log.info("调用目标系统API成功，档案ID：{}，系统代码：{}，传输模式：{}，接口URL：{}，请求方法：{}",
                        apply.getArchiveId(), systemCode, interfaceConfig.getTransferMode(), apiUrl, result.method());

                // 9. 进行响应结果处理
                log.debug("API响应状态：{}，响应体：{}", result.response().getStatusCode(), result.response().getBody());
            } else {
                log.error("未找到目标系统配置，系统代码：{}", systemCode);
                hookResult = false;
            }
        } catch (Exception e) {
            log.error("调用目标系统API失败，档案ID：{}，系统代码：{}", apply.getArchiveId(), systemCode, e);
            // 异常需要通知当前操作人入库异常，并且通知系统管理员
            
            try {
                // 1. 通知当前操作人入库异常
                SysUser currentUser = sysUserService.getByUsername(operatorName);
                Notification operatorNotification = new Notification();
                operatorNotification.setTitle("档案挂接失败通知");
                operatorNotification.setContent(String.format("档案ID：%s，挂接至系统：%s失败，%s，请查看系统日志", apply.getArchiveId(), systemCode,e.getMessage()));
                operatorNotification.setType(1); // 1-挂接失败提醒
                operatorNotification.setReceiverId(currentUser.getUserId()); // 临时设置为0，实际项目中应该传入操作人的ID
                operatorNotification.setReceiverName(currentUser.getNickname());
                operatorNotification.setSenderId(0L); // 系统发送
                operatorNotification.setSenderName("system");
                operatorNotification.setArchiveId(apply.getArchiveId());
                operatorNotification.setRemark("档案挂接异常通知");
                notificationService.sendNotification(operatorNotification);

                // 2. 通知系统管理员
                // 查询所有系统管理员
                Notification adminNotification = new Notification();
                adminNotification.setTitle("系统异常通知");
                adminNotification.setContent(String.format("档案ID：%s，挂接至系统：%s时发生异常，错误信息：%s",
                        apply.getArchiveId(), systemCode, e.getMessage()));
                adminNotification.setType(1); // 2-挂接失败提醒
                adminNotification.setReceiverId(1L); // 临时设置为0，实际项目中应该传入管理员的ID
                adminNotification.setReceiverName("系统管理员");
                adminNotification.setSenderId(1L); // 系统发送
                adminNotification.setSenderName("系统管理员");
                adminNotification.setArchiveId(apply.getArchiveId());
                adminNotification.setRemark("系统异常通知");
                notificationService.sendNotification(adminNotification);
            } catch (Exception notificationException) {
                log.error("发送异常通知失败", notificationException);
            }

            hookResult = false;
        }
        
        // 根据API调用结果处理后续流程
        if (hookResult) {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新申请状态为已入库
            apply.setApplyStatus(3); // 已入库
            apply.setFinalConfirmTime(now);
            apply.setFinalConfirmBy(operatorName);
            apply.setUpdateTime(LocalDateTime.now());
            this.updateById(apply);
            
            // 更新档案状态为已挂接和已入库
            archiveInfo.setApprovalStatus(3); // 已入库
            archiveInfo.setStatus(1); // 已挂接
            archiveInfo.setHangOnTime(now);
            archiveInfoService.updateById(archiveInfo);
            
            // 记录挂接日志
            HangOnLog hangOnLog = new HangOnLog();
            hangOnLog.setArchiveId(apply.getArchiveId());
            hangOnLog.setHangOnType(0); // 0-挂接，1-修改，2-解除
            hangOnLog.setResult(1); // 1-成功，2-失败
            hangOnLog.setOperateBy(operatorName);
            hangOnLog.setDescription("审批通过后最终入库挂接成功，系统代码：" + systemCode);
            hangOnLog.setCreateTime(LocalDateTime.now());
            hangOnLogService.save(hangOnLog);
            
            // 建立或更新档案与目标系统的关联关系
            archiveSystemRelationService.createRelation(
                apply.getArchiveId(),
                systemCode,
                interfaceConfig != null ? interfaceConfig.getBusinessSystem() : systemCode,
                1, // 已挂接状态
                1, // 手动挂接方式
                operatorName
            );
            SysUser currentUser = sysUserService.getByUsername(operatorName);
            Notification operatorNotification = new Notification();
            operatorNotification.setTitle("档案挂接成功通知");
            operatorNotification.setContent(String.format("档案ID：%s，挂接至系统：%s成功。", apply.getArchiveId(), systemCode));
            operatorNotification.setType(2); // 1-挂接失败提醒
            operatorNotification.setReceiverId(currentUser.getUserId()); // 临时设置为0，实际项目中应该传入操作人的ID
            operatorNotification.setReceiverName(currentUser.getNickname());
            operatorNotification.setSenderId(1L); // 系统发送
            operatorNotification.setSenderName("系统管理员");
            operatorNotification.setArchiveId(apply.getArchiveId());
            operatorNotification.setRemark("档案挂接成功通知");
            notificationService.sendNotification(operatorNotification);
            return true;
        } else {
            // 调用API失败，不更新状态
            log.error("最终入库失败，目标系统API调用失败，档案ID：{}", apply.getArchiveId());
            return false;
        }
    }

    private Result getResult(InterfaceConfig interfaceConfig, ArchiveInfo archiveInfo, String apiUrl) {
        // 1. 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // 2. 解析并添加配置的请求头
        if (StringUtils.hasText(interfaceConfig.getRequestHeaders())) {
            try {
                // 使用Jackson解析JSON格式的请求头
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> customHeaders = objectMapper.readValue(
                        interfaceConfig.getRequestHeaders(), new TypeReference<Map<String, String>>() {});
                // 将解析后的请求头添加到HttpHeaders中
                customHeaders.forEach(headers::set);
                log.info("成功解析并添加自定义请求头");
            } catch (Exception e) {
                log.warn("解析请求头失败，使用默认请求头：{}", e.getMessage());
            }
        }

        // 3. 添加认证信息（如果配置了）
        if (StringUtils.hasText(interfaceConfig.getSecretKey())) {
            // 如果请求头中没有Authorization，则添加
            if (!headers.containsKey("Authorization")) {
                headers.set("Authorization", "Bearer " + interfaceConfig.getSecretKey());
            }
        }

        // 4. 处理请求参数和请求体
        HttpEntity<?> entity;

        // 获取请求方法，默认POST
        String method = interfaceConfig.getRequestMethod() != null ? interfaceConfig.getRequestMethod() : "POST";

        // 5. 处理请求参数映射
        Object requestBody = archiveInfo;

        // 如果配置了请求参数映射，则根据映射转换请求体
        if (StringUtils.hasText(interfaceConfig.getRequestParams())) {
            try {
                // 使用Jackson解析JSON格式的请求参数映射
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> paramsMap = objectMapper.readValue(
                        interfaceConfig.getRequestParams(), new TypeReference<Map<String, String>>() {});

                // 如果是GET请求，参数会作为URL参数处理，由restTemplate自动处理
                if (!"GET".equalsIgnoreCase(method)) {
                    // 对于非GET请求，可以根据参数映射转换请求体
                    // 这里简单处理，实际项目中可能需要更复杂的参数映射逻辑
                    log.info("使用请求参数映射：{}", paramsMap);
                }
            } catch (Exception e) {
                log.warn("解析请求参数失败，使用原始请求体：{}", e.getMessage());
            }
        }

        // 6. 构建请求实体
        if ("GET".equalsIgnoreCase(method)) {
            // GET请求，不需要请求体
            entity = new HttpEntity<>(headers);
        } else {
            // POST/PUT等请求，使用处理后的请求体
            entity = new HttpEntity<>(requestBody, headers);
        }

        // 7. 调用真实的API
        ResponseEntity<Boolean> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.valueOf(method),
                entity,
                Boolean.class
        );
        Result result = new Result(method, response);
        return result;
    }

    private record Result(String method, ResponseEntity<Boolean> response) {
    }

    @Override
    @DataPermission(department = "current_and_children")
    public Page<ApprovalApply> pageWithPermission(int page, int size, LambdaQueryWrapper<ApprovalApply> queryWrapper) {
        // 创建分页对象
        Page<ApprovalApply> pageParam = new Page<>(page, size);
        // 执行分页查询
        return this.page(pageParam, queryWrapper);
    }
}
