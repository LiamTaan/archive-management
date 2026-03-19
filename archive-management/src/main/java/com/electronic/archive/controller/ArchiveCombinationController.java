package com.electronic.archive.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.electronic.archive.dto.ArchiveCombinationQueryDTO;
import com.electronic.archive.entity.ArchiveCombination;
import com.electronic.archive.entity.ArchiveCombinationRelation;
import com.electronic.archive.service.ArchiveCombinationService;
import com.electronic.archive.service.ArchiveCombinationRelationService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 档案组合控制器
 */
@Tag(name = "档案组合管理")
@RestController
@RequestMapping("/archive-combination")
public class ArchiveCombinationController {
    
    private static final Logger log = LoggerFactory.getLogger(ArchiveCombinationController.class);

    @Autowired
    private ArchiveCombinationService archiveCombinationService;

    @Autowired
    private ArchiveCombinationRelationService archiveCombinationRelationService;

    /**
     * 获取档案组合列表（支持分页和条件查询）
     * @param queryDTO 查询条件
     * @return 档案组合列表
     */
    @Operation(summary = "获取档案组合列表")
    @PostMapping("/list")
    public ResponseResult<PageResult<ArchiveCombination>> getArchiveCombinations(@RequestBody ArchiveCombinationQueryDTO queryDTO) {
        IPage<ArchiveCombination> pageResult = archiveCombinationService.queryArchiveCombinationByPage(queryDTO);
        PageResult<ArchiveCombination> result = PageResult.fromMpPage(pageResult);
        return ResponseResult.success("获取档案组合列表成功", result);
    }

    /**
     * 获取单个档案组合详情
     * @param id 组合ID
     * @return 档案组合详情
     */
    @Operation(summary = "获取单个档案组合详情")
    @GetMapping("/detail/{combinationId}")
    public ResponseResult<ArchiveCombination> getArchiveCombinationDetail(@PathVariable Long combinationId) {
        ArchiveCombination combination = archiveCombinationService.getById(combinationId);
        if (combination == null) {
            return ResponseResult.fail("档案组合不存在");
        }
        return ResponseResult.success("获取档案组合详情成功", combination);
    }

