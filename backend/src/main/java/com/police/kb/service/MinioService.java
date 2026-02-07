package com.police.kb.service;

import com.police.kb.config.MinioConfig;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO文件服务
 * <p>
 * 提供文件的上传、下载、删除等操作。
 * 基于MinIO对象存储实现文件的云端管理。
 * </p>
 * <p>
 * 功能特性：
 * <ul>
 *   <li>文件上传 - 支持多种文件类型</li>
 *   <li>文件下载 - 获取文件字节数组</li>
 *   <li>文件删除 - 从存储中移除文件</li>
 *   <li>文件信息查询 - 获取文件元数据</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 日期格式化模板
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd";

    /**
     * 上传文件
     * <p>
     * 将上传的文件保存到MinIO存储桶中，
     * 自动创建桶（如不存在），生成唯一对象名称。
     * </p>
     *
     * @param file 上传的文件
     * @return 文件信息（URL、存储时间、对象名称）
     */
    public DocumentFileInfo uploadFile(MultipartFile file) {
        String bucketName = minioConfig.getBucketName();
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String objectName = generateObjectName(fileExtension);

        try {
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("创建MinIO桶: {}", bucketName);
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String fileUrl = minioConfig.getEndpoint() + "/" + bucketName + "/" + objectName;
            LocalDateTime sourceTime = LocalDateTime.now();

            log.info("文件上传成功: {}, URL: {}", objectName, fileUrl);

            return new DocumentFileInfo(fileUrl, sourceTime, objectName);

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     * <p>
     * 从MinIO存储中删除指定的对象。
     * </p>
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     * <p>
     * 从MinIO存储中获取文件的字节数组。
     * </p>
     *
     * @param objectName 对象名称
     * @return 文件的字节数组
     */
    public byte[] downloadFile(String objectName) {
        try {
            InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件信息
     * <p>
     * 根据文件URL解析并返回文件信息。
     * </p>
     *
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    public DocumentFileInfo getFileInfo(String fileUrl) {
        String bucketName = minioConfig.getBucketName();
        String objectName = fileUrl.replace(minioConfig.getEndpoint() + "/" + bucketName + "/", "");
        return new DocumentFileInfo(fileUrl, LocalDateTime.now(), objectName);
    }

    /**
     * 生成对象名称
     * <p>
     * 根据当前日期和UUID生成唯一的对象存储名称。
     * 格式：documents/yyyy/MM/dd/{uuid}.{ext}
     * </p>
     *
     * @param fileExtension 文件扩展名
     * @return 生成的对象名称
     */
    private String generateObjectName(String fileExtension) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String uniqueId = UUID.randomUUID().toString().replace("-", "");
        return "documents/" + datePath + "/" + uniqueId + fileExtension;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（包含点号）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 文档文件信息
     * <p>
     * 用于在系统中传递文件相关数据。
     * </p>
     *
     * @param fileUrl 文件访问URL
     * @param sourceTime 文件上传时间
     * @param objectName MinIO对象名称
     */
    public record DocumentFileInfo(String fileUrl, LocalDateTime sourceTime, String objectName) {
    }
}
