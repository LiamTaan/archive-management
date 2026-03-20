package com.electronic.archive.service.impl;

import com.electronic.archive.constants.ArchiveTypeConstants;
import com.electronic.archive.dto.CollectionRequestDTO;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.entity.InterfaceConfig;
import com.electronic.archive.entity.SysUser;
import com.electronic.archive.service.*;
import com.electronic.archive.vo.CollectionResultVO;
import com.electronic.archive.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 档案采集服务实现类
 */
@Service
@Slf4j
public class ArchiveCollectionServiceImpl implements ArchiveCollectionService {
    
    @Autowired
    private InterfaceConfigService interfaceConfigService;

    @Autowired
    private ArchiveInfoService archiveInfoService;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private CollectionLogService collectionLogService;
    
    // 文件存储路径，可在application.yml中配置
    @Value("${archive.file.storage.path:D:/archive-files}")
    private String fileStoragePath;

    @Override
    public ResponseResult<CollectionResultVO> autoCollect(Long interfaceId) {
        try {
            // 1. 获取接口配置信息
            InterfaceConfig interfaceConfig = interfaceConfigService.getById(interfaceId);
            if (interfaceConfig == null) {
                return ResponseResult.fail("接口配置不存在");
            }

            // 2. 模拟接口调用，获取档案数据
            // 实际项目中，这里应该根据接口配置调用外部系统接口获取数据

            // 3. 模拟创建档案信息
            ArchiveInfo archiveInfo = new ArchiveInfo();
            archiveInfo.setFileName("自动采集测试档案.pdf");
            archiveInfo.setFilePath("/test/auto/archive_" + System.currentTimeMillis() + ".pdf");
            archiveInfo.setFileType(".pdf");
            archiveInfo.setFileSize(1024L);
            archiveInfo.setMd5Value("e10adc3949ba59abbe56e057f20f883e");
            archiveInfo.setArchiveType("电子档案");
            archiveInfo.setBusinessNo("AUTO_" + System.currentTimeMillis());
            archiveInfo.setBusinessType("自动采集");
            archiveInfo.setResponsiblePerson("system");
            archiveInfo.setHangOnType(0);
            archiveInfo.setStatus(1);
            archiveInfo.setRemark("自动采集测试档案");
            archiveInfo.setCreateTime(LocalDateTime.now());
            archiveInfo.setUpdateTime(LocalDateTime.now());

            // 4. 保存档案信息
            archiveInfoService.save(archiveInfo);

            // 5. 记录采集日志
            collectionLogService.saveCollectionLog(
                    archiveInfo.getId(),
                    2, // 接口采集
                    "system",
                    String.format("自动接口采集档案：%s", archiveInfo.getFileName()),
                    1, // 成功
                    null,
                    1, // totalCount
                    1, // successCount
                    0 // failCount
            );

            // 6. 构建返回结果
            CollectionResultVO resultVO = new CollectionResultVO();
            resultVO.setTotalCount(1);
            resultVO.setSuccessCount(1);
            resultVO.setFailCount(0);
            resultVO.setDescription("自动采集成功");

            log.info("自动接口采集完成，接口ID：{}，结果：{}", interfaceId, resultVO);
            return ResponseResult.success("自动采集完成", resultVO);
        } catch (Exception e) {
            log.error("自动接口采集失败，接口ID：{}", interfaceId, e);
            return ResponseResult.fail("自动采集失败");
        }
    }

    /**
     * 保存上传的文件到磁盘
     * @param file 上传的文件
     * @param uploadType 上传类型（manual/batch/external）
     * @return 保存后的文件路径
     * @throws IOException IO异常
     */
    private String saveFile(MultipartFile file, String uploadType) throws IOException {
        // 确保存储目录存在
        Path storageDir = Paths.get(fileStoragePath, uploadType);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        // 生成唯一文件名，避免重复
        String originalFilename = file.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 保存文件
        Path filePath = storageDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        log.info("文件保存成功：{}", filePath);
        return filePath.toString();
    }