    /**
     * 创建档案组合
     * @param archiveCombination 档案组合信息
     * @return 创建结果
     */
    @Operation(summary = "创建档案组合")
    @PostMapping("/create")
    public ResponseResult<ArchiveCombination> createArchiveCombination(@RequestBody ArchiveCombination archiveCombination) {
        log.info("创建档案组合前，combinationId值：{}", archiveCombination.getCombinationId());
        boolean saved = archiveCombinationService.save(archiveCombination);
        log.info("创建档案组合后，combinationId值：{}", archiveCombination.getCombinationId());
        if (saved) {
            // 保存成功后，重新查询实体，获取数据库生成的正确ID
            ArchiveCombination savedCombination = archiveCombinationService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ArchiveCombination>()
                    .eq(ArchiveCombination::getCombinationName, archiveCombination.getCombinationName())
                    .eq(ArchiveCombination::getCreateBy, archiveCombination.getCreateBy())
                    .orderByDesc(ArchiveCombination::getCreateTime)
                    .last("LIMIT 1")
            );
            log.info("重新查询后，combinationId值：{}", savedCombination != null ? savedCombination.getCombinationId() : null);
            ArchiveCombination returnDTO = savedCombination != null ? savedCombination : archiveCombination;
            return ResponseResult.success("档案组合创建成功", returnDTO);
        } else {
            return ResponseResult.fail("档案组合创建失败");
        }
    }

    /**
     * 更新档案组合
     * @param archiveCombination 档案组合信息
     * @return 更新结果
     */
    @Operation(summary = "更新档案组合")
    @PutMapping("/update")
    public ResponseResult<ArchiveCombination> updateArchiveCombination(@RequestBody ArchiveCombination archiveCombination) {
        boolean updated = archiveCombinationService.updateById(archiveCombination);
        if (updated) {
            return ResponseResult.success("档案组合更新成功", archiveCombination);
        } else {
            return ResponseResult.fail("档案组合更新失败");
        }
    }

    /**
     * 删除档案组合
     * @param id 组合ID
     * @return 删除结果
     */
    @Operation(summary = "删除档案组合")
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteArchiveCombination(@PathVariable Long id) {
        // 先删除档案组合关系
        archiveCombinationRelationService.removeByCombinationId(id);
        // 再删除档案组合
        archiveCombinationService.removeById(id);
        // 无论档案组合是否存在，删除操作都视为成功（因为目标是确保资源不存在）
        return ResponseResult.success("档案组合删除成功");
    }

    /**
     * 获取档案组合的档案关系列表
     * @param combinationId 组合ID
     * @return 档案关系列表
     */
    @Operation(summary = "获取档案组合的档案关系列表")
    @GetMapping("/relations/{combinationId}")
    public ResponseResult<List<ArchiveCombinationRelation>> getArchiveCombinationRelations(@PathVariable Long combinationId) {
        List<ArchiveCombinationRelation> relations = archiveCombinationRelationService.listByCombinationId(combinationId);
        return ResponseResult.success("获取档案组合关系列表成功", relations);
    }

    /**
     * 批量保存档案组合关系（档案组合挂接）
     * @param request 请求参数，包含组合ID和档案ID列表
     * @return 保存结果
     */
    @Operation(summary = "批量保存档案组合关系")
    @PostMapping("/save-relations")
    public ResponseResult<Void> saveArchiveCombinationRelations(@RequestBody Map<String, Object> request) {
        String combinationIdStr = request.get("combinationId").toString();
        Long combinationId = Long.valueOf(combinationIdStr);
        
        // 检查组合是否存在
        ArchiveCombination combination = archiveCombinationService.getById(combinationId);
        if (combination == null) {
            log.error("组合不存在，combinationId：{}", combinationId);
            return ResponseResult.fail("组合不存在，无法挂接");
        }
        
        List<Long> archiveIds = new ArrayList<>();
        
        // 处理不同格式的archiveIds参数
        Object archiveIdsObj = request.get("archiveIds");
        if (archiveIdsObj instanceof List) {
            // 如果是List类型，遍历转换为Long
            List<?> tempList = (List<?>) archiveIdsObj;
            for (Object idObj : tempList) {
                try {
                    if (idObj != null) {
                        if (idObj instanceof Integer) {
                            archiveIds.add(((Integer) idObj).longValue());
                        } else if (idObj instanceof Long) {
                            archiveIds.add((Long) idObj);
                        } else if (idObj instanceof String) {
                            archiveIds.add(Long.parseLong((String) idObj));
                        }
                    }
                } catch (NumberFormatException e) {
                    // 跳过无效的ID
                    log.error("无效的档案ID: {}", idObj, e);
                }
            }
        } else if (archiveIdsObj instanceof String) {
            // 如果是String类型，按逗号分割并转换
            String archiveIdsStr = (String) archiveIdsObj;
            if (archiveIdsStr != null && !archiveIdsStr.isEmpty() && !"null".equalsIgnoreCase(archiveIdsStr)) {
                String[] idsStrArray = archiveIdsStr.split(",");
                for (String idStr : idsStrArray) {
                    try {
                        String trimmedId = idStr.trim();
                        if (!trimmedId.isEmpty() && !"null".equalsIgnoreCase(trimmedId)) {
                            archiveIds.add(Long.parseLong(trimmedId));
                        }
                    } catch (NumberFormatException e) {
                        // 跳过无效的ID
                        log.error("无效的档案ID: {}", idStr, e);
                    }
                }
            }
        }
        
        log.info("档案ID列表：{}", archiveIds);

        // 先删除原有的档案组合关系
        archiveCombinationRelationService.removeByCombinationId(combinationId);

        // 批量保存新的档案组合关系
        List<ArchiveCombinationRelation> relations = new ArrayList<>();
        for (int i = 0; i < archiveIds.size(); i++) {
            ArchiveCombinationRelation relation = new ArchiveCombinationRelation();
            relation.setCombinationId(combinationId);
            relation.setArchiveId(archiveIds.get(i));
            relation.setArchiveOrder(i + 1);
            relations.add(relation);
            log.info("创建关系：组合ID={}，档案ID={}，顺序={}", combinationId, archiveIds.get(i), i + 1);
        }

        boolean saved = archiveCombinationRelationService.saveBatch(relations);
        if (!saved) {
            log.error("档案组合关系保存失败");
            return ResponseResult.fail("档案组合关系保存失败");
        }
        return ResponseResult.success("档案组合关系保存成功");
    }

    /**
     * 删除档案组合关系
     * @param id 关系ID
     * @return 删除结果
     */
    @Operation(summary = "删除档案组合关系")
    @DeleteMapping("/relation/{id}")
    public ResponseResult<Void> deleteArchiveCombinationRelation(@PathVariable Long id) {
        boolean deleted = archiveCombinationRelationService.removeById(id);
        if (deleted) {
            return ResponseResult.success("档案组合关系删除成功");
        } else {
            return ResponseResult.fail("档案组合关系删除失败");
        }
    }
}
