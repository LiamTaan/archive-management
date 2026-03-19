package com.electronic.archive.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electronic.archive.entity.SystemParam;

import java.util.Map;

/**
 * 系统参数服务接口
 */
public interface SystemParamService extends IService<SystemParam> {
    /**
     * 获取所有系统参数
     * @return 系统参数Map
     */
    Map<String, String> getAllParams();

    /**
     * 根据参数键名获取参数值
     * @param paramKey 参数键名
     * @return 参数值
     */
    String getParamByKey(String paramKey);

    /**
     * 保存系统参数
     * @param paramMap 系统参数Map
     * @return 是否保存成功
     */
    boolean saveParams(Map<String, String> paramMap);
}