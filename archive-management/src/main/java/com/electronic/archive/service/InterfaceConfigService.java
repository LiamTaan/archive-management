package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.InterfaceConfig;

/**
 * 接口配置服务接口
 */
public interface InterfaceConfigService extends IService<InterfaceConfig> {
    String getTargetSystemName(String systemCode);

    InterfaceConfig getByInterfaceCode(String systemCode);
}