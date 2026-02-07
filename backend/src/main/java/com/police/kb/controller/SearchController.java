package com.police.kb.controller;

import com.police.kb.common.Result;
import com.police.kb.domain.vo.SearchResult;
import com.police.kb.service.VectorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final VectorService vectorService;

    /**
     * 语义搜索
     * <p>
     * 使用向量相似度进行语义搜索，返回最相关的文档列表。
     * 支持按知识库、分类、归属地和来源部门进行过滤。
     * </p>
     *
     * @param request 搜索请求参数
     * @return 搜索结果列表
     */
    @PostMapping("/semantic")
    public Result<List<SearchResult>> semanticSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = vectorService.semanticSearch(
            request.getKeyword(),
            request.getKbId(),
            request.getCategoryId(),
            request.getOriginScope(),
            request.getOriginDepartment(),
            request.getTopK()
        );
        return Result.success(results);
    }

    /**
     * 混合搜索
     * <p>
     * 结合向量搜索和关键词搜索，返回综合评分最高的文档列表。
     * 支持按知识库、分类、归属地和来源部门进行过滤。
     * </p>
     *
     * @param request 搜索请求参数
     * @return 搜索结果列表
     */
    @PostMapping("/hybrid")
    public Result<List<SearchResult>> hybridSearch(@Valid @RequestBody SearchRequest request) {
        List<SearchResult> results = vectorService.hybridSearch(
            request.getKeyword(),
            request.getKbId(),
            request.getCategoryId(),
            request.getOriginScope(),
            request.getOriginDepartment(),
            request.getTopK()
        );
        return Result.success(results);
    }

    /**
     * 搜索请求类
     */
    @lombok.Data
    public static class SearchRequest {
        @NotBlank(message = "搜索关键词不能为空")
        private String keyword;

        private Long kbId;

        private Long categoryId;

        private String originScope;

        private String originDepartment;

        @Min(value = 1, message = "topK最小值为1")
        @Max(value = 100, message = "topK最大值为100")
        private int topK = 10;
    }
}
