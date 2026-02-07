package com.police.kb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.police.kb.common.Result;
import com.police.kb.domain.dto.KnowledgeBaseDTO;
import com.police.kb.domain.entity.KnowledgeBase;
import com.police.kb.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库管理控制器
 * <p>
 * 处理知识库的CRUD操作，包括知识库分页查询、创建、修改、删除、状态管理等。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Tag(name = "知识库管理")
@RestController
@RequestMapping("/api/v1/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 分页查询知识库列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键词
     * @return 分页知识库列表
     */
    @Operation(summary = "分页查询知识库列表")
    @GetMapping
    public Result<IPage<KnowledgeBase>> pageKnowledgeBases(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(knowledgeBaseService.pageKnowledgeBases(pageNum, pageSize, keyword));
    }

    /**
     * 获取所有知识库列表
     *
     * @return 知识库列表
     */
    @Operation(summary = "获取所有知识库")
    @GetMapping("/list")
    public Result<List<KnowledgeBase>> listKnowledgeBases() {
        return Result.success(knowledgeBaseService.listKnowledgeBases());
    }

    /**
     * 获取知识库详情
     *
     * @param id 知识库ID
     * @return 知识库详情
     */
    @Operation(summary = "获取知识库详情")
    @GetMapping("/{id}")
    public Result<KnowledgeBase> getKnowledgeBaseById(@PathVariable Long id) {
        return Result.success(knowledgeBaseService.getKnowledgeBaseById(id));
    }

    /**
     * 创建知识库
     *
     * @param dto 知识库信息
     * @return 创建的知识库
     */
    @Operation(summary = "创建知识库")
    @PostMapping
    public Result<KnowledgeBase> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseDTO dto) {
        return Result.success(knowledgeBaseService.createKnowledgeBase(dto));
    }

    /**
     * 更新知识库
     *
     * @param id  知识库ID
     * @param dto 知识库信息
     * @return 更新后的知识库
     */
    @Operation(summary = "更新知识库")
    @PutMapping("/{id}")
    public Result<KnowledgeBase> updateKnowledgeBase(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeBaseDTO dto) {
        return Result.success(knowledgeBaseService.updateKnowledgeBase(id, dto));
    }

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     * @return 操作结果
     */
    @Operation(summary = "删除知识库")
    @DeleteMapping("/{id}")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return Result.success();
    }

    /**
     * 批量删除知识库
     *
     * @param ids 知识库ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除知识库")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteKnowledgeBases(@RequestBody List<Long> ids) {
        knowledgeBaseService.batchDeleteKnowledgeBases(ids);
        return Result.success();
    }

    /**
     * 更改知识库状态
     *
     * @param id     知识库ID
     * @param status 状态
     * @return 操作结果
     */
    @Operation(summary = "更改知识库状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        knowledgeBaseService.changeStatus(id, status);
        return Result.success();
    }
}
