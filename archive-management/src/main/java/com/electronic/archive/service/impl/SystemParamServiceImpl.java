package com.electronic.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electronic.archive.entity.SystemParam;
import com.electronic.archive.mapper.SystemParamMapper;
import com.electronic.archive.service.SystemParamService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统参数服务实现类
 */
@Service
public class SystemParamServiceImpl extends ServiceImpl<SystemParamMapper, SystemParam> implements SystemParamService {
    @Override
    public Map<String, String> getAllParams() {
        List<SystemParam> params = this.list(new LambdaQueryWrapper<SystemParam>().eq(SystemParam::getStatus, 1));
        Map<String, String> paramMap = new HashMap<>();
        for (SystemParam param : params) {
            paramMap.put(param.getParamKey(), param.getParamValue());
        }
        return paramMap;
    }

    @Override
    public String getParamByKey(String paramKey) {
        SystemParam param = this.getOne(new LambdaQueryWrapper<SystemParam>()
                .eq(SystemParam::getParamKey, paramKey)
                .eq(SystemParam::getStatus, 1));
        return param != null ? param.getParamValue() : null;
    }

    @Override
    public boolean saveParams(Map<String, String> paramMap) {
        boolean success = true;
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            SystemParam param = this.getOne(new LambdaQueryWrapper<SystemParam>().eq(SystemParam::getParamKey, entry.getKey()));
            if (param != null) {
                // 更新参数
                param.setParamValue(entry.getValue());
                param.setUpdateTime(LocalDateTime.now());
                success &= this.updateById(param);
            } else {
                // 新增参数（默认参数类型为字符串，状态为启用）
                param = new SystemParam();
                param.setParamKey(entry.getKey());
                param.setParamValue(entry.getValue());
                param.setParamName(entry.getKey()); // 简化实现，实际应提供参数名称
                param.setParamType(0);
                param.setStatus(1);
                param.setOperateBy("admin"); // 简化实现，实际应从当前登录用户获取
                param.setCreateTime(LocalDateTime.now());
                param.setUpdateTime(LocalDateTime.now());
                success &= this.save(param);
            }
        }
        return success;
    }
}