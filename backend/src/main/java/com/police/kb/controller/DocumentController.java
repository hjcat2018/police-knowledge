package com.police.kb.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.kb.common.Result;
import com.police.kb.domain.dto.DocumentDTO;
import com.police.kb.domain.entity.Document;
import com.police.kb.domain.vo.DocumentVO;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.service.DocumentService;
import com.police.kb.service.FileParseService;
import com.police.kb.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.util.List;

@Slf4j
@Tag(name = "文档管理")
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final FileParseService fileParseService;
    private final MinioService minioService;

    /**
     * 分页查询文档列表
     *
     * @param pageNum          页码
     * @param pageSize         每页数量
     * @param kbId             知识库ID（可选）
     * @param keyword          搜索关键词（可选）
     * @param originScope      归属地过滤（可选）
     * @param originDepartment 来源部门过滤（可选）
     * @return 分页文档列表
     */
    @Operation(summary = "分页查询文档列表")
    @GetMapping
    public Result<IPage<DocumentVO>> pageDocuments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long kbId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String originScope,
            @RequestParam(required = false) String originDepartment) {
        return Result.success(documentService.pageDocuments(pageNum, pageSize, kbId, keyword, originScope, originDepartment));
    }

    /**
     * 获取文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    @Operation(summary = "获取文档详情")
    @GetMapping("/{id}")
    public Result<DocumentVO> getDocumentById(@PathVariable Long id) {
        return Result.success(documentService.getDocumentById(id));
    }

    /**
     * 创建文档
     *
     * @param dto 文档信息
     * @return 创建的文档
     */
    @Operation(summary = "创建文档")
    @PostMapping
    public Result<Document> createDocument(@Valid @RequestBody DocumentDTO dto) {
        return Result.success(documentService.createDocument(dto));
    }

    /**
     * 更新文档
     *
     * @param id  文档ID
     * @param dto 文档信息
     * @return 更新后的文档
     */
    @Operation(summary = "更新文档")
    @PutMapping("/{id}")
    public Result<Document> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDTO dto) {
        return Result.success(documentService.updateDocument(id, dto));
    }

    /**
     * 删除文档
     *
     * @param id 文档ID
     * @return 操作结果
     */
    @Operation(summary = "删除文档")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 批量删除文档
     *
     * @param ids 文档ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除文档")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteDocuments(@RequestBody List<Long> ids) {
        documentService.batchDeleteDocuments(ids);
        return Result.success();
    }

    /**
     * 更改文档状态
     *
     * @param id     文档ID
     * @param status 状态
     * @return 操作结果
     */
    @Operation(summary = "更改文档状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        documentService.changeStatus(id, status);
        return Result.success();
    }

    /**
     * 获取热门文档
     *
     * @return 热门文档列表
     */
    @Operation(summary = "获取热门文档")
    @GetMapping("/hot")
    public Result<List<DocumentVO>> getHotDocuments() {
        return Result.success(documentService.getHotDocuments());
    }

    /**
     * 获取最新文档
     *
     * @return 最新文档列表
     */
    @Operation(summary = "获取最新文档")
    @GetMapping("/recent")
    public Result<List<DocumentVO>> getRecentDocuments() {
        return Result.success(documentService.getRecentDocuments());
    }

    /**
     * 浏览文档
     *
     * @param id 文档ID
     * @return 操作结果
     */
    @Operation(summary = "浏览文档")
    @PostMapping("/{id}/view")
    public Result<Void> viewDocument(@PathVariable Long id) {
        documentService.incrementViewCount(id);
        return Result.success();
    }

    /**
     * 解析文件内容
     *
     * @param file 上传的文件
     * @return 文件解析后的文本内容
     */
    @Operation(summary = "解析文件内容")
    @PostMapping(value = "/parse-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> parseFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileParseService.isSupported(fileName)) {
            return Result.error("不支持的文件类型，仅支持: doc, docx, xls, xlsx, pdf, txt, md, ppt, pptx");
        }

        try {
            String content = fileParseService.parse(fileName, file.getBytes());
            log.info("文件解析完成: {}, 内容长度: {}", fileName, content.length());
            return Result.success(content);
        } catch (Exception e) {
            log.error("文件解析失败: {}", e.getMessage(), e);
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析文件并上传到MinIO
     * 同时上传文件到MinIO和解析文件内容，返回MinIO地址、来源时间和解析内容
     *
     * @param file 上传的文件
     * @return MinIO地址、来源时间和解析内容
     */
    @Operation(summary = "解析文件并上传到MinIO")
    @PostMapping(value = "/parse-and-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ParseAndUploadResult> parseAndUploadFile(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileParseService.isSupported(fileName)) {
            return Result.error("不支持的文件类型，仅支持: doc, docx, xls, xlsx, pdf, txt, md, ppt, pptx");
        }

        try {
            MinioService.DocumentFileInfo fileInfo = minioService.uploadFile(file);

            String content = fileParseService.parse(fileName, file.getBytes());
            log.info("文件解析并上传完成: {}, URL: {}, 内容长度: {}", fileName, fileInfo.fileUrl(), content.length());

            ParseAndUploadResult result = new ParseAndUploadResult(
                    fileInfo.fileUrl(),
                    fileInfo.sourceTime().toString(),
                    content);
            return Result.success(result);
        } catch (Exception e) {
            log.error("文件解析或上传失败: {}", e.getMessage(), e);
            return Result.error("文件处理失败: " + e.getMessage());
        }
    }

    public record ParseAndUploadResult(String docUrl, String sourceTime, String content) {
    }

    /**
     * 下载文档文件
     * 从MinIO获取文件流并返回，强制浏览器下载而不是打开
     *
     * @param id 文档ID
     * @return 文件流
     */
    @Operation(summary = "下载文档文件")
    @SaIgnore
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null || doc.getDocUrl() == null || doc.getDocUrl().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String docUrl = doc.getDocUrl();
            MinioService.DocumentFileInfo fileInfo = minioService.getFileInfo(docUrl);

            byte[] bytes = minioService.downloadFile(fileInfo.objectName());

            String fileName = doc.getTitle();
            String extension = getFileExtension(docUrl);
            if (!extension.isEmpty() && !fileName.toLowerCase().endsWith(extension.toLowerCase())) {
                fileName = fileName + extension;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "UTF-8"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * 创建文档并解析文件
     *
     * @param title      文档标题
     * @param summary    文档摘要
     * @param kbId       知识库ID
     * @param categoryId 分类ID
     * @param tags       标签
     * @param file       上传的文件
     * @param content    文档内容
     * @return 创建的文档
     */
    @Operation(summary = "创建文档并解析文件")
    @PostMapping(value = "/create-with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Document> createDocumentWithFile(
            @RequestParam("title") String title,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam("kbId") Long kbId,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "content", required = false) String content) {

        if (title == null || title.isBlank()) {
            return Result.error("文档标题不能为空");
        }
        if (kbId == null) {
            return Result.error("请选择知识库");
        }

        StringBuilder fullContent = new StringBuilder();
        if (content != null && !content.isBlank()) {
            fullContent.append(content);
        }

        // 解析上传的文件
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileParseService.isSupported(fileName)) {
                try {
                    String fileContent = fileParseService.parse(fileName, file.getBytes());
                    if (fileContent != null && !fileContent.isEmpty()) {
                        if (fullContent.length() > 0) {
                            fullContent.append("\n\n");
                        }
                        fullContent.append(fileContent);
                    }
                    log.info("文件解析完成: {}, 内容长度: {}", fileName, fileContent != null ? fileContent.length() : 0);
                } catch (Exception e) {
                    log.error("文件解析失败: {}", e.getMessage(), e);
                    return Result.error("文件解析失败: " + e.getMessage());
                }
            }
        }

        DocumentDTO dto = new DocumentDTO();
        dto.setTitle(title);
        dto.setSummary(summary);
        dto.setKbId(kbId);
        dto.setCategoryId(categoryId);
        dto.setTags(tags);
        dto.setContent(fullContent.toString());
        dto.setStatus(1); // 默认发布

        Document doc = documentService.createDocument(dto);
        return Result.success(doc);
    }
}
