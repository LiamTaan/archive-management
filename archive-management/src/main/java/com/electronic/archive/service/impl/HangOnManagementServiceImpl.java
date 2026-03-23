package com.electronic.archive.service.impl;

import com.electronic.archive.annotation.DataPermission;
import com.electronic.archive.dto.CombinationHangOnRequestDTO;
import com.electronic.archive.dto.HangOnRequestDTO;
import com.electronic.archive.dto.HookValidationRequestDTO;
import com.electronic.archive.constants.RoleConstants;
import com.electronic.archive.entity.*;
import com.electronic.archive.service.ArchiveSystemRelationService;
import com.electronic.archive.mapper.HangOnLogMapper;
import com.electronic.archive.service.*;
import com.electronic.archive.vo.HookValidationResultVO;
import com.electronic.archive.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 挂接管理服务实现类
 */
@Service
@Slf4j
public class HangOnManagementServiceImpl implements HangOnManagementService {
    @Autowired
    private ArchiveInfoService archiveInfoService;

    @Autowired
    private HangOnLogMapper hangOnLogMapper;
    
    @Autowired
    private HookValidationService hookValidationService;
    
    @Autowired
    private ArchiveCombinationService archiveCombinationService;
    
    @Autowired
    private ArchiveCombinationRelationService archiveCombinationRelationService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private InterfaceConfigService interfaceConfigService;

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysDeptService sysDeptService;
    
    @Autowired
    private SysUserRoleService sysUserRoleService;
    
    @Autowired
    private SysRoleService sysRoleService;
    
    @Autowired
    private ApprovalApplyService approvalApplyService;

    @Autowired
    private ArchiveSystemRelationService archiveSystemRelationService;

    /**
     * 检查档案是否已经挂接了指定系统
     * @param archiveId 档案ID
     * @param systemCode 系统代码
     * @return 是否已挂接
     */
    private boolean isArchiveHangedOnSystem(Long archiveId, String systemCode) {
        // 查询该档案的所有挂接日志
        LambdaQueryWrapper<HangOnLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HangOnLog::getArchiveId, archiveId)
                   .eq(HangOnLog::getHangOnType, 0) // 只查询挂接操作
                   .eq(HangOnLog::getResult, 1); // 只查询成功的挂接
        List<HangOnLog> hangOnLogs = hangOnLogMapper.selectList(queryWrapper);
        
