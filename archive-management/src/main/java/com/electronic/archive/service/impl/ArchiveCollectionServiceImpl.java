package com.electronic.archive.service.impl;

import com.electronic.archive.constants.ArchiveTypeConstants;
import com.electronic.archive.dto.CollectionRequestDTO;
import com.electronic.archive.entity.*;
import com.electronic.archive.service.*;
import com.electronic.archive.vo.CollectionResultVO;
import com.electronic.archive.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private HangOnLogService hangOnLogService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    // 文件存储路径，可在application.yml中配置
    @Value("${archive.file.storage.path:D:/archive-files}")
    private String fileStoragePath;

    @Transactional
    @Override
    public ResponseResult<CollectionResultVO> autoCollect(Long interfaceId) {
        log.info("开始自动接口采集，接口ID：{}", interfaceId);
        
        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;
        String failureReason = null;
        
        try {
            // 1. 获取接口配置信息
            InterfaceConfig interfaceConfig = interfaceConfigService.getById(interfaceId);
            if (interfaceConfig == null) {
                failureReason = "接口配置不存在";
                log.error("自动接口采集失败，接口ID：{}，原因：{}", interfaceId, failureReason);
                return ResponseResult.fail(failureReason);
            }
            
            // 检查接口配置状态
            if (interfaceConfig.getStatus() == null || interfaceConfig.getStatus() == 0) {
                failureReason = "接口配置已禁用";
                log.error("自动接口采集失败，接口ID：{}，原因：{}", interfaceId, failureReason);
                return ResponseResult.fail(failureReason);
            }

            // 2. 获取文件元信息列表
            List<FileMetaDTO> fileMetaList = getFileMetaList(interfaceConfig);
            totalCount = fileMetaList.size();
            
            // 3. 遍历文件元信息，执行分片传输采集
            for (FileMetaDTO fileMeta : fileMetaList) {
                try {
                    // 执行单个文件的分片采集
                    Long archiveId = collectLargeFile(interfaceConfig, fileMeta);
                    successCount++;
                    
                    // 记录采集日志
                    collectionLogService.saveCollectionLog(
                            archiveId, // 档案ID在collectLargeFile方法中已经处理
                            2, // 接口采集
                            "system",
                            String.format("自动接口采集档案，来源：%s，文件名：%s", 
                                    interfaceConfig.getInterfaceName(), fileMeta.getFileName()),
                            1, // 成功
                            null,
                            1,
                            1,
                            0
                    );
                    
                    // 生成挂接日志
                    HangOnLog hangOnLog = new HangOnLog();
                    hangOnLog.setArchiveId(archiveId);
                    hangOnLog.setOperateBy("system");
                    hangOnLog.setCreateTime(LocalDateTime.now());
                    hangOnLog.setHangOnType(0); // 0表示挂接
                    hangOnLog.setDescription(String.format("自动挂接成功 [方式: manual, 目标系统编码：%s,目标系统: %s]",
                            interfaceConfig.getInterfaceCode(),interfaceConfig.getInterfaceCode()));
                    hangOnLog.setResult(1); // 1表示成功
                    hangOnLog.setErrorInfo(null);
                    hangOnLog.setRemark("自动接口采集挂接");
                    
                    // 保存挂接日志
                    hangOnLogService.save(hangOnLog);
                    log.info("挂接日志生成成功，档案ID：{}", archiveId);
                } catch (Exception e) {
                    failCount++;
                    String fileFailureReason = String.format("文件采集失败，文件名：%s，原因：%s", 
                            fileMeta.getFileName(), e.getMessage());
                    log.error(fileFailureReason, e);
                    
                    // 记录采集日志（失败）
                    collectionLogService.saveCollectionLog(
                            null,
                            2, // 接口采集
                            "system",
                            String.format("自动接口采集档案，来源：%s，文件名：%s", 
                                    interfaceConfig.getInterfaceName(), fileMeta.getFileName()),
                            0, // 失败
                            fileFailureReason,
                            1,
                            0,
                            1
                    );
                }
            }

            // 4. 构建返回结果
            CollectionResultVO resultVO = new CollectionResultVO();
            resultVO.setTotalCount(totalCount);
            resultVO.setSuccessCount(successCount);
            resultVO.setFailCount(failCount);
            resultVO.setDescription(String.format("自动采集完成，成功：%d，失败：%d", successCount, failCount));

            log.info("自动接口采集完成，接口ID：{}，结果：{}", interfaceId, resultVO);
            return ResponseResult.success("自动采集完成", resultVO);
        } catch (Exception e) {
            failCount = totalCount > 0 ? totalCount : 1;
            failureReason = String.format("自动采集失败：%s", e.getMessage());
            log.error("自动接口采集失败，接口ID：{}", interfaceId, e);
            
            // 记录采集日志（失败）
            collectionLogService.saveCollectionLog(
                    1111111,
                    2, // 接口采集
                    "system",
                    String.format("自动接口采集档案，接口ID：%d", interfaceId),
                    0, // 失败
                    failureReason,
                    totalCount,
                    successCount,
                    failCount
            );
            
            return ResponseResult.fail(failureReason);
        }
    }
    
    /**
     * 大文件分片采集
     * @param config 接口配置
     * @param fileMeta 文件元信息
     * @throws Exception 异常
     */
    private Long collectLargeFile(InterfaceConfig config, FileMetaDTO fileMeta) throws Exception {
        log.info("开始分片采集，接口名称：{}，文件ID：{}，文件名：{}", 
                config.getInterfaceName(), fileMeta.getFileId(), fileMeta.getFileName());
        
        // 1. 下载分片文件
        List<File> shardFiles = downloadShards(config, fileMeta);
        log.info("所有分片下载完成，共{}个分片", shardFiles.size());
        
        // 2. 合并分片文件
        File mergedFile = mergeFiles(shardFiles, fileMeta.getFileName());
        log.info("分片合并完成，合并后的文件：{}", mergedFile.getAbsolutePath());
        
        // 3. 校验总MD5
        String calculatedMd5 = calculateMd5(mergedFile);
        if (!calculatedMd5.equals(fileMeta.getTotalMd5())) {
            throw new Exception(String.format("文件分片合并后MD5不一致，预期：%s，实际：%s", 
                    fileMeta.getTotalMd5(), calculatedMd5));
        }
        log.info("MD5校验通过，文件完整");
        
        // 4. 存储合并后的文件
        String filePath = saveFileFromMerge(mergedFile, "auto", fileMeta.getFileName());
        log.info("文件存储成功，存储路径：{}", filePath);
        
        // 5. 保存档案信息
        Long archiveId = saveArchiveInfo(fileMeta, filePath, config);
        log.info("档案信息保存成功");
        
        // 6. 清理临时文件
        cleanupTempFiles(shardFiles, mergedFile);
        log.info("临时文件清理完成");
        return archiveId;
    }
    
    /**
     * 获取文件元信息列表
     * @param config 接口配置
     * @return 文件元信息列表
     * @throws Exception 异常
     */
    private List<FileMetaDTO> getFileMetaList(InterfaceConfig config) throws Exception {
        log.info("调用文件元信息接口，URL：{}", config.getMetadataUrl());
        
        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        
        // 1. 解析并添加配置的请求头
        if (StringUtils.hasText(config.getRequestHeaders())) {
            try {
                // 使用Jackson解析JSON格式的请求头
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> customHeaders = objectMapper.readValue(
                        config.getRequestHeaders(), new TypeReference<Map<String, String>>() {});
                // 将解析后的请求头添加到HttpHeaders中
                customHeaders.forEach(headers::set);
                log.info("成功解析并添加自定义请求头");
            } catch (Exception e) {
                log.warn("解析请求头失败，使用默认请求头：{}", e.getMessage());
            }
        }
        
        // 2. 添加认证信息（如果配置了）
        if (StringUtils.hasText(config.getSecretKey())) {
            // 如果请求头中没有Authorization，则添加
            if (!headers.containsKey("Authorization")) {
                headers.set("Authorization", "Bearer " + config.getSecretKey());
            }
        }
        
        // 3. 处理请求参数
        String requestBody = null;
        HttpEntity<?> entity = null;
        
        // 获取请求方法，默认GET
        String method = config.getRequestMethod() != null ? config.getRequestMethod() : "GET";
        
        // 如果是POST或PUT等方法，且配置了请求参数，则将参数作为请求体
        if (StringUtils.hasText(config.getRequestParams())) {
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                requestBody = config.getRequestParams();
                entity = new HttpEntity<>(requestBody, headers);
                log.info("使用请求体：{}", requestBody);
            } else {
                // GET等方法，请求参数会作为URL参数处理，由restTemplate自动处理
                entity = new HttpEntity<>(headers);
            }
        } else {
            entity = new HttpEntity<>(headers);
        }
        
        // 4. 调用元信息接口，使用配置的请求方法
        try {
            // 尝试获取多个文件元信息
            ResponseEntity<List<FileMetaDTO>> response = restTemplate.exchange(
                    config.getMetadataUrl(),
                    HttpMethod.valueOf(method),
                    entity,
                    new org.springframework.core.ParameterizedTypeReference<List<FileMetaDTO>>() {}
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception(String.format("获取文件元信息失败，状态码：%d", 
                        response.getStatusCode().value()));
            }
            
            List<FileMetaDTO> fileMetaList = response.getBody();
            if (fileMetaList == null || fileMetaList.isEmpty()) {
                log.info("获取到的文件元信息列表为空，尝试获取单个文件元信息");
                // 如果获取不到多个文件元信息，尝试获取单个文件元信息
                ResponseEntity<FileMetaDTO> singleResponse = restTemplate.exchange(
                        config.getMetadataUrl(),
                        HttpMethod.valueOf(method),
                        entity,
                        FileMetaDTO.class
                );
                
                if (!singleResponse.getStatusCode().is2xxSuccessful()) {
                    throw new Exception(String.format("获取单个文件元信息失败，状态码：%d", 
                            singleResponse.getStatusCode().value()));
                }
                
                FileMetaDTO singleFileMeta = singleResponse.getBody();
                if (singleFileMeta == null) {
                    throw new Exception("获取文件元信息失败，返回数据为空");
                }
                
                // 将单个文件元信息转换为列表返回
                List<FileMetaDTO> singleFileList = new ArrayList<>();
                singleFileList.add(singleFileMeta);
                return singleFileList;
            }
            
            log.info("成功获取到{}个文件元信息", fileMetaList.size());
            return fileMetaList;
        } catch (Exception e) {
            log.info("获取多个文件元信息失败，尝试获取单个文件元信息：{}", e.getMessage());
            // 如果获取多个文件元信息失败，尝试获取单个文件元信息
            ResponseEntity<FileMetaDTO> singleResponse = restTemplate.exchange(
                    config.getMetadataUrl(),
                    HttpMethod.valueOf(method),
                    entity,
                    FileMetaDTO.class
            );
            
            if (!singleResponse.getStatusCode().is2xxSuccessful()) {
                throw new Exception(String.format("获取单个文件元信息失败，状态码：%d", 
                        singleResponse.getStatusCode().value()));
            }
            
            FileMetaDTO singleFileMeta = singleResponse.getBody();
            if (singleFileMeta == null) {
                throw new Exception("获取文件元信息失败，返回数据为空");
            }
            
            // 将单个文件元信息转换为列表返回
            List<FileMetaDTO> singleFileList = new ArrayList<>();
            singleFileList.add(singleFileMeta);
            return singleFileList;
        }
    }
    
    /**
     * 获取单个文件元信息（兼容旧版调用）
     * @param config 接口配置
     * @return 文件元信息
     * @throws Exception 异常
     */
    private FileMetaDTO getFileMeta(InterfaceConfig config) throws Exception {
        List<FileMetaDTO> fileMetaList = getFileMetaList(config);
        if (fileMetaList != null && !fileMetaList.isEmpty()) {
            return fileMetaList.get(0);
        }
        return null;
    }
    
    /**
     * 下载分片文件
     * @param config 接口配置
     * @param fileMeta 文件元信息
     * @return 分片文件列表
     * @throws Exception 异常
     */
    private List<File> downloadShards(InterfaceConfig config, FileMetaDTO fileMeta) throws Exception {
        List<File> shardFiles = new ArrayList<>();
        
        // 创建临时目录用于存储分片文件
        String tempDirPath = "d:/archive-temp/shards/" + fileMeta.getFileId();
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        // 逐个下载分片
        for (int i = 0; i < fileMeta.getShardCount(); i++) {
            String shardUrl = fileMeta.getShardUrls().get(i);
            String shardMd5 = fileMeta.getShardMd5s().get(i);
            
            log.info("下载分片{}，URL：{}", i + 1, shardUrl);
            
            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            
            // 1. 解析并添加配置的请求头
            if (StringUtils.hasText(config.getRequestHeaders())) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> customHeaders = objectMapper.readValue(
                            config.getRequestHeaders(), new TypeReference<Map<String, String>>() {});
                    customHeaders.forEach(headers::set);
                } catch (Exception e) {
                    log.warn("解析请求头失败，使用默认请求头：{}", e.getMessage());
                }
            }
            
            // 2. 添加认证信息（如果配置了）
            if (StringUtils.hasText(config.getSecretKey())) {
                if (!headers.containsKey("Authorization")) {
                    headers.set("Authorization", "Bearer " + config.getSecretKey());
                }
            }
            
            // 3. 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 4. 获取请求方法，分片下载默认使用GET
            String method = "GET";
            // 如果配置了请求方法，并且不是GET以外的方法，使用配置的方法
            if (StringUtils.hasText(config.getRequestMethod()) && 
                !"POST".equalsIgnoreCase(config.getRequestMethod()) && 
                !"PUT".equalsIgnoreCase(config.getRequestMethod())) {
                method = config.getRequestMethod();
            }
            
            // 下载分片
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    shardUrl,
                    HttpMethod.valueOf(method),
                    entity,
                    byte[].class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception(String.format("下载分片%d失败，状态码：%d，URL：%s", 
                        i + 1, response.getStatusCode().value(), shardUrl));
            }
            
            byte[] shardData = response.getBody();
            if (shardData == null) {
                throw new Exception(String.format("下载分片%d失败，数据为空，URL：%s", i + 1, shardUrl));
            }
            
            // 校验分片MD5
            String calculatedShardMd5 = DigestUtils.md5Hex(shardData);
            if (!calculatedShardMd5.equals(shardMd5)) {
                throw new Exception(String.format("分片%dMD5校验失败，预期：%s，实际：%s，URL：%s", 
                        i + 1, shardMd5, calculatedShardMd5, shardUrl));
            }
            
            // 保存分片到临时文件
            File shardFile = new File(tempDir, String.format("%s_%d", fileMeta.getFileId(), i));
            try (FileOutputStream fos = new FileOutputStream(shardFile)) {
                fos.write(shardData);
            }
            
            shardFiles.add(shardFile);
            log.info("分片{}下载成功，大小：{}字节", i + 1, shardData.length);
        }
        
        return shardFiles;
    }
    
    /**
     * 合并分片文件
     * @param shardFiles 分片文件列表
     * @param fileName 文件名
     * @return 合并后的文件
     * @throws Exception 异常
     */
    private File mergeFiles(List<File> shardFiles, String fileName) throws Exception {
        // 创建临时目录用于存储合并后的文件
        String tempMergeDirPath = "d:/archive-temp/merge";
        File tempMergeDir = new File(tempMergeDirPath);
        if (!tempMergeDir.exists()) {
            tempMergeDir.mkdirs();
        }
        
        // 创建合并后的文件
        File mergedFile = new File(tempMergeDir, fileName);
        
        // 合并分片
        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (File shardFile : shardFiles) {
                try (FileInputStream fis = new FileInputStream(shardFile)) {
                    byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
        
        return mergedFile;
    }
    
    /**
     * 计算文件MD5
     * @param file 文件
     * @return MD5值
     * @throws Exception 异常
     */
    private String calculateMd5(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        }
    }
    
    /**
     * 从合并文件保存到正式存储
     * @param mergedFile 合并后的文件
     * @param uploadType 上传类型
     * @param originalFilename 原始文件名
     * @return 保存后的文件路径
     * @throws Exception 异常
     */
    private String saveFileFromMerge(File mergedFile, String uploadType, String originalFilename) throws Exception {
        // 确保存储目录存在
        Path storageDir = Paths.get(fileStoragePath, uploadType);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        // 生成唯一文件名，避免重复
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 保存文件
        Path filePath = storageDir.resolve(fileName);
        Files.copy(mergedFile.toPath(), filePath);
        
        return filePath.toString();
    }
    
    /**
     * 保存档案信息
     * @param fileMeta 文件元信息
     * @param filePath 文件路径
     * @param config 接口配置
     * @throws Exception 异常
     */
    private Long saveArchiveInfo(FileMetaDTO fileMeta, String filePath, InterfaceConfig config) throws Exception {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setFileName(fileMeta.getFileName());
        archiveInfo.setFilePath(filePath);
        archiveInfo.setFileType("." + StringUtils.getFilenameExtension(fileMeta.getFileName()));
        archiveInfo.setFileSize(fileMeta.getFileSize());
        archiveInfo.setMd5Value(fileMeta.getTotalMd5());
        archiveInfo.setArchiveType("电子档案");
        archiveInfo.setBusinessNo(fileMeta.getBusinessNo());
        archiveInfo.setBusinessType("自动采集");
        archiveInfo.setResponsiblePerson("system");
        archiveInfo.setDepartment(fileMeta.getDepartment());
        archiveInfo.setHangOnType(0);
        archiveInfo.setStatus(1);
        archiveInfo.setRemark(String.format("从接口采集：%s", config.getInterfaceName()));
        archiveInfo.setCreateTime(LocalDateTime.now());
        archiveInfo.setUpdateTime(LocalDateTime.now());

        // 保存档案信息
        boolean saveResult = archiveInfoService.save(archiveInfo);
        if (!saveResult) {
            throw new Exception("档案信息保存失败");
        }
        
        log.info("档案信息保存成功，档案ID：{}", archiveInfo.getId());
        return archiveInfo.getId();
    }
    
    /**
     * 清理临时文件
     * @param shardFiles 分片文件列表
     * @param mergedFile 合并后的文件
     * @throws Exception 异常
     */
    private void cleanupTempFiles(List<File> shardFiles, File mergedFile) throws Exception {
        // 删除分片文件
        for (File shardFile : shardFiles) {
            if (shardFile.exists()) {
                if (!shardFile.delete()) {
                    log.warn("无法删除分片文件：{}", shardFile.getAbsolutePath());
                }
            }
        }
        
        // 删除合并后的临时文件
        if (mergedFile.exists()) {
            if (!mergedFile.delete()) {
                log.warn("无法删除合并后的临时文件：{}", mergedFile.getAbsolutePath());
            }
        }
        
        // 删除分片所在的临时目录
        if (!shardFiles.isEmpty()) {
            File tempDir = shardFiles.get(0).getParentFile();
            if (tempDir.exists() && tempDir.isDirectory()) {
                // 检查目录是否为空
                if (tempDir.listFiles().length == 0) {
                    if (!tempDir.delete()) {
                        log.warn("无法删除临时目录：{}", tempDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * 从字节数组保存文件到磁盘
     * @param fileData 文件字节数组
     * @param uploadType 上传类型
     * @param originalFilename 原始文件名
     * @return 保存后的文件路径
     */
    private String saveFileFromBytes(byte[] fileData, String uploadType, String originalFilename) throws IOException {
        // 确保存储目录存在
        Path storageDir = Paths.get(fileStoragePath, uploadType);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }
        
        // 生成唯一文件名，避免重复
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 保存文件
        Path filePath = storageDir.resolve(fileName);
        Files.copy(new ByteArrayInputStream(fileData), filePath);
        
        log.info("文件保存成功：{}", filePath);
        return filePath.toString();
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
                    // 记录采集日志（失败）
                    collectionLogService.saveCollectionLog(
                            null, // 档案ID为null，因为保存失败
                            0, // 手动采集
                            responsiblePerson,
                            String.format("手动上传档案：%s", file.getOriginalFilename()),
                            0, // 失败
                            e.getMessage(),
                            1, // totalCount
                            0, // successCount
                            1 // failCount
                    );
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