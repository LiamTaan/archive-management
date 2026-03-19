package com.electronic.archive.service;

import com.electronic.archive.dto.CollectionRequestDTO;
import com.electronic.archive.vo.CollectionResultVO;
import com.electronic.archive.vo.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 档案采集服务接口
 */
public interface ArchiveCollectionService {
    /**
     * 自动接口采集
     * @param interfaceId 接口配置ID
     * @return 采集结果
     */
    ResponseResult<CollectionResultVO> autoCollect(Long interfaceId);

    /**
     * 手动上传采集
     * @param requestDTO 采集请求参数
     * @param files 上传的文件列表
     * @return 采集结果
     */
    ResponseResult<CollectionResultVO> manualUpload(CollectionRequestDTO requestDTO, List<MultipartFile> files);

    /**
     * 批量上传采集
     * @param requestDTO 采集请求参数
     * @param files 上传的文件列表
     * @return 采集结果
     */
    ResponseResult<CollectionResultVO> batchUpload(CollectionRequestDTO requestDTO, List<MultipartFile> files);

    /**
     * 外部导入采集
     * @param requestDTO 采集请求参数
     * @param files 上传的文件列表
     * @return 采集结果
     */
    ResponseResult<CollectionResultVO> externalImport(CollectionRequestDTO requestDTO, List<MultipartFile> files);
}