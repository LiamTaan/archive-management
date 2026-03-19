package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.HangOnLog;
import com.electronic.archive.mapper.HangOnLogMapper;
import com.electronic.archive.service.HangOnLogService;
import org.springframework.stereotype.Service;

/**
 * 挂接日志服务实现类
 */
@Service
public class HangOnLogServiceImpl extends ServiceImpl<HangOnLogMapper, HangOnLog> implements HangOnLogService {
}