package com.electronic.archive.controller;

import com.electronic.archive.dto.ArchivePushDTO;
import com.electronic.archive.service.ArchivePushService;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 档案推送控制器
 * 用于接收业务系统推送的档案信息，自动完成挂接
 */
@Tag(name = "档案推送")
@RestController
@RequestMapping("/archive")
public class ArchivePushController {
    @Autowired
    private ArchivePushService archivePushService;

    @Operation(summary = "业务系统档案推送")
    @PostMapping("/push")
    public ResponseResult<Boolean> pushArchive(@RequestBody ArchivePushDTO pushDTO) {
        boolean result = archivePushService.pushAndHookArchive(pushDTO);
        return ResponseResult.success("档案推送完成", result);
    }
}