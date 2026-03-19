package com.electronic.archive.service.impl;

import com.electronic.archive.dto.CombinationHangOnRequestDTO;
import com.electronic.archive.dto.HangOnRequestDTO;
import com.electronic.archive.dto.HookValidationRequestDTO;
import com.electronic.archive.entity.ArchiveCombination;
import com.electronic.archive.entity.ArchiveCombinationRelation;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.entity.Notification;
import com.electronic.archive.mapper.HangOnLogMapper;
import com.electronic.archive.service.*;
import com.electronic.archive.vo.HookValidationResultVO;
import com.electronic.archive.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public boolean autoHangOn(Long archiveId) {
        try {
            // 1. 检查档案是否存在
            ArchiveInfo archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                log.error("自动挂接失败，档案ID：{}，档案不存在", archiveId);
                return false;
            }
            
            // 2. 检查档案是否已挂接
            if (archiveInfo.getStatus() == 1) {
                log.error("自动挂接失败，档案ID：{}，档案已挂接", archiveId);
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
            validationRequest.setOperateBy("system");
            
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

            // 3. 模拟挂接操作
            boolean hookResult = true; // 模拟挂接成功

            // 4. 更新档案状态
            if (hookResult) {
                archiveInfo.setStatus(1); // 已挂接
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);
            } else {
                archiveInfo.setStatus(2); // 挂接失败
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);
            }

            // 5. 记录挂接日志（0-挂接，1-修改，2-解除）
            HangOnLog hangOnLog = createHangOnLog(archiveId, 0, hookResult ? 1 : 2, "system", "auto", "target-system", 
                                          hookResult ? "自动挂接成功" : "自动挂接失败", 
                                          hookResult ? null : "模拟挂接失败");
            hangOnLogMapper.insert(hangOnLog);

            // 发送挂接通知
            Notification notification = new Notification();
            notification.setTitle("档案挂接通知");
            notification.setContent(hookResult ? "档案自动挂接成功" : "档案自动挂接失败");
            notification.setType(1); // 1-挂接通知
            notification.setReceiveBy(archiveInfo.getResponsiblePerson() != null ? archiveInfo.getResponsiblePerson() : "system");
            notification.setBusinessId(archiveId);
            notification.setBusinessType("archive_hang_on");
            notificationService.sendNotification(notification);
            
            log.info("自动挂接档案完成，档案ID：{}，结果：{}", archiveId, hookResult);
            return hookResult;
        } catch (Exception e) {
            log.error("自动挂接档案失败，档案ID：{}", archiveId, e);
            return false;
        }
    }

    @Override
    public boolean manualHangOn(Long archiveId, String systemCode, String operateBy) {
        boolean hookResult = false;
        String errorInfo = null;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                errorInfo = "档案不存在";
                log.error("手动挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } 
            // 2. 检查档案是否已挂接
            else if (archiveInfo.getStatus() == 1) {
                errorInfo = "档案已挂接";
                log.error("手动挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 模拟挂接操作
                hookResult = true; // 模拟挂接成功

                // 4. 更新档案状态
                if (hookResult) {
                    archiveInfo.setStatus(1); // 已挂接
                } else {
                    archiveInfo.setStatus(2); // 挂接失败
                    errorInfo = "模拟挂接失败";
                }
                archiveInfo.setUpdateTime(LocalDateTime.now());
                archiveInfoService.updateById(archiveInfo);
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("手动挂接档案失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
        } finally {
            // 5. 记录挂接日志（0-挂接，1-修改，2-解除）
            HangOnLog hangOnLog = createHangOnLog(archiveId, 0, hookResult ? 1 : 2, operateBy, "manual", systemCode,
                                          hookResult ? "手动挂接成功" : "手动挂接失败", 
                                          errorInfo);
            hangOnLogMapper.insert(hangOnLog);

            // 发送挂接通知
            if (archiveInfo != null) {
                Notification notification = new Notification();
                notification.setTitle("档案挂接通知");
                notification.setContent(hookResult ? "档案手动挂接成功" : "档案手动挂接失败");
                notification.setType(1); // 1-挂接通知
                notification.setReceiveBy(archiveInfo.getResponsiblePerson() != null ? archiveInfo.getResponsiblePerson() : operateBy);
                notification.setBusinessId(archiveId);
                notification.setBusinessType("archive_hang_on");
                notificationService.sendNotification(notification);
            }
            
            log.info("手动挂接档案完成，档案ID：{}，系统代码：{}，结果：{}", archiveId, systemCode, hookResult);
        }
        
        return hookResult;
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
            } 
            // 2. 检查档案是否已挂接
            else if (archiveInfo.getStatus() == 1) {
                errorInfo = "档案已挂接";
                log.error("手动挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 模拟挂接操作
                hookResult = true; // 模拟挂接成功

                // 4. 更新档案状态和关联业务信息
                if (hookResult) {
                    archiveInfo.setStatus(1); // 已挂接
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
                } else {
                    archiveInfo.setStatus(2); // 挂接失败
                    archiveInfo.setUpdateTime(LocalDateTime.now());
                    archiveInfoService.updateById(archiveInfo);
                    errorInfo = "模拟挂接失败";
                }
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("手动挂接档案失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
        } finally {
            // 5. 记录挂接日志（0-挂接，1-修改，2-解除）
            HangOnLog hangOnLog = createHangOnLog(archiveId, 0, hookResult ? 1 : 2, operateBy, "manual", systemCode, 
                                          hookResult ? "手动挂接成功" : "手动挂接失败", 
                                          errorInfo);
            hangOnLogMapper.insert(hangOnLog);

            log.info("手动挂接档案完成，档案ID：{}，系统代码：{}，结果：{}", archiveId, systemCode, hookResult);
        }
        
        return hookResult;
    }

    @Override
    public ResponseResult<Map<String, Object>> batchHangOn(HangOnRequestDTO hangOnRequestDTO) {
        try {
            List<Long> archiveIds = hangOnRequestDTO.getArchiveIds();
            if (archiveIds == null || archiveIds.isEmpty()) {
                return ResponseResult.fail("档案ID列表不能为空");
            }

            String systemCode = hangOnRequestDTO.getSystemCode();
            String operateBy = hangOnRequestDTO.getOperateBy();
            String hangOnMethod = hangOnRequestDTO.getHangOnMethod();
            
            // 获取关联业务信息
            String archiveType = hangOnRequestDTO.getArchiveType();
            String businessNo = hangOnRequestDTO.getBusinessNo();
            String businessType = hangOnRequestDTO.getBusinessType();
            String responsiblePerson = hangOnRequestDTO.getResponsiblePerson();
            String department = hangOnRequestDTO.getDepartment();

            int totalCount = archiveIds.size();
            int successCount = 0;
            int failCount = 0;

            // 批量挂接档案
            for (Long archiveId : archiveIds) {
                boolean result = manualHangOnWithBusinessInfo(archiveId, systemCode, operateBy, 
                                                            archiveType, businessNo, businessType, 
                                                            responsiblePerson, department);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCount", totalCount);
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);
            resultMap.put("description", "批量挂接完成，成功：" + successCount + "，失败：" + failCount);

            log.info("批量挂接档案完成，总数：{}，成功：{}，失败：{}", totalCount, successCount, failCount);
            return ResponseResult.success("批量挂接完成", resultMap);
        } catch (Exception e) {
            log.error("批量挂接档案失败", e);
            return ResponseResult.fail("批量挂接失败");
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
            }
            // 2. 检查档案是否已挂接
            else if (archiveInfo.getStatus() != 1) {
                errorInfo = "档案状态不是已挂接";
                log.error("解除挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 模拟解除挂接操作
                unhookResult = true; // 模拟解除挂接成功

                // 4. 更新档案状态
                if (unhookResult) {
                    archiveInfo.setStatus(2); // 未挂接
                    archiveInfo.setUpdateTime(LocalDateTime.now());
                    archiveInfoService.updateById(archiveInfo);
                } else {
                    errorInfo = "模拟解除挂接失败";
                }
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("解除挂接档案失败，档案ID：{}，系统代码：{}", archiveId, systemCode, e);
        } finally {
            // 5. 记录解除挂接日志（作为特殊的挂接日志）
            HangOnLog hangOnLog = createHangOnLog(archiveId, 2, unhookResult ? 1 : 2, operateBy, "unhook", systemCode,
                                          unhookResult ? "解除挂接成功" : "解除挂接失败", 
                                          errorInfo);
            hangOnLogMapper.insert(hangOnLog);

            // 发送解除挂接通知
            if (archiveInfo != null) {
                Notification notification = new Notification();
                notification.setTitle("档案挂接通知");
                notification.setContent(unhookResult ? "档案解除挂接成功" : "档案解除挂接失败");
                notification.setType(1); // 1-挂接通知
                notification.setReceiveBy(archiveInfo.getResponsiblePerson() != null ? archiveInfo.getResponsiblePerson() : operateBy);
                notification.setBusinessId(archiveId);
                notification.setBusinessType("archive_hang_on");
                notificationService.sendNotification(notification);
            }
            
            log.info("解除挂接档案完成，档案ID：{}，系统代码：{}，结果：{}", archiveId, systemCode, unhookResult);
        }
        
        return unhookResult;
    }

    @Override
    public boolean retryHangOn(Long archiveId, String operateBy) {
        boolean retryResult = false;
        String errorInfo = null;
        ArchiveInfo archiveInfo = null;
        
        try {
            // 1. 检查档案是否存在
            archiveInfo = archiveInfoService.getById(archiveId);
            if (archiveInfo == null) {
                errorInfo = "档案不存在";
                log.error("重试挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            }
            // 2. 检查档案是否为挂接失败状态
            else if (archiveInfo.getStatus() != 2) {
                errorInfo = "档案状态不是挂接失败";
                log.error("重试挂接失败，档案ID：{}，{}", archiveId, errorInfo);
            } else {
                // 3. 模拟重试挂接操作
                retryResult = true; // 模拟重试挂接成功

                // 4. 更新档案状态
                if (retryResult) {
                    archiveInfo.setStatus(1); // 已挂接
                    archiveInfo.setUpdateTime(LocalDateTime.now());
                    archiveInfoService.updateById(archiveInfo);
                } else {
                    errorInfo = "模拟重试挂接失败";
                }
            }
        } catch (Exception e) {
            errorInfo = "系统异常：" + e.getMessage();
            log.error("重试挂接档案失败，档案ID：{}", archiveId, e);
        } finally {
            // 5. 记录重试挂接日志
            HangOnLog hangOnLog = createHangOnLog(archiveId, 0, retryResult ? 1 : 2, operateBy, "retry", "target-system",
                                          retryResult ? "重试挂接成功" : "重试挂接失败", 
                                          errorInfo);
            hangOnLogMapper.insert(hangOnLog);

            // 发送重试挂接通知
            if (archiveInfo != null) {
                Notification notification = new Notification();
                notification.setTitle("档案挂接通知");
                notification.setContent(retryResult ? "档案重试挂接成功" : "档案重试挂接失败");
                notification.setType(1); // 1-挂接通知
                notification.setReceiveBy(archiveInfo.getResponsiblePerson() != null ? archiveInfo.getResponsiblePerson() : operateBy);
                notification.setBusinessId(archiveId);
                notification.setBusinessType("archive_hang_on");
                notificationService.sendNotification(notification);
            }
            
            log.info("重试挂接档案完成，档案ID：{}，结果：{}", archiveId, retryResult);
        }
        
        return retryResult;
    }

    @Override
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
                // 5. 添加所有状态的关系，不仅仅是已挂接状态
            relations.addAll(systemRelations.values());
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
     * 创建挂接日志
     */
    private HangOnLog createHangOnLog(Long archiveId, Integer hangOnType, Integer result, 
                                     String operateBy, String hangOnMethod, String targetSystem, 
                                     String description, String errorInfo) {
        HangOnLog hangOnLog = new HangOnLog();
        hangOnLog.setArchiveId(archiveId);
        hangOnLog.setHangOnType(hangOnType);
        hangOnLog.setResult(result);
        hangOnLog.setOperateBy(operateBy);
        // 将hangOnMethod和targetSystem合并到description中
        String fullDescription = String.format("%s [方式: %s, 目标系统: %s]", description, hangOnMethod, targetSystem);
        hangOnLog.setDescription(fullDescription);
        hangOnLog.setErrorInfo(errorInfo);
        hangOnLog.setCreateTime(LocalDateTime.now());
        return hangOnLog;
    }

    @Override
    public ResponseResult<Map<String, Object>> batchRetryHangOn(List<Long> archiveIds, String operateBy) {
        try {
            if (archiveIds == null || archiveIds.isEmpty()) {
                return ResponseResult.fail("档案ID列表不能为空");
            }

            int totalCount = archiveIds.size();
            int successCount = 0;
            int failCount = 0;
            List<Long> successIds = new ArrayList<>();
            List<Long> failIds = new ArrayList<>();

            // 批量重试挂接档案
            for (Long archiveId : archiveIds) {
                boolean result = retryHangOn(archiveId, operateBy);
                if (result) {
                    successCount++;
                    successIds.add(archiveId);
                } else {
                    failCount++;
                    failIds.add(archiveId);
                }
            }

            // 构建返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("totalCount", totalCount);
            resultMap.put("successCount", successCount);
            resultMap.put("failCount", failCount);
            resultMap.put("successIds", successIds);
            resultMap.put("failIds", failIds);
            resultMap.put("description", "批量重试挂接完成，成功：" + successCount + "，失败：" + failCount);

            log.info("批量重试挂接档案完成，总数：{}，成功：{}，失败：{}", totalCount, successCount, failCount);
            return ResponseResult.success("批量重试挂接完成", resultMap);
        } catch (Exception e) {
            log.error("批量重试挂接档案失败", e);
            return ResponseResult.fail("批量重试挂接失败");
        }
    }

    @Override
    public ResponseResult<Map<String, Object>> combinationHangOn(CombinationHangOnRequestDTO combinationHangOnRequestDTO) {
        try {
            String combinationName = combinationHangOnRequestDTO.getCombinationName();
            String combinationType = combinationHangOnRequestDTO.getCombinationType();
            List<Long> archiveIds = combinationHangOnRequestDTO.getArchiveIds();
            String systemCode = combinationHangOnRequestDTO.getSystemCode();
            String operateBy = combinationHangOnRequestDTO.getOperateBy();
            String hangOnMethod = combinationHangOnRequestDTO.getHangOnMethod();
            
            // 验证参数
            if (archiveIds == null || archiveIds.isEmpty()) {
                return ResponseResult.fail("档案ID列表不能为空");
            }
            
            if (StringUtils.isBlank(combinationName)) {
                return ResponseResult.fail("组合名称不能为空");
            }
            
            // 1. 创建档案组合
            ArchiveCombination combination = new ArchiveCombination();
            combination.setCombinationName(combinationName);
            combination.setCombinationType(combinationType);
            combination.setStatus(0); // 初始状态：未挂接
            combination.setCreateBy(operateBy);
            combination.setCreateTime(LocalDateTime.now());
            combination.setUpdateTime(LocalDateTime.now());
            
            // 保存档案组合
            archiveCombinationService.save(combination);
            Long combinationId = combination.getId();
            
            // 2. 保存档案组合关系
            List<ArchiveCombinationRelation> relations = new ArrayList<>();
            for (int i = 0; i < archiveIds.size(); i++) {
                ArchiveCombinationRelation relation = new ArchiveCombinationRelation();
                relation.setCombinationId(combinationId);
                relation.setArchiveId(archiveIds.get(i));
                relation.setArchiveOrder(i + 1);
                relation.setCreateTime(LocalDateTime.now());
                relations.add(relation);
            }
            archiveCombinationRelationService.saveBatch(relations);
            
            // 3. 批量挂接组合内的档案
            HangOnRequestDTO hangOnRequestDTO = new HangOnRequestDTO();
            hangOnRequestDTO.setArchiveIds(archiveIds);
            hangOnRequestDTO.setSystemCode(systemCode);
            hangOnRequestDTO.setOperateBy(operateBy);
            hangOnRequestDTO.setHangOnMethod(hangOnMethod);
            hangOnRequestDTO.setArchiveType(combinationHangOnRequestDTO.getArchiveType());
            hangOnRequestDTO.setBusinessNo(combinationHangOnRequestDTO.getBusinessNo());
            hangOnRequestDTO.setBusinessType(combinationHangOnRequestDTO.getBusinessType());
            hangOnRequestDTO.setResponsiblePerson(combinationHangOnRequestDTO.getResponsiblePerson());
            hangOnRequestDTO.setDepartment(combinationHangOnRequestDTO.getDepartment());
            
            ResponseResult<Map<String, Object>> batchResult = batchHangOn(hangOnRequestDTO);
            
            // 4. 更新组合状态
            if (batchResult.getCode() == 200) {
                Map<String, Object> resultMap = batchResult.getData();
                if (resultMap != null) {
                    Integer successCount = (Integer) resultMap.get("successCount");
                    if (successCount != null && successCount == archiveIds.size()) {
                        combination.setStatus(1); // 全部挂接成功
                    } else {
                        combination.setStatus(2); // 部分挂接失败
                    }
                } else {
                    combination.setStatus(2); // 部分挂接失败
                }
                combination.setUpdateTime(LocalDateTime.now());
                archiveCombinationService.updateById(combination);
            }
            
            log.info("档案组合挂接完成，组合ID：{}，组合名称：{}，档案数量：{}", combinationId, combinationName, archiveIds.size());
            return batchResult;
        } catch (Exception e) {
            log.error("档案组合挂接失败", e);
            return ResponseResult.fail("档案组合挂接失败");
        }
    }
}