    @Override
    public ResponseResult<CollectionResultVO> manualUpload(CollectionRequestDTO requestDTO, List<MultipartFile> files) {
        try {
            // 获取当前登录用户的昵称作为责任人
            String responsiblePerson = "user";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    responsiblePerson = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            
            int totalCount = files.size();
            int successCount = 0;
            
            for (MultipartFile file : files) {
                try {
                    // 保存文件到磁盘
                    String filePath = saveFile(file, "manual");
                    
                    // 记录档案类型ID，用于调试
                    log.info("Archive type ID from request: {}", requestDTO.getArchiveType());
                    
                    // 根据档案类型ID获取档案分类名称
                    String archiveTypeName = ArchiveTypeConstants.getTypeNameByTypeId(
                        requestDTO.getArchiveType()
                    );
                    
                    // 记录获取到的档案分类名称
                    log.info("Archive type name: {}", archiveTypeName);
                    
                    // 创建档案信息
                    ArchiveInfo archiveInfo = new ArchiveInfo();
                    archiveInfo.setFileName(file.getOriginalFilename());
                    archiveInfo.setFilePath(filePath);
                    archiveInfo.setFileType("." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                    archiveInfo.setFileSize(file.getSize());
                    archiveInfo.setMd5Value("e10adc3949ba59abbe56e057f20f883e"); // 模拟MD5
                    archiveInfo.setArchiveType(archiveTypeName);
                    archiveInfo.setBusinessNo("MANUAL_" + System.currentTimeMillis());
                    archiveInfo.setBusinessType("手动上传");
                    archiveInfo.setResponsiblePerson(responsiblePerson);
                    archiveInfo.setHangOnType(1);
                    archiveInfo.setStatus(0);
                    archiveInfo.setRemark("手动上传档案");
                    archiveInfo.setCreateTime(LocalDateTime.now());
                    archiveInfo.setUpdateTime(LocalDateTime.now());

                    archiveInfoService.save(archiveInfo);
                    successCount++;
                    
                    // 记录采集日志
                    collectionLogService.saveCollectionLog(
                            archiveInfo.getId(),
                            0, // 手动采集
                            responsiblePerson,
                            String.format("手动上传档案：%s", file.getOriginalFilename()),
                            1, // 成功
                            null,
                            1, // totalCount
                            1, // successCount
                            0 // failCount
                    );
                } catch (Exception e) {
                    log.error("保存文件失败：{}", file.getOriginalFilename(), e);
                }
            }

            CollectionResultVO resultVO = new CollectionResultVO();
            resultVO.setTotalCount(totalCount);
            resultVO.setSuccessCount(successCount);
            resultVO.setFailCount(totalCount - successCount);
            resultVO.setDescription("手动上传采集完成");

            log.info("手动上传采集完成，结果：{}", resultVO);
            return ResponseResult.success("手动上传完成", resultVO);
        } catch (Exception e) {
            log.error("手动上传采集失败", e);
            return ResponseResult.fail("手动上传失败");
        }
    }

    @Override
    public ResponseResult<CollectionResultVO> batchUpload(CollectionRequestDTO requestDTO, List<MultipartFile> files) {
        try {
            // 获取当前登录用户的昵称作为责任人
            String responsiblePerson = "user";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    responsiblePerson = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            
            int totalCount = files.size();
            int successCount = 0;
            
            for (MultipartFile file : files) {
                try {
                    // 保存文件到磁盘
                    String filePath = saveFile(file, "batch");
                    
                    // 记录档案类型ID，用于调试
                    log.info("Archive type ID from request: {}", requestDTO.getArchiveType());
                    
                    // 根据档案类型ID获取档案分类名称
                    String archiveTypeName = ArchiveTypeConstants.getTypeNameByTypeId(
                        requestDTO.getArchiveType()
                    );
                    
                    // 记录获取到的档案分类名称
                    log.info("Archive type name: {}", archiveTypeName);
                    
                    // 创建档案信息
                    ArchiveInfo archiveInfo = new ArchiveInfo();
                    archiveInfo.setFileName(file.getOriginalFilename());
                    archiveInfo.setFilePath(filePath);
                    archiveInfo.setFileType("." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                    archiveInfo.setFileSize(file.getSize());
                    archiveInfo.setMd5Value("e10adc3949ba59abbe56e057f20f883e"); // 模拟MD5
                    archiveInfo.setArchiveType(archiveTypeName);
                    archiveInfo.setBusinessNo("BATCH_" + System.currentTimeMillis());
                    archiveInfo.setBusinessType("批量上传");
                    archiveInfo.setResponsiblePerson(responsiblePerson);
                    archiveInfo.setHangOnType(1);
                    archiveInfo.setStatus(0);
                    archiveInfo.setRemark("批量上传档案");
                    archiveInfo.setCreateTime(LocalDateTime.now());
                    archiveInfo.setUpdateTime(LocalDateTime.now());

                    archiveInfoService.save(archiveInfo);
                    successCount++;
                    
                    // 记录采集日志
                    collectionLogService.saveCollectionLog(
                            archiveInfo.getId(),
                            1, // 批量采集
                            responsiblePerson,
                            String.format("批量上传档案：%s", file.getOriginalFilename()),
                            1, // 成功
                            null,
                            1, // totalCount
                            1, // successCount
                            0 // failCount
                    );
                } catch (Exception e) {
                    log.error("保存文件失败：{}", file.getOriginalFilename(), e);
                }
            }

            CollectionResultVO resultVO = new CollectionResultVO();
            resultVO.setTotalCount(totalCount);
            resultVO.setSuccessCount(successCount);
            resultVO.setFailCount(totalCount - successCount);
            resultVO.setDescription("批量上传采集完成");

            log.info("批量上传采集完成，结果：{}", resultVO);
            return ResponseResult.success("批量上传完成", resultVO);
        } catch (Exception e) {
            log.error("批量上传采集失败", e);
            return ResponseResult.fail("批量上传失败");
        }
    }

    @Override
    public ResponseResult<CollectionResultVO> externalImport(CollectionRequestDTO requestDTO, List<MultipartFile> files) {
        try {
            // 获取当前登录用户的昵称作为责任人
            String responsiblePerson = "user";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    responsiblePerson = user.getNickname() != null ? user.getNickname() : user.getUsername();
                }
            }
            
            int totalCount = files.size();
            int successCount = 0;
            
            for (MultipartFile file : files) {
                try {
                    // 保存文件到磁盘
                    String filePath = saveFile(file, "external");
                    
                    // 记录档案类型ID，用于调试
                    log.info("Archive type ID from request: {}", requestDTO.getArchiveType());
                    
                    // 根据档案类型ID获取档案分类名称
                    String archiveTypeName = ArchiveTypeConstants.getTypeNameByTypeId(
                        requestDTO.getArchiveType()
                    );
                    
                    // 记录获取到的档案分类名称
                    log.info("Archive type name: {}", archiveTypeName);
                    
                    // 创建档案信息
                    ArchiveInfo archiveInfo = new ArchiveInfo();
                    archiveInfo.setFileName(file.getOriginalFilename());
                    archiveInfo.setFilePath(filePath);
                    archiveInfo.setFileType("." + StringUtils.getFilenameExtension(file.getOriginalFilename()));
                    archiveInfo.setFileSize(file.getSize());
                    archiveInfo.setMd5Value("e10adc3949ba59abbe56e057f20f883e"); // 模拟MD5
                    archiveInfo.setArchiveType(archiveTypeName);
                    archiveInfo.setBusinessNo("EXTERNAL_" + System.currentTimeMillis());
                    archiveInfo.setBusinessType("外部导入");
                    archiveInfo.setResponsiblePerson(responsiblePerson);
                    archiveInfo.setHangOnType(1);
                    archiveInfo.setStatus(0);
                    archiveInfo.setRemark("外部导入档案");
                    archiveInfo.setCreateTime(LocalDateTime.now());
                    archiveInfo.setUpdateTime(LocalDateTime.now());

                    archiveInfoService.save(archiveInfo);
                    successCount++;
                    
                    // 记录采集日志
                    collectionLogService.saveCollectionLog(
                            archiveInfo.getId(),
                            3, // 外部导入
                            responsiblePerson,
                            String.format("外部导入档案：%s", file.getOriginalFilename()),
                            1, // 成功
                            null,
                            1, // totalCount
                            1, // successCount
                            0 // failCount
                    );
                } catch (Exception e) {
                    log.error("保存文件失败：{}", file.getOriginalFilename(), e);
                }
            }

            CollectionResultVO resultVO = new CollectionResultVO();
            resultVO.setTotalCount(totalCount);
            resultVO.setSuccessCount(successCount);
            resultVO.setFailCount(totalCount - successCount);
            resultVO.setDescription("外部导入采集完成");

            log.info("外部导入采集完成，结果：{}", resultVO);
            return ResponseResult.success("外部导入完成", resultVO);
        } catch (Exception e) {
            log.error("外部导入采集失败", e);
            return ResponseResult.fail("外部导入失败");
        }
    }
}