package com.police.kb.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.police.kb.common.Result;
import com.police.kb.domain.entity.PromptTemplate;
import com.police.kb.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "提示模板管理")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    @Operation(summary = "查询模板列表")
    @GetMapping
    public Result<List<PromptTemplate>> list(
            @RequestParam(defaultValue = "my") String type) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<PromptTemplate> templates = templateService.listTemplates(type, userId);
        return Result.success(templates);
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<PromptTemplate> getById(@PathVariable Long id) {
        PromptTemplate template = templateService.getById(id);
        return Result.success(template);
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CreateTemplateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        PromptTemplate template = new PromptTemplate();
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setDescription(request.getDescription());
        template.setSort(request.getSort() != null ? request.getSort() : 0);
        template.setIsDefault(0);
        template.setIsSystem(0);

        Long id = templateService.create(template, userId);
        return Result.success(id);
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, 
                               @RequestBody @Valid CreateTemplateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        PromptTemplate template = new PromptTemplate();
        template.setName(request.getName());
        template.setContent(request.getContent());
        template.setVariables(request.getVariables());
        template.setDescription(request.getDescription());
        template.setSort(request.getSort() != null ? request.getSort() : 0);

        templateService.update(id, template, userId);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        templateService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "设为默认模板")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        templateService.setDefault(id, userId);
        return Result.success();
    }

    @Data
    public static class CreateTemplateRequest {
        @NotBlank(message = "模板名称不能为空")
        private String name;

        @NotBlank(message = "模板内容不能为空")
        private String content;

        private String variables;

        private String description;

        private Integer sort;
    }
}
