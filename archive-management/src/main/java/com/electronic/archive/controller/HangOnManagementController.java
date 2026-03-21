package com.electronic.archive.controller;

import com.electronic.archive.dto.HangOnRequestDTO;
import com.electronic.archive.service.HangOnManagementService;
import com.electronic.archive.vo.HangOnResultVO;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 挂接管理控制器
 */
@Tag(name = "挂接管理")
@RestController
@RequestMapping("/hang-on")
public class HangOnManagementController {
    @Autowired
    private HangOnManagementService hangOnManagementService;


    @Operation(summary = "手动挂接档案")
    @PostMapping("/manual/{archiveId}")
    public ResponseResult<Map<String, Boolean>> manualHangOn(@PathVariable Long archiveId,
                                                            @RequestParam String systemCode,
                                                            @RequestParam(required = false, defaultValue = "user") String operateBy) {
        boolean result = hangOnManagementService.manualHangOn(archiveId, systemCode, operateBy);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", result);
        return ResponseResult.success("手动挂接完成", response);
    }

    @Operation(summary = "批量挂接档案")
    @PostMapping("/batch")
    public ResponseResult<Map<String, Object>> batchHangOn(@RequestBody HangOnRequestDTO hangOnRequestDTO) {
        return hangOnManagementService.batchHangOn(hangOnRequestDTO);
    }

    @Operation(summary = "解除挂接档案")
    @PostMapping("/unhook/{archiveId}")
    public ResponseResult<Map<String, Boolean>> unhook(@PathVariable Long archiveId,
                                                     @RequestParam String systemCode,
                                                     @RequestParam(required = false, defaultValue = "user") String operateBy) {
        boolean result = hangOnManagementService.unhook(archiveId, systemCode, operateBy);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", result);
        return ResponseResult.success("解除挂接完成", response);
    }


    @Operation(summary = "获取档案挂接关系")
    @GetMapping("/relations/{archiveId}")
    public ResponseResult<List<Map<String, Object>>> getHangOnRelations(@PathVariable Long archiveId) {
        List<Map<String, Object>> relations = hangOnManagementService.getHangOnRelations(archiveId);
        return ResponseResult.success("获取挂接关系完成", relations);
    }

    @Operation(summary = "修改档案挂接关系")
    @PutMapping("/modify/{archiveId}")
    public ResponseResult<Map<String, Boolean>> modifyHangOnRelation(@PathVariable Long archiveId,
                                                                  @RequestParam String systemCode,
                                                                  @RequestParam(required = false, defaultValue = "user") String operateBy,
                                                                  @RequestParam(required = false) String archiveType,
                                                                  @RequestParam(required = false) String businessNo,
                                                                  @RequestParam(required = false) String businessType,
                                                                  @RequestParam(required = false) String responsiblePerson,
                                                                  @RequestParam(required = false) String department) {
        boolean result = hangOnManagementService.modifyHangOnRelation(archiveId, systemCode, operateBy,
                                                                   archiveType, businessNo, businessType,
                                                                   responsiblePerson, department);
        Map<String, Boolean> response = new HashMap<>();
        response.put("success", result);
        return ResponseResult.success("修改挂接关系完成", response);
    }
}