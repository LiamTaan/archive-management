package com.electronic.archive.service;

import com.electronic.archive.dto.CombinationHangOnRequestDTO;
import com.electronic.archive.dto.HangOnRequestDTO;
import com.electronic.archive.vo.HangOnResultVO;
import com.electronic.archive.vo.ResponseResult;

import java.util.List;
import java.util.Map;

/**
 * 挂接管理服务接口
 */
public interface HangOnManagementService {
    /**
     * 自动挂接档案
     * @param archiveId 档案ID
     * @return 挂接结果
     */
    boolean autoHangOn(Long archiveId);

    /**
     * 手动挂接档案
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param operateBy 操作人
     * @return 挂接结果
     */
    boolean manualHangOn(Long archiveId, String systemCode, String operateBy);  

    /**
     * 批量挂接档案
     * @param hangOnRequestDTO 挂接请求参数
     * @return 挂接结果
     */
    ResponseResult<Map<String, Object>> batchHangOn(HangOnRequestDTO hangOnRequestDTO);

    /**
     * 解除挂接档案
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param operateBy 操作人
     * @return 解除挂接结果
     */
    boolean unhook(Long archiveId, String systemCode, String operateBy);        

    /**
     * 重试挂接失败的档案
     * @param archiveId 档案ID
     * @param operateBy 操作人
     * @return 重试挂接结果
     */
    boolean retryHangOn(Long archiveId, String operateBy);

    /**
     * 获取挂接关系
     * @param archiveId 档案ID
     * @return 挂接关系列表
     */
    List<Map<String, Object>> getHangOnRelations(Long archiveId);

    /**
     * 修改挂接关系
     * @param archiveId 档案ID
     * @param systemCode 目标系统代码
     * @param operateBy 操作人
     * @param archiveType 档案分类（可选）
     * @param businessNo 业务单号（可选）
     * @param businessType 业务类型（可选）
     * @param responsiblePerson 责任人（可选）
     * @param department 所属部门（可选）
     * @return 是否成功
     */
    boolean modifyHangOnRelation(Long archiveId, String systemCode, String operateBy,
                              String archiveType, String businessNo, String businessType,
                              String responsiblePerson, String department);      

    /**
     * 批量重试挂接失败的档案
     * @param archiveIds 档案ID列表  
     * @param operateBy 操作人
     * @return 批量重试结果
     */
    ResponseResult<Map<String, Object>> batchRetryHangOn(List<Long> archiveIds, String operateBy);

    /**
     * 档案组合挂接
     * @param combinationHangOnRequestDTO 组合挂接请求参数
     * @return 组合挂接结果
     */
    ResponseResult<Map<String, Object>> combinationHangOn(CombinationHangOnRequestDTO combinationHangOnRequestDTO);
}