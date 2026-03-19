package com.electronic.archive.service.impl;

import com.electronic.archive.constants.ArchiveTypeConstants;
import com.electronic.archive.dto.ChunkUploadDTO;
import com.electronic.archive.dto.MergeChunksDTO;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.service.ChunkUploadService;
import com.electronic.archive.service.SysUserService;
import com.electronic.archive.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 分片上传服务实现类
 */
@Service
@Slf4j
public class ChunkUploadServiceImpl implements ChunkUploadService {
    
    @Autowired
    private ArchiveInfoService archiveInfoService;
    
    @Autowired
    private SysUserService sysUserService;
    
    // 文件存储路径，可在application.yml中配置
    @Value("${archive.file.storage.path:D:/archive-files}")
    private String fileStoragePath;
    
    // 分片临时存储目录
    private static final String CHUNK_TEMP_DIR = "chunk_temp";
    
    @Override
    public ResponseResult<Void> uploadChunk(ChunkUploadDTO chunkUploadDTO) {
        try {
            // 创建临时存储目录
            String tempDir = Paths.get(fileStoragePath, CHUNK_TEMP_DIR, chunkUploadDTO.getFileMd5()).toString();
            File tempDirFile = new File(tempDir);
            if (!tempDirFile.exists()) {
                tempDirFile.mkdirs();
            }
            
            // 分片文件路径
            String chunkFilePath = Paths.get(tempDir, chunkUploadDTO.getChunkIndex().toString()).toString();
            File chunkFile = new File(chunkFilePath);
            
            // 保存分片文件
            chunkUploadDTO.getChunk().transferTo(chunkFile);
            
            log.info("分片上传成功：fileMd5={}, chunkIndex={}", chunkUploadDTO.getFileMd5(), chunkUploadDTO.getChunkIndex());
            return ResponseResult.success("分片上传成功");
        } catch (IOException e) {
            log.error("分片上传失败：", e);
            return ResponseResult.fail("分片上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResponseResult<Void> mergeChunks(MergeChunksDTO mergeChunksDTO) {
        try {
            // 获取临时存储目录
            String tempDir = Paths.get(fileStoragePath, CHUNK_TEMP_DIR, mergeChunksDTO.getFileMd5()).toString();
            File tempDirFile = new File(tempDir);
            
            if (!tempDirFile.exists()) {
                return ResponseResult.fail("分片文件不存在");
            }
            
            // 检查所有分片是否已上传
            File[] chunkFiles = tempDirFile.listFiles();
            if (chunkFiles == null || chunkFiles.length != mergeChunksDTO.getTotalChunks()) {
                return ResponseResult.fail("分片数量不完整");
            }
            
            // 创建最终文件目录
            String finalDir = Paths.get(fileStoragePath, mergeChunksDTO.getUploadType()).toString();
            File finalDirFile = new File(finalDir);
            if (!finalDirFile.exists()) {
                finalDirFile.mkdirs();
            }
            
            // 生成最终文件名
            String fileExtension = StringUtils.getFilenameExtension(mergeChunksDTO.getFileName());
            String finalFileName = mergeChunksDTO.getFileMd5() + "." + fileExtension;
            String finalFilePath = Paths.get(finalDir, finalFileName).toString();
            
            // 合并分片
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(finalFilePath, "rw")) {
                // 按分片索引排序
                List<File> sortedChunkFiles = new ArrayList<>();
                for (File chunkFile : chunkFiles) {
                    sortedChunkFiles.add(chunkFile);
                }
                sortedChunkFiles.sort(Comparator.comparingInt(file -> Integer.parseInt(file.getName())));
                
                // 合并所有分片
                for (File chunkFile : sortedChunkFiles) {
                    byte[] bytes = Files.readAllBytes(chunkFile.toPath());
                    randomAccessFile.write(bytes);
                    chunkFile.delete(); // 删除已合并的分片
                }
            }
            
            // 删除临时目录
            tempDirFile.delete();
            
            // 创建档案信息
            createArchiveInfo(mergeChunksDTO, finalFilePath);
            
            log.info("文件合并成功：fileMd5={}, fileName={}", mergeChunksDTO.getFileMd5(), mergeChunksDTO.getFileName());
            return ResponseResult.success("文件合并成功");
        } catch (IOException e) {
            log.error("文件合并失败：", e);
            return ResponseResult.fail("文件合并失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResponseResult<Boolean> checkChunk(String fileMd5, Integer chunkIndex) {
        try {
            // 检查分片是否已上传
            String tempDir = Paths.get(fileStoragePath, CHUNK_TEMP_DIR, fileMd5).toString();
            String chunkFilePath = Paths.get(tempDir, chunkIndex.toString()).toString();
            File chunkFile = new File(chunkFilePath);
            
            boolean exists = chunkFile.exists();
            log.info("检查分片是否已上传：fileMd5={}, chunkIndex={}, exists={}", fileMd5, chunkIndex, exists);
            return ResponseResult.success("分片检查成功", exists);
        } catch (Exception e) {
            log.error("分片检查失败：", e);
            return ResponseResult.fail("分片检查失败：" + e.getMessage());
        }
    }
    
    @Override
    public ResponseResult<Boolean> checkFile(String fileMd5) {
        try {
            // 检查文件是否已上传完成（在archive_info表中存在）
            // 这里简单实现为检查临时目录是否存在
            String tempDir = Paths.get(fileStoragePath, CHUNK_TEMP_DIR, fileMd5).toString();
            File tempDirFile = new File(tempDir);
            
            boolean exists = tempDirFile.exists();
            log.info("检查文件是否已上传：fileMd5={}, exists={}", fileMd5, exists);
            return ResponseResult.success("文件检查成功", exists);
        } catch (Exception e) {
            log.error("文件检查失败：", e);
            return ResponseResult.fail("文件检查失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建档案信息
     * @param mergeChunksDTO 合并分片请求参数
     * @param filePath 文件路径
     */
    private void createArchiveInfo(MergeChunksDTO mergeChunksDTO, String filePath) {
        try {
            // 获取当前登录用户的昵称作为责任人
            String responsiblePerson = "user";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                // 这里假设SysUserService有getByUsername方法
                // SysUser user = sysUserService.getByUsername(username);
                // if (user != null) {
                //     responsiblePerson = user.getNickname() != null ? user.getNickname() : user.getUsername();
                // }
            }
            
            // 创建档案信息
            ArchiveInfo archiveInfo = new ArchiveInfo();
            archiveInfo.setFileName(mergeChunksDTO.getFileName());
            archiveInfo.setFilePath(filePath);
            archiveInfo.setFileType("." + StringUtils.getFilenameExtension(mergeChunksDTO.getFileName()));
            archiveInfo.setFileSize(mergeChunksDTO.getTotalSize());
            archiveInfo.setMd5Value(mergeChunksDTO.getFileMd5());
            archiveInfo.setArchiveType(ArchiveTypeConstants.getTypeNameByTypeId(mergeChunksDTO.getArchiveType()));
            archiveInfo.setBusinessNo(mergeChunksDTO.getUploadType().toUpperCase() + "_" + System.currentTimeMillis());
            archiveInfo.setBusinessType(getBusinessType(mergeChunksDTO.getUploadType()));
            archiveInfo.setResponsiblePerson(responsiblePerson);
            archiveInfo.setHangOnType(1);
            archiveInfo.setStatus(2);
            archiveInfo.setRemark(mergeChunksDTO.getUploadType() + "上传档案");
            archiveInfo.setCreateTime(LocalDateTime.now());
            archiveInfo.setUpdateTime(LocalDateTime.now());

            archiveInfoService.save(archiveInfo);
            
            log.info("档案信息创建成功：fileId={}, fileName={}", archiveInfo.getId(), archiveInfo.getFileName());
        } catch (Exception e) {
            log.error("档案信息创建失败：", e);
        }
    }
    
    /**
     * 根据上传类型获取业务类型
     * @param uploadType 上传类型
     * @return 业务类型
     */
    private String getBusinessType(String uploadType) {
        switch (uploadType) {
            case "manual":
                return "手动上传";
            case "batch":
                return "批量上传";
            case "external":
                return "外部导入";
            default:
                return "未知上传类型";
        }
    }
}