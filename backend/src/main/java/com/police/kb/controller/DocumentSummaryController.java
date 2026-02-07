package com.police.kb.controller;

import com.police.kb.common.Result;
import com.police.kb.domain.entity.Document;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.service.DocumentSummaryService;
import com.police.kb.websocket.SummaryProgressWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 文档摘要API控制器
 * <p>
 * 提供文档摘要的生成、查询、重新生成等RESTful接口。
 * 支持同步和异步两种生成模式，配合WebSocket实现进度推送。
 * </p>
 * <p>
 * API端点：
 * <ul>
 *   <li>POST /api/v1/summary/sync - 同步生成摘要</li>
 *   <li>POST /api/v1/summary/async/{docId} - 异步生成摘要</li>
 *   <li>GET /api/v1/summary/status/{docId} - 查询摘要状态</li>
 *   <li>POST /api/v1/summary/regenerate/{docId} - 重新生成摘要</li>
 *   <li>POST /api/v1/summary/batch - 批量生成摘要</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/summary")
@RequiredArgsConstructor
public class DocumentSummaryController {

    private final DocumentSummaryService summaryService;
    private final DocumentMapper documentMapper;
    private final SummaryProgressWebSocket summaryProgressWebSocket;

    /**
     * 同步生成摘要
     * <p>
     * 直接调用LLM接口生成摘要，适合小文档的快速处理。
     * 请求参数为文档原始内容，返回生成的摘要文本。
     * </p>
     * <p>
     * 示例请求：
     * <pre>
     * POST /api/v1/summary/sync
     * Content-Type: application/json
     * {
     *   "content": "文档原始内容..."
     * }
     * </pre>
     * </p>
     *
     * @param request 摘要请求，包含文档内容
     * @return Result包含生成的摘要文本
     */
    @PostMapping(value = "/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<String> generateSummary(@RequestBody SummaryRequest request) {
        try {
            String summary = summaryService.generateSummary(request.getContent());
            return Result.success(summary);
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return Result.error("生成摘要失败: " + e.getMessage());
        }
    }

    /**
     * 异步生成摘要
     * <p>
     * 启动异步任务生成摘要，立即返回任务状态。
     * 通过WebSocket推送生成进度，完成后推送最终结果。
     * </p>
     * <p>
     * WebSocket端点：ws://host/ws/summary/progress/{docId}
     * </p>
     * <p>
     * 示例请求：
     * <pre>
     * POST /api/v1/summary/async/123
     * </pre>
     * </p>
     *
     * @param docId 文档ID
     * @return Result包含docId、status、message
     */
    @PostMapping("/async/{docId}")
    public Result<Map<String, Object>> generateSummaryAsync(@PathVariable Long docId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            return Result.error("文档不存在");
        }

        BiConsumer<Integer, Integer> progressCallback = (current, total) ->
            summaryProgressWebSocket.sendProgress(docId, current, total);

        summaryService.generateSummaryAsync(docId, progressCallback)
            .thenAccept(summary -> summaryProgressWebSocket.sendComplete(docId, summary))
            .exceptionally(e -> {
                log.error("异步摘要生成异常: docId={}", docId, e);
                summaryProgressWebSocket.sendError(docId, e.getMessage());
                return null;
            });

        Map<String, Object> result = new HashMap<>();
        result.put("docId", docId);
        result.put("status", "processing");
        result.put("message", "摘要生成任务已启动，请通过WebSocket监听进度");

        return Result.success(result);
    }

    /**
     * 获取摘要状态
     * <p>
     * 查询指定文档的摘要生成状态，包括：
     * <ul>
     *   <li>status - pending/processing/completed/failed</li>
     *   <li>chunks - 分块数量</li>
     *   <li>summary - 摘要内容（如果已完成）</li>
     * </ul>
     * </p>
     * <p>
     * 示例请求：
     * <pre>
     * GET /api/v1/summary/status/123
     * </pre>
     * </p>
     *
     * @param docId 文档ID
     * @return Result包含状态信息
     */
    @GetMapping("/status/{docId}")
    public Result<Map<String, Object>> getSummaryStatus(@PathVariable Long docId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            return Result.error("文档不存在");
        }

        Map<String, Object> status = summaryService.getSummaryStatus(docId);
        return Result.success(status);
    }

    /**
     * 重新生成摘要
     * <p>
     * 清除现有缓存，重新启动摘要生成任务。
     * </p>
     * <p>
     * 示例请求：
     * <pre>
     * POST /api/v1/summary/regenerate/123
     * </pre>
     * </p>
     *
     * @param docId 文档ID
     * @return Result包含任务状态
     */
    @PostMapping("/regenerate/{docId}")
    public Result<Map<String, Object>> regenerateSummary(@PathVariable Long docId) {
        summaryService.evictSummaryCache(docId);
        return generateSummaryAsync(docId);
    }

    /**
     * 批量生成摘要
     * <p>
     * 为多个文档启动摘要生成任务。
     * 每个任务独立执行，互不影响。
     * </p>
     * <p>
     * 示例请求：
     * <pre>
     * POST /api/v1/summary/batch
     * {
     *   "docIds": [123, 456, 789]
     * }
     * </pre>
     * </p>
     *
     * @param request 批量请求，包含文档ID列表
     * @return Result包含每个文档的处理结果
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchGenerate(@RequestBody BatchSummaryRequest request) {
        Map<String, Object> results = new HashMap<>();
        int success = 0;
        int fail = 0;

        for (Long docId : request.getDocIds()) {
            try {
                if (summaryService.needsResummary(docId)) {
                    summaryService.generateSummaryAsync(docId, null);
                }
                results.put("doc_" + docId, "started");
                success++;
            } catch (Exception e) {
                results.put("doc_" + docId, "error: " + e.getMessage());
                fail++;
            }
        }

        results.put("success", success);
        results.put("fail", fail);

        return Result.success(results);
    }

    /**
     * 同步生成摘要请求
     * <p>
     * 用于传递待摘要的文档原始内容。
     * </p>
     */
    @lombok.Data
    public static class SummaryRequest {
        /**
         * 文档原始内容
         */
        private String content;
    }

    /**
     * 批量生成摘要请求
     * <p>
     * 用于传递需要生成摘要的文档ID列表。
     * </p>
     */
    @lombok.Data
    public static class BatchSummaryRequest {
        /**
         * 文档ID列表
         */
        private List<Long> docIds;
    }
}
