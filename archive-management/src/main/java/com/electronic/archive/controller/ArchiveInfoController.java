package com.electronic.archive.controller;

import com.electronic.archive.dto.ArchiveQueryDTO;
import com.electronic.archive.entity.ArchiveInfo;
import com.electronic.archive.service.ArchiveInfoService;
import com.electronic.archive.util.PageResult;
import com.electronic.archive.vo.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 档案信息控制器
 * 提供档案信息查询API接口
 */
@RestController
@RequestMapping("/info")
@Tag(name = "档案信息管理", description = "提供档案信息查询相关的API接口")
public class ArchiveInfoController {

    @Autowired
    private ArchiveInfoService archiveInfoService;

    /**
     * 分页查询档案信息
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询档案信息", description = "根据条件分页查询档案信息")
    public ResponseResult<PageResult<ArchiveInfo>> queryArchiveByPage(@RequestBody ArchiveQueryDTO queryDTO) {
        try {
            var pageResult = archiveInfoService.queryArchiveByPage(queryDTO);
            
            // 转换为统一的分页响应格式
            PageResult<ArchiveInfo> result = PageResult.fromMpPage(pageResult);
            
            return ResponseResult.success("查询档案信息成功", result);
        } catch (Exception e) {
            return ResponseResult.fail("查询档案信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询档案详情
     * @param id 档案ID
     * @return 档案详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询档案详情", description = "根据档案ID查询档案详情")
    public ResponseResult<ArchiveInfo> getArchiveById(@PathVariable Long id) {
        try {
            ArchiveInfo archiveInfo = archiveInfoService.getById(id);
            if (archiveInfo == null) {
                return ResponseResult.fail("档案不存在");
            }
            return ResponseResult.success("查询档案详情成功", archiveInfo);
        } catch (Exception e) {
            return ResponseResult.fail("查询档案详情失败: " + e.getMessage());
        }
    }



    /**
     * 下载档案文件
     * @param id 档案ID
     * @return 文件内容
     */
    @GetMapping("/download")
    @Operation(summary = "下载档案文件", description = "根据档案ID下载档案文件")
    public ResponseEntity<StreamingResponseBody> downloadArchive(@RequestParam Long id) {
        try {
            ArchiveInfo archiveInfo = archiveInfoService.getById(id);
            if (archiveInfo == null) {
                ResponseResult<String> result = ResponseResult.fail("档案不存在");
                return ResponseEntity.status(HttpStatus.OK).body(outputStream -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(outputStream, result);
                });
            }

            // 根据文件路径获取文件
            String filePath = archiveInfo.getFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                String errorMsg = "文件不存在: " + filePath;
                ResponseResult<String> result = ResponseResult.fail(errorMsg);
                return ResponseEntity.status(HttpStatus.OK).body(outputStream -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(outputStream, result);
                });
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            // 获取文件名和后缀
            String fileName = archiveInfo.getFileName();
            String actualFileName = fileName;

            // 如果文件名没有后缀，从文件路径中提取
            if (!fileName.contains(".")) {
                String filePathTwo = archiveInfo.getFilePath();
                if (filePathTwo != null && filePathTwo.contains(".")) {
                    String fileExtension = filePathTwo.substring(filePathTwo.lastIndexOf(".") + 1).toLowerCase();
                    // 给文件名添加后缀
                    actualFileName = fileName + "." + fileExtension;
                }
            }

            // 设置为attachment，让浏览器提示下载文件
            headers.setContentDispositionFormData("attachment", actualFileName);

