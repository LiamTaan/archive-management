package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electronic.archive.dto.ArchivePushDTO;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.mapper.HangOnLogMapper;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.service.ArchivePushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 档案推送服务实现类
 * 处理业务系统推送的档案信息，自动完成挂接
 */
@Service
@Slf4j
public class ArchivePushServiceImpl implements ArchivePushService {
    @Autowired
    private ArchiveInfoService archiveInfoService;

    @Autowired
    private HangOnLogMapper hangOnLogMapper;



    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean pushAndHookArchive(ArchivePushDTO pushDTO) {
        try {
            // 1. 检查是否已挂接相同系统
            String systemCode = pushDTO.getSystemCode();
            // 先查询该业务单号对应的档案ID（假设业务单号唯一对应档案）
            // 注意：这里可能需要根据实际业务逻辑调整，比如通过businessNo或其他字段查询档案
            // 暂时使用简单的实现，假设每个业务单号对应一个档案
            LambdaQueryWrapper<ArchiveInfo> archiveQueryWrapper = new LambdaQueryWrapper<>();
            archiveQueryWrapper.eq(ArchiveInfo::getBusinessNo, pushDTO.getBusinessNo());
            ArchiveInfo existingArchive = archiveInfoService.getOne(archiveQueryWrapper);
            
            if (existingArchive != null) {
                // 检查该档案是否已挂接相同系统
                LambdaQueryWrapper<HangOnLog> logQueryWrapper = new LambdaQueryWrapper<>();
                logQueryWrapper.eq(HangOnLog::getArchiveId, existingArchive.getId())
                           .eq(HangOnLog::getHangOnType, 0) // 只查询挂接操作
                           .eq(HangOnLog::getResult, 1); // 只查询成功的挂接
                List<HangOnLog> hangOnLogs = hangOnLogMapper.selectList(logQueryWrapper);
                
                // 检查是否存在相同系统的挂接记录
                for (HangOnLog hangOnLog : hangOnLogs) {
                    String description = hangOnLog.getDescription();
                    if (description != null && description.contains("目标系统: " + systemCode)) {
                        log.error("业务系统档案推送挂接失败，业务单号：{}，档案已挂接该系统", pushDTO.getBusinessNo());
                        return false;
                    }
                }
            }

            // 2. 转换为档案信息实体
            ArchiveInfo archiveInfo = new ArchiveInfo();
            archiveInfo.setFileName(pushDTO.getFileName());
            archiveInfo.setFilePath(pushDTO.getFilePath());
            archiveInfo.setFileType(pushDTO.getFileType());
            archiveInfo.setFileSize(pushDTO.getFileSize());
            archiveInfo.setMd5Value(pushDTO.getMd5Value());
            archiveInfo.setArchiveType(pushDTO.getArchiveType());
            archiveInfo.setBusinessNo(pushDTO.getBusinessNo());
            archiveInfo.setBusinessType(pushDTO.getBusinessType());
            archiveInfo.setResponsiblePerson(pushDTO.getResponsiblePerson());
            archiveInfo.setHangOnType(0); // 0-自动挂接
            archiveInfo.setStatus(1); // 1-已挂接
            archiveInfo.setRemark(pushDTO.getRemark());
            archiveInfo.setCreateTime(LocalDateTime.now());
            archiveInfo.setUpdateTime(LocalDateTime.now());

            // 3. 如果办理时间不为空，解析并设置
            if (pushDTO.getHandleTime() != null && !pushDTO.getHandleTime().isEmpty()) {
                archiveInfo.setHangOnTime(LocalDateTime.parse(pushDTO.getHandleTime(), DATE_TIME_FORMATTER));
            } else {
                archiveInfo.setHangOnTime(LocalDateTime.now());
            }

            // 4. 保存档案信息
            archiveInfoService.save(archiveInfo);

            // 5. 记录挂接日志
            HangOnLog hangOnLog = new HangOnLog();
            hangOnLog.setArchiveId(archiveInfo.getId());
            hangOnLog.setHangOnType(1); // 1-自动挂接
            hangOnLog.setResult(1); // 1-成功
            hangOnLog.setOperateBy(pushDTO.getResponsiblePerson());
            // 将hangOnMethod和targetSystem合并到description中
            String fullDescription = String.format("业务系统推送挂接成功，业务单号：%s [方式: auto, 目标系统编码：%s,目标系统: %s]", pushDTO.getBusinessNo(), pushDTO.getSystemCode(), pushDTO.getSystemCode());
            hangOnLog.setDescription(fullDescription);
            hangOnLog.setCreateTime(LocalDateTime.now());
            hangOnLogMapper.insert(hangOnLog);

            log.info("业务系统档案推送挂接成功，档案ID：{}，业务单号：{}", archiveInfo.getId(), pushDTO.getBusinessNo());
            return true;
        } catch (Exception e) {
            log.error("业务系统档案推送挂接失败，业务单号：{}", pushDTO.getBusinessNo(), e);
            return false;
        }
    }
}