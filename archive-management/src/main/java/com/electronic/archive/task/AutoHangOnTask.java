package com.electronic.archive.task;

import com.electronic.archive.entity.InterfaceConfig;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.service.InterfaceConfigService;
import com.electronic.archive.service.ArchiveCollectionService;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.service.HangOnManagementService;
import com.electronic.archive.vo.ResponseResult;
import com.electronic.archive.vo.CollectionResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自动挂接定时任务类
 * 用于定时执行自动挂接逻辑，通过目标系统配置调用第三方API拉取档案文件及数据
 */
@Component
@Slf4j
public class AutoHangOnTask {

    @Autowired
    private InterfaceConfigService interfaceConfigService;

    @Autowired
    private ArchiveCollectionService archiveCollectionService;

    @Autowired
    private HangOnManagementService hangOnManagementService;

    @Autowired
    private ArchiveInfoService archiveInfoService;

    // 创建线程池用于并行处理不同系统的自动挂接
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 自动挂接定时任务
     * 每小时执行一次
     */
//    @Scheduled(cron = "0 0 * * * ?")
    public void autoHangOnTask() {
        log.info("开始执行自动挂接定时任务");
        
        try {
            // 1. 查询所有启用的接口配置
            List<InterfaceConfig> enabledInterfaces = interfaceConfigService.getEnabledInterfaces();
            log.info("共获取到 {} 个启用的目标系统接口配置", enabledInterfaces.size());
            
            // 2. 并行处理每个接口的自动挂接
            enabledInterfaces.forEach(interfaceConfig -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        executeAutoHangOn(interfaceConfig);
                    } catch (Exception e) {
                        log.error("处理自动挂接失败，接口ID：{}，接口名称：{}", 
                                interfaceConfig.getId(), interfaceConfig.getInterfaceName(), e);
                    }
                }, executorService);
            });
            
        } catch (Exception e) {
            log.error("执行自动挂接定时任务失败", e);
        } finally {
            log.info("自动挂接定时任务执行完成");
        }
    }

    /**
     * 执行单个目标系统的自动挂接
     * @param interfaceConfig 目标系统接口配置
     */
    private void executeAutoHangOn(InterfaceConfig interfaceConfig) {
        log.info("开始处理目标系统自动挂接，接口ID：{}，接口名称：{}", 
                interfaceConfig.getId(), interfaceConfig.getInterfaceName());
        
        try {
            // 1. 调用档案采集服务获取档案数据
            ResponseResult<CollectionResultVO> result = archiveCollectionService.autoCollect(interfaceConfig.getId());
            
            if (result.getCode() == 200) {
                CollectionResultVO collectionResult = result.getData();
                log.info("目标系统自动挂接完成，接口ID：{}，接口名称：{}，成功采集：{}，失败：{}", 
                        interfaceConfig.getId(), interfaceConfig.getInterfaceName(), 
                        collectionResult.getSuccessCount(), collectionResult.getFailCount());
            } else {
                log.error("目标系统自动挂接失败，接口ID：{}，接口名称：{}，原因：{}", 
                        interfaceConfig.getId(), interfaceConfig.getInterfaceName(), result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("执行目标系统自动挂接异常，接口ID：{}，接口名称：{}", 
                    interfaceConfig.getId(), interfaceConfig.getInterfaceName(), e);
        }
    }

    /**
     * 手动触发自动挂接任务
     * 用于手动执行自动挂接逻辑
     * @param interfaceId 接口ID，可以为null表示执行所有启用的接口
     */
    public void manualTriggerAutoHangOn(Long interfaceId) {
        log.info("手动触发自动挂接任务，接口ID：{}", interfaceId);
        
        try {
            List<InterfaceConfig> interfaceConfigs;
            
            if (interfaceId != null) {
                // 只执行指定接口的自动挂接
                InterfaceConfig interfaceConfig = interfaceConfigService.getById(interfaceId);
                if (interfaceConfig == null) {
                    log.error("手动触发自动挂接失败，接口ID：{}不存在", interfaceId);
                    return;
                }
                if (interfaceConfig.getStatus() != 1) {
                    log.error("手动触发自动挂接失败，接口ID：{}已禁用", interfaceId);
                    return;
                }
                interfaceConfigs = List.of(interfaceConfig);
            } else {
                // 执行所有启用的接口的自动挂接
                interfaceConfigs = interfaceConfigService.getEnabledInterfaces();
            }
            
            // 并行处理每个接口的自动挂接
            interfaceConfigs.forEach(interfaceConfig -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        executeAutoHangOn(interfaceConfig);
                    } catch (Exception e) {
                        log.error("手动触发自动挂接失败，接口ID：{}，接口名称：{}", 
                                interfaceConfig.getId(), interfaceConfig.getInterfaceName(), e);
                    }
                }, executorService);
            });
            
        } catch (Exception e) {
            log.error("手动触发自动挂接任务失败", e);
        } finally {
            log.info("手动触发自动挂接任务执行完成");
        }
    }
}