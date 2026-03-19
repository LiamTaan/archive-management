package com.electronic.archive.service;

import com.electronic.archive.dto.ArchivePushDTO;

/**
 * 档案推送服务接口
 * 用于处理业务系统推送的档案信息，自动完成挂接
 */
public interface ArchivePushService {
    /**
     * 推送并挂接档案
     * @param pushDTO 档案推送信息
     * @return 是否成功
     */
    boolean pushAndHookArchive(ArchivePushDTO pushDTO);
}