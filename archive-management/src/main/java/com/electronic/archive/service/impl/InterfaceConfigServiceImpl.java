package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.InterfaceConfig;
import com.electronic.archive.mapper.InterfaceConfigMapper;
import com.electronic.archive.service.InterfaceConfigService;
import org.springframework.stereotype.Service;

/**
 * 接口配置服务实现类
 */
@Service
public class InterfaceConfigServiceImpl extends ServiceImpl<InterfaceConfigMapper, InterfaceConfig> implements InterfaceConfigService {

    @Override
    public String getTargetSystemName(String systemCode) {
        // 根据系统编码（接口编码）获取业务系统名称
        InterfaceConfig interfaceConfig = this.lambdaQuery()
                .eq(InterfaceConfig::getInterfaceCode, systemCode)
                .one();
        return interfaceConfig != null ? interfaceConfig.getBusinessSystem() : "未知系统";
    }

    @Override
    public InterfaceConfig getByInterfaceCode(String systemCode) {
        return this.lambdaQuery()
                .eq(InterfaceConfig::getInterfaceCode, systemCode)
                .one();
    }
}