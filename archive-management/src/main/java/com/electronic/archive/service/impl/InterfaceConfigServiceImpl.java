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
}