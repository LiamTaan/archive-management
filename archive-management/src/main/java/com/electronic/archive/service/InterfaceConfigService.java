package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.InterfaceConfig;

import java.util.List;

/**
 * 接口配置服务接口
 */
public interface InterfaceConfigService extends IService<InterfaceConfig> {
    String getTargetSystemName(String systemCode);

    InterfaceConfig getByInterfaceCode(String systemCode);

    List<InterfaceConfig> getEnabledInterfaces();
}