        // 检查是否存在相同系统的挂接记录
        for (HangOnLog log : hangOnLogs) {
            String description = log.getDescription();
            if (description != null && description.contains("目标系统: " + systemCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean autoHangOn(Long archiveId, String systemCode) {
        try {
            // 获取当前登录用户的昵称作为责任人
            String responsiblePerson = "admin";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                log.error("自动挂接失败，未找到当前登录用户");
                return false;
            }
            String username = authentication.getName();
            SysUser currentUser = sysUserService.getByUsername(username);
            if (currentUser != null) {
                responsiblePerson = currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getUsername();
            }
            // 1. 检查档案是否存在
            ArchiveInfo archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                log.error("自动挂接失败，档案ID：{}，档案不存在", archiveId);
                return false;
            }

            // 2. 检查是否已挂接相同系统
            if (isArchiveHangedOnSystem(archiveId, systemCode)) {
                log.error("自动挂接失败，档案ID：{}，已挂接该系统", archiveId);
                return false;
            }

            // 2. 挂接前校验
            HookValidationRequestDTO validationRequest = new HookValidationRequestDTO();
            validationRequest.setArchiveId(archiveId);
            validationRequest.setArchiveType(archiveInfo.getArchiveType());
            validationRequest.setBusinessNo(archiveInfo.getBusinessNo());
            validationRequest.setResponsiblePerson(archiveInfo.getResponsiblePerson());
            validationRequest.setDepartment(archiveInfo.getDepartment());
            validationRequest.setFilePath(archiveInfo.getFilePath());
            validationRequest.setFileType(archiveInfo.getFileType());
            validationRequest.setMd5Value(archiveInfo.getMd5Value());
            validationRequest.setOperateBy(responsiblePerson);

            HookValidationResultVO validationResult = hookValidationService.validateSingleHook(validationRequest);
            if (!validationResult.isValid()) {
                log.error("自动挂接失败，档案ID：{}，校验失败：{}", archiveId, validationResult.getSuggestion());

                // 更新档案状态为挂接失败
                archiveInfo.setStatus(2); // 挂接失败
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);

                // 记录挂接日志（0-挂接，1-修改，2-解除）
                HangOnLog hangOnLog = createHangOnLog(archiveId, 0, 2, "system", "auto", "target-system",
                                              "自动挂接失败",
                                              "校验失败：" + validationResult.getSuggestion());
                hangOnLogMapper.insert(hangOnLog);

                return false;
            }

            // 3. 执行真实挂接操作
            boolean hookResult = false;
            try {
                // 获取目标系统配置
                InterfaceConfig interfaceConfig = interfaceConfigService.getByInterfaceCode(systemCode);
                if (interfaceConfig == null) {
                    log.error("自动挂接失败，档案ID：{}，目标系统配置不存在：{}", archiveId, systemCode);
                    return false;
                }

                // 执行挂接逻辑
                // 这里应该调用目标系统的挂接API，根据实际情况实现
                // 例如：hookResult = hookService.callRemoteHook(interfaceConfig, archiveInfo);

                // 临时实现：根据档案ID的奇偶性决定挂接结果，用于演示真实逻辑
                hookResult = true;

                if (hookResult) {
                    log.info("自动挂接成功，档案ID：{}，系统代码：{}", archiveId, systemCode);
                } else {
                    log.error("自动挂接失败，档案ID：{}，系统代码：{}", archiveId, systemCode);
                }
            } catch (Exception e) {
                log.error("自动挂接异常，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
                hookResult = false;
            }

            // 4. 更新档案状态
            if (hookResult) {
                archiveInfo.setStatus(0); // 未挂接（等待审批后才会变为已挂接）
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);

                // 建立档案和目标系统的关联关系，默认未挂接
                InterfaceConfig interfaceConfig = interfaceConfigService.getByInterfaceCode(systemCode);
                if (interfaceConfig != null) {
                    // 创建档案与目标系统的关联关系，默认未挂接状态
                    archiveSystemRelationService.createRelation(
                        archiveId,
                        systemCode,
                        interfaceConfig.getBusinessSystem(),
                        0, // 0-未挂接
                        0, // 0-自动挂接
                        responsiblePerson
                    );
                }

                // 提交审批申请，实现挂接与审批的深度集成
                approvalApplyService.submitApply(archiveId, authentication.getName());
            } else {
                archiveInfo.setStatus(2); // 挂接失败
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);
            }

            // 5. 记录挂接日志（0-挂接，1-修改，2-解除）
            HangOnLog hangOnLog = createHangOnLog(archiveId, 0, hookResult ? 1 : 2, "system", "auto", systemCode,
                                          hookResult ? "自动挂接成功" : "自动挂接失败",
                                          hookResult ? null : "挂接失败");
            hangOnLogMapper.insert(hangOnLog);

            // 发送挂接通知，接收人是当前部门的档案管理员
            try {
                // 获取当前操作人的信息
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
                            Notification notification = new Notification();
                            notification.setTitle("档案挂接通知");
                            notification.setContent(hookResult ? "档案自动挂接成功" : "档案自动挂接失败");
                            notification.setType(hookResult ? 2 : 1); // 1-挂接失败提醒, 2-挂接完成提醒
                            notification.setReceiverId(user.getUserId()); // 临时设置为0，实际项目中应该传入责任人的ID
                            notification.setReceiverName(user.getNickname());
                            notification.setSenderId(1L); // 系统发送
                            notification.setSenderName("系统管理员");
                            notification.setArchiveId(archiveId);
                            notification.setRemark("自动挂接失败");
                            notification.setStatus(0);
                            notificationService.sendNotification(notification);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("发送挂接通知失败", e);
            }

            log.info("自动挂接档案完成，档案ID：{}，结果：{}", archiveId, hookResult);
            return hookResult;
        } catch (Exception e) {
            log.error("自动挂接档案失败，档案ID：{}", archiveId, e);
            return false;
        }
    }

    @Transactional
    @Override
    public boolean manualHangOn(Long archiveId, List<String> systemCode, String operateBy) {
        boolean allSuccess = true;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                log.error("手动挂接失败，档案ID：{}，档案不存在", archiveId);
                return false;
            }
            
            // 2. 设置档案状态为未挂接（等待审批后才会变为已挂接）
            if (archiveInfo.getStatus() != 0) {
                archiveInfo.setStatus(0);
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);
            }
            
            // 3. 遍历每个系统代码执行挂接操作
            for (String sysCode : systemCode) {
                boolean hookResult = false;
                
                try {
                    // 检查是否已挂接相同系统
                    if (isArchiveHangedOnSystem(archiveId, sysCode)) {
                        log.error("手动挂接跳过，档案ID：{}，系统代码：{}，档案已挂接该系统", archiveId, sysCode);
                        continue;
                    }
                    
                    // 获取目标系统配置
                    InterfaceConfig interfaceConfig = interfaceConfigService.getByInterfaceCode(sysCode);
                    if (interfaceConfig == null) {
                        log.error("手动挂接失败，档案ID：{}，系统代码：{}，目标系统配置不存在", archiveId, sysCode);
                        allSuccess = false;
                        continue;
                    }
                    
                    // 创建档案与目标系统的关联关系，默认未挂接状态
                    archiveSystemRelationService.createRelation(
                        archiveId, 
                        sysCode, 
                        interfaceConfig.getBusinessSystem(), 
                        0, // 0-未挂接
                        1, // 1-手动挂接
                        operateBy
                    );
                    
                    hookResult = true;
                } catch (Exception e) {
                    log.error("手动挂接档案失败，档案ID：{}，系统代码：{}", archiveId, sysCode, e);
                    allSuccess = false;
                }
            }
            
            if (allSuccess) {
                // 提交审批申请，实现挂接与审批的深度集成
                approvalApplyService.submitApply(archiveId, operateBy);
                // 发送挂接通知，接收人是当前部门的所有部门负责人
                try {
                    // 获取当前操作人的信息
                    SysUser currentUser = sysUserService.getByUsername(operateBy);
                    if (currentUser != null && currentUser.getDeptId() != null) {
                        // 获取当前部门的部门负责人列表
                        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                        userQueryWrapper.eq(SysUser::getDeptId, currentUser.getDeptId());
                        userQueryWrapper.eq(SysUser::getStatus, 1); // 只获取启用状态的用户
                        List<SysUser> deptLeaders = sysUserService.list(userQueryWrapper);

                        // 3. 筛选出具有部门负责人角色的用户
                        for (SysUser user : deptLeaders) {
                            List<String> userRoleCodes = sysUserRoleService.getRoleCodesByUserId(user.getUserId());
                            if (userRoleCodes.contains(RoleConstants.DEPT_LEADER.getRoleCode())) {
                                // 发送挂接审批通知
                                Notification notification = new Notification();
                                notification.setTitle("档案挂接审批通知");
                                notification.setContent("您有一份新的档案挂接申请需要审批");
                                notification.setType(3); // 3-审批提醒
                                notification.setReceiverId(user.getUserId());
                                notification.setReceiverName(user.getNickname());
                                notification.setSenderId(1L); // 系统发送
                                notification.setSenderName("系统管理员");
                                notification.setArchiveId(archiveId);
                                notification.setRemark("档案挂接审批通知");
                                notification.setStatus(0);
                                notificationService.sendNotification(notification);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("发送挂接通知失败", e);
                }
            }
        } catch (Exception e) {
            log.error("手动挂接档案失败，档案ID：{}", archiveId, e);
            allSuccess = false;
        }
        return allSuccess;
    }
    
    /**
     * 手动挂接档案（带关联业务信息）
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param operateBy 操作人
     * @param archiveType 档案分类
     * @param businessNo 业务单号
     * @param businessType 业务类型
     * @param responsiblePerson 责任人
     * @param department 所属部门
     * @return 是否成功
     */
    public boolean manualHangOnWithBusinessInfo(Long archiveId, String systemCode, String operateBy, 
                                             String archiveType, String businessNo, String businessType, 
                                             String responsiblePerson, String department) {
        boolean hookResult = false;
        String errorInfo = null;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                errorInfo = "档案不存在";
                log.error("手动挂接失败，档案ID：{}，{}", archiveId, errorInfo);
                return hookResult;
            } 
            // 2. 检查是否已挂接相同系统
            else if (isArchiveHangedOnSystem(archiveId, systemCode)) {
                errorInfo = "档案已挂接该系统";
                log.error("手动挂接失败，档案ID：{}，系统代码：{}，{}", archiveId, systemCode, errorInfo);
                return hookResult;
            } else {
                InterfaceConfig interfaceConfig = interfaceConfigService.getByInterfaceCode(systemCode);
                if (interfaceConfig == null) {
                    errorInfo = "目标系统配置不存在: " + systemCode;
                    log.error("手动挂接失败，档案ID：{}，{}", archiveId, errorInfo);
                    return hookResult;
                }

                archiveInfo.setStatus(0); // 未挂接（等待审批后才会变为已挂接）
                archiveInfo.setUpdateTime(LocalDateTime.now());
                // 更新关联业务信息
                if (archiveType != null) {
                    archiveInfo.setArchiveType(archiveType);
                }
                if (businessNo != null) {
                    archiveInfo.setBusinessNo(businessNo);
                }
                if (businessType != null) {
                    archiveInfo.setBusinessType(businessType);
                }
                if (responsiblePerson != null) {
                    archiveInfo.setResponsiblePerson(responsiblePerson);
                }
                if (department != null) {
                    archiveInfo.setDepartment(department);
                }
                archiveInfoService.updateById(archiveInfo);

                // 创建档案与目标系统的关联关系，默认未挂接状态
                archiveSystemRelationService.createRelation(
                        archiveId,
                        systemCode,
                        interfaceConfig.getBusinessSystem(),
                        0, // 0-未挂接
                        1, // 1-手动挂接
                        operateBy
                );

                // 提交审批申请，实现挂接与审批的深度集成
                approvalApplyService.submitApply(archiveId, operateBy);
                // 发送挂接通知，接收人是当前部门的所有部门负责人
                try {
                    // 获取当前操作人的信息
                    SysUser currentUser = sysUserService.getByUsername(operateBy);
                    if (currentUser != null && currentUser.getDeptId() != null) {
                        // 获取当前部门的部门负责人列表
                        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                        userQueryWrapper.eq(SysUser::getDeptId, currentUser.getDeptId());
                        userQueryWrapper.eq(SysUser::getStatus, 1); // 只获取启用状态的用户
                        List<SysUser> deptLeaders = sysUserService.list(userQueryWrapper);

                        // 3. 筛选出具有部门负责人角色的用户
                        for (SysUser user : deptLeaders) {
                            List<String> userRoleCodes = sysUserRoleService.getRoleCodesByUserId(user.getUserId());
                            if (userRoleCodes.contains(RoleConstants.DEPT_LEADER.getRoleCode())) {
                                // 发送挂接审批通知
                                Notification notification = new Notification();
                                notification.setTitle("档案挂接审批通知");
                                notification.setContent("您有一份新的档案挂接申请需要审批");
                                notification.setType(3); // 3-审批提醒
                                notification.setReceiverId(user.getUserId());
                                notification.setReceiverName(user.getNickname());
                                notification.setSenderId(1L); // 系统发送
                                notification.setSenderName("系统管理员");
                                notification.setArchiveId(archiveId);
                                notification.setRemark("档案挂接审批通知");
                                notification.setStatus(0);
                                notificationService.sendNotification(notification);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("发送挂接通知失败", e);
                }
                hookResult = true;
            }
        } catch (Exception e) {
            log.error("批量挂接档案失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
            hookResult = false;
        }
        
        return hookResult;
    }

    @Transactional
    @Override
    public ResponseResult<Map<String, Object>> batchHangOn(HangOnRequestDTO hangOnRequestDTO) {
        try {
            List<Long> archiveIds = hangOnRequestDTO.getArchiveIds();
            if (archiveIds == null || archiveIds.isEmpty()) {
                return ResponseResult.fail("档案ID列表不能为空");
            }

            List<String> systemCodes = hangOnRequestDTO.getSystemCode();
            if (systemCodes == null || systemCodes.isEmpty()) {
                return ResponseResult.fail("目标系统列表不能为空");
            }
            
            String operateBy = hangOnRequestDTO.getOperateBy();
            String hangOnMethod = hangOnRequestDTO.getHangOnMethod();
            
            // 获取关联业务信息
            String archiveType = hangOnRequestDTO.getArchiveType();
            String businessNo = hangOnRequestDTO.getBusinessNo();
            String businessType = hangOnRequestDTO.getBusinessType();
            String responsiblePerson = hangOnRequestDTO.getResponsiblePerson();
            String department = hangOnRequestDTO.getDepartment();

            int totalCount = archiveIds.size() * systemCodes.size();
            int successCount = 0;
            int failCount = 0;

            // 批量挂接档案：每个档案挂接到每个选择的系统
            for (Long archiveId : archiveIds) {
                for (String systemCode : systemCodes) {
                    boolean result = manualHangOnWithBusinessInfo(archiveId, systemCode, operateBy, 
                                                                archiveType, businessNo, businessType, 
                                                                responsiblePerson, department);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                }
            }

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCount", totalCount);
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);
            resultMap.put("description", "已通知挂接审批，成功：" + successCount + "，失败：" + failCount);

            log.info("批量挂接档案发送审批完成，总数：{}，成功：{}，失败：{}", totalCount, successCount, failCount);
            return ResponseResult.success(resultMap);
        } catch (Exception e) {
            log.error("批量挂接档案失败", e);
            return ResponseResult.fail("批量挂接失败：" + e.getMessage());
        }
    }

    @Override
    public boolean unhook(Long archiveId, String systemCode, String operateBy) {
        boolean unhookResult = false;
        String errorInfo = null;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                errorInfo = "档案不存在";
                log.error("解除挂接失败，档案ID：{}，{}", archiveId, errorInfo);
                return unhookResult;
            }
            // 2. 检查档案是否已挂接
            else if (archiveInfo.getStatus() != 1) {
                errorInfo = "档案状态不是已挂接";
                log.error("解除挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 执行真实解除挂接操作
                try {
                    // 获取目标系统配置
                    InterfaceConfig interfaceConfig = interfaceConfigService.getByInterfaceCode(systemCode);
                    if (interfaceConfig == null) {
                        errorInfo = "目标系统配置不存在: " + systemCode;
                        log.error("解除挂接失败，档案ID：{}，{}", archiveId, errorInfo);
                        unhookResult = false;
                    } else {
                        // 执行解除挂接逻辑
                        // 这里应该调用目标系统的解除挂接API，根据实际情况实现
                        // 例如：unhookResult = hookService.callRemoteUnhook(interfaceConfig, archiveInfo);
                        
                        // 临时实现：根据档案ID的奇偶性决定解除挂接结果，用于演示真实逻辑
                        unhookResult = archiveId % 2 == 0;
                        
                        if (unhookResult) {
                            log.info("解除挂接成功，档案ID：{}，系统代码：{}", archiveId, systemCode);
                        } else {
                            log.error("解除挂接失败，档案ID：{}，系统代码：{}", archiveId, systemCode);
                        }
                    }
                } catch (Exception e) {
                    errorInfo = "系统异常：" + e.getMessage();
                    log.error("解除挂接异常，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
                    unhookResult = false;
                }

                // 4. 更新档案状态
                if (unhookResult) {
                    archiveInfo.setStatus(0); // 未挂接
                    archiveInfo.setUpdateTime(LocalDateTime.now());
                    archiveInfoService.updateById(archiveInfo);
                } else {
                    if (errorInfo == null) {
                        errorInfo = "解除挂接失败";
                    }
                }
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("解除挂接档案失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
        } finally {
            // 只有当档案存在时才记录挂接日志
            if (archiveInfo != null) {
                // 5. 记录解除挂接日志（作为特殊的挂接日志）
                HangOnLog hangOnLog = createHangOnLog(archiveId, 2, unhookResult ? 1 : 2, operateBy, "unhook", systemCode,
                                              unhookResult ? "解除挂接成功" : "解除挂接失败", 
                                              errorInfo);
                hangOnLogMapper.insert(hangOnLog);

                // 发送解除挂接通知
                Notification notification = new Notification();
                notification.setTitle("档案挂接通知");
                notification.setContent(unhookResult ? "档案解除挂接成功" : "档案解除挂接失败");
                notification.setType(4); // 4-解除挂接通知
                notification.setReceiverId(1L); // 临时设置为0，实际项目中应该传入责任人的ID
                notification.setReceiverName(archiveInfo.getResponsiblePerson() != null ? archiveInfo.getResponsiblePerson() : operateBy);
                notification.setSenderId(1L); // 系统发送
                notification.setSenderName("系统管理员");
                notification.setArchiveId(archiveId);
                notificationService.sendNotification(notification);
            }
            
            log.info("解除挂接档案完成，档案ID：{}，系统代码：{}，结果：{}", archiveId, systemCode, unhookResult);
        }
        
        return unhookResult;
    }

    @Override
    @DataPermission(department = "current_and_children")
    public List<Map<String, Object>> getHangOnRelations(Long archiveId) {
        try {
            List<Map<String, Object>> relations = new ArrayList<>();
            
            // 1. 查询档案信息
            ArchiveInfo archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                log.warn("获取挂接关系失败，档案不存在，档案ID：{}", archiveId);
                return relations;
            }
            
            // 2. 查询挂接日志，获取所有操作日志
            LambdaQueryWrapper<HangOnLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(HangOnLog::getArchiveId, archiveId)
                       .orderByAsc(HangOnLog::getCreateTime);
            List<HangOnLog> hangOnLogs = hangOnLogMapper.selectList(queryWrapper);
            
            // 3. 处理挂接日志，构建挂接关系
            // 记录每个系统的最新挂接状态
            Map<String, Map<String, Object>> systemRelations = new HashMap<>();
            
            for (HangOnLog log : hangOnLogs) {
                // 从日志描述中提取系统信息
                String systemCode = "default-system";
                String systemName = "默认系统";
                
                // 尝试从日志描述中提取目标系统信息
                String description = log.getDescription();
                if (description != null && description.contains("目标系统: ")) {
                    int systemIndex = description.indexOf("目标系统: ") + 6;
                    int endIndex = description.indexOf("]", systemIndex);
                    if (endIndex > systemIndex) {
                        systemCode = description.substring(systemIndex, endIndex);
                        systemName = systemCode.replace("-", " ");
                    }
                }
                
                // 检查是否已存在该系统的关系
                if (!systemRelations.containsKey(systemCode)) {
                    Map<String, Object> relation = new HashMap<>();
                    relation.put("id", log.getId());
                    relation.put("archiveId", archiveId);
                    relation.put("systemCode", systemCode);
                    relation.put("systemName", systemName);
                    relation.put("hangOnTime", log.getCreateTime());
                    relation.put("operateBy", log.getOperateBy());
                    
                    // 初始状态设为未挂接
                    relation.put("status", "未挂接");
                    
                    systemRelations.put(systemCode, relation);
                }
                
                // 根据操作类型和结果更新状态
                // 0-挂接，1-修改，2-解除
                if (log.getHangOnType() == 0) { // 挂接操作
                    if (log.getResult() == 1) { // 挂接成功
                        systemRelations.get(systemCode).put("status", "已挂接");
                        systemRelations.get(systemCode).put("id", log.getId());
                        systemRelations.get(systemCode).put("hangOnTime", log.getCreateTime());
                        systemRelations.get(systemCode).put("operateBy", log.getOperateBy());
                    }
                } else if (log.getHangOnType() == 2) { // 解除操作
                    systemRelations.get(systemCode).put("status", "已解除");
                    systemRelations.get(systemCode).put("id", log.getId());
                    systemRelations.get(systemCode).put("hangOnTime", log.getCreateTime());
                    systemRelations.get(systemCode).put("operateBy", log.getOperateBy());
                }
            }
            
            // 4. 如果没有挂接日志，但档案状态为已挂接，创建一条基本的挂接关系
            if (hangOnLogs.isEmpty()) {
                if (archiveInfo.getStatus() == 1) {
                    Map<String, Object> relation = new HashMap<>();
                    relation.put("id", archiveInfo.getId());
                    relation.put("archiveId", archiveId);
                    relation.put("systemCode", "default-system");
                    relation.put("systemName", "默认系统");
                    relation.put("hangOnTime", archiveInfo.getHangOnTime());
                    relation.put("status", "已挂接");
                    relation.put("operateBy", "system");
                    relations.add(relation);
                }
            } else {
                // 5. 只添加挂接成功的关系
                for (Map<String, Object> relation : systemRelations.values()) {
                    if ("已挂接".equals(relation.get("status"))) {
                        relations.add(relation);
                    }
                }
            }

            log.info("获取挂接关系完成，档案ID：{}，关系数量：{}", archiveId, relations.size());
            return relations;
        } catch (Exception e) {
            log.error("获取挂接关系失败，档案ID：{}", archiveId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean modifyHangOnRelation(Long archiveId, String systemCode, String operateBy,
                                     String archiveType, String businessNo, String businessType,
                                     String responsiblePerson, String department) {
        boolean modifyResult = false;
        String errorInfo = null;
        String description = null;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                errorInfo = "档案不存在";
                log.error("修改挂接关系失败，档案ID：{}，{}", archiveId, errorInfo);
            }
            // 2. 检查档案是否已挂接
            else if (archiveInfo.getStatus() != 1) {
                errorInfo = "档案状态不是已挂接";
                log.error("修改挂接关系失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 保存原始信息用于日志记录
                String originalArchiveType = archiveInfo.getArchiveType();
                String originalBusinessNo = archiveInfo.getBusinessNo();
                String originalBusinessType = archiveInfo.getBusinessType();
                String originalResponsiblePerson = archiveInfo.getResponsiblePerson();
                String originalDepartment = archiveInfo.getDepartment();

                // 4. 更新档案的关联业务信息
                boolean isModified = false;
                if (archiveType != null && !archiveType.equals(archiveInfo.getArchiveType())) {
                    archiveInfo.setArchiveType(archiveType);
                    isModified = true;
                }
                if (businessNo != null && !businessNo.equals(archiveInfo.getBusinessNo())) {
                    archiveInfo.setBusinessNo(businessNo);
                    isModified = true;
                }
                if (businessType != null && !businessType.equals(archiveInfo.getBusinessType())) {
                    archiveInfo.setBusinessType(businessType);
                    isModified = true;
                }
                if (responsiblePerson != null && !responsiblePerson.equals(archiveInfo.getResponsiblePerson())) {
                    archiveInfo.setResponsiblePerson(responsiblePerson);
                    isModified = true;
                }
                if (department != null && !department.equals(archiveInfo.getDepartment())) {
                    archiveInfo.setDepartment(department);
                    isModified = true;
                }

                // 5. 如果有修改，更新档案信息
                if (isModified) {
                    archiveInfo.setUpdateTime(LocalDateTime.now());
                    archiveInfoService.updateById(archiveInfo);
                    modifyResult = true;

                    // 构建修改描述
                    description = String.format("修改挂接关系，原始信息：[档案分类：%s，业务单号：%s，业务类型：%s，责任人：%s，部门：%s]，新信息：[档案分类：%s，业务单号：%s，业务类型：%s，责任人：%s，部门：%s]",
                                           originalArchiveType, originalBusinessNo, originalBusinessType, originalResponsiblePerson, originalDepartment,
                                           archiveInfo.getArchiveType(), archiveInfo.getBusinessNo(), archiveInfo.getBusinessType(), archiveInfo.getResponsiblePerson(), archiveInfo.getDepartment());
                    
                    log.info("修改挂接关系完成，档案ID：{}，系统代码：{}", archiveId, systemCode);
                } else {
                    errorInfo = "没有需要修改的信息";
                    log.info("修改挂接关系失败，档案ID：{}，系统代码：{}，{}", archiveId, systemCode, errorInfo);
                }
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("修改挂接关系失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
        } finally {
            // 6. 记录修改挂接关系的日志
            HangOnLog hangOnLog = createHangOnLog(archiveId, 1, modifyResult ? 1 : 2, operateBy, "modify", systemCode,
                                          modifyResult ? "修改挂接关系成功" : "修改挂接关系失败", 
                                          modifyResult ? description : errorInfo);
            hangOnLogMapper.insert(hangOnLog);
        }

        return modifyResult;
    }

    /**
     * 校验档案挂接前的业务状态
     */
    @Override
    public ResponseResult<Boolean> checkHangOnValid(List<Long> archiveIds, List<String> systemCode) {
        if (archiveIds == null || archiveIds.isEmpty()) {
            return ResponseResult.fail("档案ID列表不能为空");
        }
        
        if (systemCode == null || systemCode.isEmpty()) {
            return ResponseResult.fail("目标系统代码列表不能为空");
        }
        
        // 遍历每个档案ID
        for (Long archiveId : archiveIds) {
            // 查询该档案是否有正在审核的业务
            List<ApprovalApply> applyList = approvalApplyService.list(
                new LambdaQueryWrapper<ApprovalApply>()
                    .eq(ApprovalApply::getArchiveId, archiveId)
                    .in(ApprovalApply::getApplyStatus, 0, 1, 2) // 0-待审批, 1-部门审核通过, 2-档案复核通过
            );
            
            if (!applyList.isEmpty()) {
                // 获取该档案的系统关系
                List<ArchiveSystemRelation> relations = archiveSystemRelationService.getByArchiveId(archiveId);
                if (!relations.isEmpty()) {
                    StringBuilder message = new StringBuilder();
                    message.append("档案ID: " + archiveId + " 与以下系统存在正在审核的业务: ");
                    
                    for (ArchiveSystemRelation relation : relations) {
                        // 如果该系统在请求的系统列表中
                        if (systemCode.contains(relation.getSystemCode())) {
                            String systemName = interfaceConfigService.getTargetSystemName(relation.getSystemCode());
                            message.append(systemName + "[" + relation.getSystemCode() + "]、");
                        }
                    }
                    
                    if (message.length() > 0) {
                        // 移除最后的顿号
                        message.setLength(message.length() - 1);
                        return ResponseResult.fail(message.toString());
                    }
                }
            }
        }
        
        return ResponseResult.success(true);
    }

    /**
     * 创建挂接日志
     */
    private HangOnLog createHangOnLog(Long archiveId, Integer hangOnType, Integer result, 
                                     String operateBy, String hangOnMethod, String systemCode,
                                     String description, String errorInfo) {
        // 获取接口配置的业务名称
        String targetSystemName = interfaceConfigService.getTargetSystemName(systemCode);
        HangOnLog hangOnLog = new HangOnLog();
        hangOnLog.setArchiveId(archiveId);
        hangOnLog.setHangOnType(hangOnType);
        hangOnLog.setResult(result);
        hangOnLog.setOperateBy(operateBy);
        // 将hangOnMethod和targetSystem合并到description中
        String fullDescription = String.format("%s [方式: %s, 目标编码：%s, 目标系统: %s]", description, hangOnMethod, systemCode,targetSystemName);
        hangOnLog.setDescription(fullDescription);
        hangOnLog.setErrorInfo(errorInfo);
        hangOnLog.setCreateTime(LocalDateTime.now());
        return hangOnLog;
    }
}