            // 使用StreamingResponseBody实现流式传输，避免一次性加载大文件到内存
            StreamingResponseBody responseBody = outputStream -> {
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                    // 增大缓冲区大小以提高传输效率，适应大文件传输
                    byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲区
                    int bytesRead;
                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        // 刷新输出流，确保数据及时发送
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    // 记录错误日志，但不抛出异常，避免影响响应
                    System.err.println("文件传输错误: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);
        } catch (Exception e) {
            String errorMsg = "下载失败: " + e.getMessage();
            ResponseResult<String> result = ResponseResult.fail(errorMsg);
            StreamingResponseBody responseBody = outputStream -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(outputStream, result);
            };
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        }
    }

    /**
     * 预览档案文件
     * @param id 档案ID
     * @return 文件内容
     */
    @GetMapping("/preview")
    @Operation(summary = "预览档案文件", description = "根据档案ID 预览档案文件")
    public ResponseEntity<StreamingResponseBody> previewArchive(@RequestParam Long id) {
        try {
            ArchiveInfo archiveInfo = archiveInfoService.getById(id);
            if (archiveInfo == null) {
                ResponseResult<String> result = ResponseResult.fail("档案不存在");
                return ResponseEntity.status(HttpStatus.OK).body(outputStream -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(outputStream, result);
                });
            }

            // 根据文件路径获取文件
            String filePath = archiveInfo.getFilePath();
            File file = new File(filePath);

            if (!file.exists()) {
                String errorMsg = "文件不存在：" + filePath;
                ResponseResult<String> result = ResponseResult.fail(errorMsg);
                return ResponseEntity.status(HttpStatus.OK).body(outputStream -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(outputStream, result);
                });
            }

            // 获取文件名和后缀
            String fileName = archiveInfo.getFileName();
            String actualFileName = fileName;

            // 如果文件名没有后缀，从文件路径中提取
            if (!fileName.contains(".")) {
                String filePathTwo = archiveInfo.getFilePath();
                if (filePathTwo != null && filePathTwo.contains(".")) {
                    String fileExtension = filePathTwo.substring(filePathTwo.lastIndexOf(".") + 1).toLowerCase();
                    // 给文件名添加后缀
                    actualFileName = fileName + "." + fileExtension;
                }
            }

            // 从实际文件名中获取扩展名（更可靠）
            String fileExtension = "";
            if (actualFileName.contains(".")) {
                fileExtension = actualFileName.substring(actualFileName.lastIndexOf(".") + 1).toLowerCase();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();

            // 设置 Content-Disposition 响应头，inline 表示在浏览器中显示
            headers.setContentDisposition(org.springframework.http.ContentDisposition
                    .inline()
                    .filename(actualFileName, StandardCharsets.UTF_8)
                    .build());

            // 根据文件扩展名设置 Content-Type
            MediaType mediaType = getMediaTypeByExtension(fileExtension);
            if (mediaType != null) {
                headers.setContentType(mediaType);
            } else {
                // 未知类型，尝试自动检测
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            // 使用StreamingResponseBody实现流式传输，避免一次性加载大文件到内存
            StreamingResponseBody responseBody = outputStream -> {
                try (FileInputStream fileInputStream = new FileInputStream(file);
                     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                    // 增大缓冲区大小以提高传输效率，适应大文件传输
                    byte[] buffer = new byte[1024 * 1024]; // 1MB缓冲区
                    int bytesRead;
                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        // 刷新输出流，确保数据及时发送
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    // 记录错误日志，但不抛出异常，避免影响响应
                    System.err.println("文件传输错误: " + e.getMessage());
                }
            };

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);
        } catch (Exception e) {
            String errorMsg = "预览失败：" + e.getMessage();
            ResponseResult<String> result = ResponseResult.fail(errorMsg);
            StreamingResponseBody responseBody = outputStream -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(outputStream, result);
            };
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        }
    }

    /**
     * 根据文件扩展名获取 Media Type
     * @param extension 文件扩展名
     * @return Media Type
     */
    private MediaType getMediaTypeByExtension(String extension) {
        if (extension == null) {
            return null;
        }

        switch (extension.toLowerCase()) {
            // PDF - 浏览器直接预览
            case "pdf":
                return MediaType.APPLICATION_PDF;

            // 图片 - 浏览器直接预览
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "bmp":
                return new MediaType("image", "bmp");
            case "webp":
                return new MediaType("image", "webp");

            // 文本 - 浏览器直接预览
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "html":
            case "htm":
                return MediaType.TEXT_HTML;
            case "xml":
                return MediaType.APPLICATION_XML;
            case "json":
                return MediaType.APPLICATION_JSON;
            case "csv":
                return new MediaType("text", "csv");

            // Office 文档 - 浏览器可能会下载，取决于浏览器配置和插件
            case "doc":
                return new MediaType("application", "msword");
            case "docx":
                return new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls":
                return new MediaType("application", "vnd.ms-excel");
            case "xlsx":
                return new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "ppt":
                return new MediaType("application", "vnd.ms-powerpoint");
            case "pptx":
                return new MediaType("application", "vnd.openxmlformats-officedocument.presentationml.presentation");

            // 其他类型
            default:
                return null;
        }
    }

}