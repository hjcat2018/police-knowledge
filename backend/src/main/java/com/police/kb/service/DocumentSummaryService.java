package com.police.kb.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 文档摘要服务接口
 * <p>
 * 提供文档摘要的生成、缓存、状态管理等核心功能。
 * 支持同步和异步两种生成方式，配合WebSocket实现实时进度推送。
 * </p>
 * <p>
 * 功能特性：
 * <ul>
 *   <li>同步生成 - 适合小文档的快速摘要</li>
 *   <li>异步生成 - 适合大文档的并发处理</li>
 *   <li>缓存管理 - Redis缓存摘要数据，提升查询性能</li>
 *   <li>状态追踪 - 记录摘要生成进度和状态</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
public interface DocumentSummaryService {

    /**
     * 同步生成文档摘要
     * <p>
     * 直接调用LLM接口生成摘要，适合小文档的快速处理。
     * 不会产生进度回调。
     * </p>
     *
     * @param content 文档原始内容
     * @return 生成的摘要文本，如果输入为空则返回空字符串
     */
    String generateSummary(String content);

    /**
     * 异步生成文档摘要（带进度回调）
     * <p>
     * 采用分块并行处理策略：
     * <ol>
     *   <li>将文档按语义分块（每块约2000字符）</li>
     *   <li>并发调用LLM生成每块摘要</li>
     *   <li>拼接各块摘要形成最终结果</li>
     * </ol>
     * 进度信息通过onProgress回调实时推送。
     * </p>
     *
     * @param documentId 文档ID
     * @param onProgress 进度回调，参数为(current, total)表示当前完成数和总块数
     * @return CompletableFuture，异步返回生成的摘要
     */
    CompletableFuture<String> generateSummaryAsync(Long documentId, BiConsumer<Integer, Integer> onProgress);

    /**
     * 获取缓存的摘要
     * <p>
     * 优先从Redis缓存获取，如果缓存未命中则查询数据库。
     * </p>
     *
     * @param docId 文档ID
     * @return 缓存的摘要文本，如果不存在则返回null
     */
    String getCachedSummary(Long docId);

    /**
     * 设置摘要缓存
     * <p>
     * 将摘要内容写入Redis缓存，TTL为1小时。
     * </p>
     *
     * @param docId 文档ID
     * @param summary 摘要内容
     */
    void setSummaryCache(Long docId, String summary);

    /**
     * 清除摘要缓存
     * <p>
     * 删除指定文档的摘要缓存，通常在重新生成摘要时调用。
     * </p>
     *
     * @param docId 文档ID
     */
    void evictSummaryCache(Long docId);

    /**
     * 获取摘要生成状态
     * <p>
     * 返回当前摘要的生成状态，包括：
     * <ul>
     *   <li>status - pending/processing/completed/failed</li>
     *   <li>current - 已完成的块数</li>
     *   <li>total - 总块数</li>
     *   <li>summary - 生成的摘要内容（如果已完成）</li>
     * </ul>
     * </p>
     *
     * @param docId 文档ID
     * @return 状态信息Map
     */
    Map<String, Object> getSummaryStatus(Long docId);

    /**
     * 更新摘要生成状态
     * <p>
     * 将进度信息写入Redis Hash，实时追踪摘要生成进度。
     * </p>
     *
     * @param docId 文档ID
     * @param status 状态：pending/processing/completed/failed
     * @param current 已完成的块数
     * @param total 总块数
     */
    void updateSummaryStatus(Long docId, String status, int current, int total);

    /**
     * 保存摘要到数据库
     * <p>
     * 事务性地更新document表的摘要字段，
     * 同时更新Redis缓存和状态。
     * </p>
     *
     * @param documentId 文档ID
     * @param summary 摘要内容
     * @param chunks 分块数量
     */
    void saveSummary(Long documentId, String summary, int chunks);

    /**
     * 检查是否需要重新生成摘要
     * <p>
     * 判断当前摘要状态是否为pending或failed，
     * 或者不存在有效摘要时返回true。
     * </p>
     *
     * @param docId 文档ID
     * @return true-需要重新生成，false-已有有效摘要
     */
    boolean needsResummary(Long docId);
}
