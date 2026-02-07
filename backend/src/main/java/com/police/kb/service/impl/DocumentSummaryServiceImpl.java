package com.police.kb.service.impl;

import com.police.kb.domain.entity.Document;
import com.police.kb.mapper.DocumentChunksMapper;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.service.DocumentSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 文档摘要服务实现类
 * <p>
 * 提供文档摘要的生成、缓存、状态管理等核心功能实现。
 * 采用异步分块并行处理策略，支持WebSocket实时进度推送。
 * </p>
 * <p>
 * 技术特性：
 * <ul>
 *   <li>异步处理 - 使用@Async注解实现非阻塞异步执行</li>
 *   <li>并发优化 - CompletableFuture并行调用LLM</li>
 *   <li>缓存策略 - Redis缓存摘要和状态信息</li>
 *   <li>进度追踪 - Redis Hash存储实时进度</li>
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
public class DocumentSummaryServiceImpl implements DocumentSummaryService {

    private final DocumentMapper documentMapper;
    private final DocumentChunksMapper documentChunksMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * DashScope API密钥
     */
    @Value("${spring.ai.dashscope.api-key:sk-e8d86d11e21540d9b72bc83f78bf1154}")
    private String apiKey;

    /**
     * LLM Chat API地址
     */
    private static final String CHAT_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /**
     * 默认使用的LLM模型
     */
    private static final String DEFAULT_MODEL = "deepseek-chat";

    /**
     * 摘要生成提示词模板
     * <p>
     * 使用字数为约束，生成简洁明了的法律文档摘要。
     * </p>
     */
    private static final String SUMMARY_PROMPT = """
            请用%d字以内总结以下内容，要求：
            1. 保留核心要点
            2. 语言简洁明了
            3. 适用于后续法律问答参考

            内容：
            %s
            """;

    /**
     * 摘要缓存Key前缀
     */
    private static final String SUMMARY_CACHE_KEY = "police:kb:summary:";

    /**
     * 摘要状态缓存Key前缀
     */
    private static final String SUMMARY_STATUS_KEY = "police:kb:summary:status:";

    /**
     * 缓存过期时间（秒）- 1小时
     */
    private static final long SUMMARY_CACHE_TTL = 3600L;

    /**
     * 分块大小（字符数）
     */
    private static final int CHUNK_SIZE = 2000;

    /**
     * 最小分块大小
     */
    private static final int MIN_CHUNK_SIZE = 100;

    /**
     * 重叠大小（用于保持语义连贯性）
     */
    private static final int CHUNK_OVERLAP = 200;

    /**
     * 最大分块数量（防止大文件生成过多摘要）
     */
    private static final int MAX_CHUNKS = 10;

    /**
     * WebClient实例，用于调用LLM API
     */
    private final WebClient webClient = WebClient.builder()
            .baseUrl(CHAT_URL)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

    /**
     * 调用LLM生成摘要
     * <p>
     * 内部方法，直接调用DashScope Chat API。
     * </p>
     *
     * @param prompt 提示词
     * @return LLM生成的文本
     */
    private String callLLM(String prompt) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", DEFAULT_MODEL);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            body.put("temperature", 0.3);

            Map<String, Object> response = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
            return "";
        } catch (Exception e) {
            log.error("LLM调用失败: {}", e.getMessage());
            throw new RuntimeException("LLM调用失败: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        try {
            String prompt = String.format(SUMMARY_PROMPT, 200, content);
            return callLLM(prompt);
        } catch (Exception e) {
            log.error("生成摘要失败: {}", e.getMessage());
            return content.substring(0, Math.min(200, content.length())) + "...";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Async("taskExecutor")
    public CompletableFuture<String> generateSummaryAsync(Long documentId, BiConsumer<Integer, Integer> onProgress) {
        Document doc = documentMapper.selectById(documentId);
        if (doc == null) {
            return CompletableFuture.failedFuture(new RuntimeException("文档不存在"));
        }

        try {
            updateSummaryStatus(documentId, "processing", 0, MAX_CHUNKS);

            List<String> chunks = chunkDocument(doc.getContent());
            int totalChunks = Math.min(chunks.size(), MAX_CHUNKS);
            updateSummaryStatus(documentId, "processing", 0, totalChunks);

            if (chunks.isEmpty()) {
                String summary = "";
                saveSummary(documentId, summary, 0);
                return CompletableFuture.completedFuture(summary);
            }

            List<CompletableFuture<String>> futures = new ArrayList<>();
            for (int i = 0; i < totalChunks; i++) {
                int chunkIndex = i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return generateSummary(chunks.get(chunkIndex));
                    } catch (Exception e) {
                        log.error("生成分块摘要失败: chunkIndex={}", chunkIndex, e);
                        return "摘要生成失败";
                    }
                }));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            StringBuilder summaryBuilder = new StringBuilder();
            for (int i = 0; i < totalChunks; i++) {
                String chunkSummary = futures.get(i).get();
                summaryBuilder.append(String.format("【第%d部分】%s\n\n", i + 1, chunkSummary));

                int current = i + 1;
                updateSummaryStatus(documentId, "processing", current, totalChunks);

                if (onProgress != null) {
                    onProgress.accept(current, totalChunks);
                }
            }

            String finalSummary = summaryBuilder.toString();
            saveSummary(documentId, finalSummary, totalChunks);
            setSummaryCache(documentId, finalSummary);

            log.info("文档摘要生成完成: docId={}, chunks={}", documentId, totalChunks);
            return CompletableFuture.completedFuture(finalSummary);

        } catch (Exception e) {
            log.error("异步生成摘要失败: docId={}", documentId, e);
            updateSummaryStatus(documentId, "failed", 0, 0);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCachedSummary(Long docId) {
        try {
            String cacheKey = SUMMARY_CACHE_KEY + docId;
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);

            if (cached != null && !cached.isEmpty()) {
                log.debug("摘要缓存命中: docId={}", docId);
                return cached;
            }

            Document doc = documentMapper.selectById(docId);
            if (doc != null && doc.getSummary() != null && !doc.getSummary().isEmpty()) {
                stringRedisTemplate.opsForValue().set(
                    cacheKey, doc.getSummary(), SUMMARY_CACHE_TTL, TimeUnit.SECONDS);
                return doc.getSummary();
            }

            return null;
        } catch (Exception e) {
            log.warn("获取摘要缓存失败: docId={}", docId, e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSummaryCache(Long docId, String summary) {
        try {
            String cacheKey = SUMMARY_CACHE_KEY + docId;
            stringRedisTemplate.opsForValue().set(cacheKey, summary, SUMMARY_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("摘要缓存已设置: docId={}", docId);
        } catch (Exception e) {
            log.warn("设置摘要缓存失败: docId={}", docId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evictSummaryCache(Long docId) {
        try {
            String cacheKey = SUMMARY_CACHE_KEY + docId;
            stringRedisTemplate.delete(cacheKey);
            log.debug("摘要缓存已清除: docId={}", docId);
        } catch (Exception e) {
            log.warn("清除摘要缓存失败: docId={}", docId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getSummaryStatus(Long docId) {
        Map<String, Object> status = new HashMap<>();
        status.put("docId", docId);

        try {
            String statusKey = SUMMARY_STATUS_KEY + docId;
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(statusKey);

            if (!entries.isEmpty()) {
                status.put("status", entries.getOrDefault("status", "unknown"));
                status.put("current", entries.getOrDefault("current", 0));
                status.put("total", entries.getOrDefault("total", 0));
                status.put("updatedTime", entries.getOrDefault("updatedTime", ""));
            } else {
                Document doc = documentMapper.selectById(docId);
                if (doc != null) {
                    status.put("status", doc.getSummaryStatus() != null ? doc.getSummaryStatus() : "pending");
                    status.put("chunks", doc.getSummaryChunks() != null ? doc.getSummaryChunks() : 0);
                    status.put("summary", doc.getSummary() != null ? doc.getSummary() : "");
                    status.put("length", doc.getSummaryLength() != null ? doc.getSummaryLength() : 0);
                } else {
                    status.put("status", "pending");
                    status.put("chunks", 0);
                    status.put("summary", "");
                    status.put("length", 0);
                }
            }
        } catch (Exception e) {
            log.warn("获取摘要状态失败: docId={}", docId, e);
            status.put("status", "pending");
            status.put("chunks", 0);
            status.put("summary", "");
            status.put("length", 0);
        }

        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSummaryStatus(Long docId, String status, int current, int total) {
        try {
            String statusKey = SUMMARY_STATUS_KEY + docId;
            Map<String, String> hash = new HashMap<>();
            hash.put("status", status);
            hash.put("current", String.valueOf(current));
            hash.put("total", String.valueOf(total));
            hash.put("updatedTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            stringRedisTemplate.opsForHash().putAll(statusKey, hash);
            stringRedisTemplate.expire(statusKey, SUMMARY_CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("更新摘要状态失败: docId={}", docId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void saveSummary(Long documentId, String summary, int chunks) {
        documentMapper.updateSummary(documentId, summary, "completed", chunks,
            summary != null ? summary.length() : 0);

        updateSummaryStatus(documentId, "completed", chunks, chunks);
        setSummaryCache(documentId, summary);

        String statusKey = SUMMARY_STATUS_KEY + documentId;
        stringRedisTemplate.delete(statusKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsResummary(Long docId) {
        try {
            Map<String, Object> redisStatus = getSummaryStatus(docId);
            if ("completed".equals(redisStatus.get("status"))) {
                return false;
            }

            Document doc = documentMapper.selectById(docId);
            if (doc == null) return true;
            String status = doc.getSummaryStatus();
            return status == null || "failed".equals(status) || "pending".equals(status);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 文档智能分块
     * <p>
     * 将长文档按语义分割成适当大小的块，
     * 保持段落完整性，同时控制每块大小。
     * </p>
     * <p>
     * 分块策略：
     * <ol>
     *   <li>按段落分割（保持语义完整性）</li>
     *   <li>累积段落直到达到块大小上限</li>
     *   <li>相邻块之间保留Overlap</li>
     * </ol>
     * </p>
     *
     * @param content 文档原始内容
     * @return 分块后的内容列表
     */
    private List<String> chunkDocument(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        List<String> paragraphs = Arrays.asList(content.split("\n\n+"));
        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            if (currentChunk.length() + para.length() > CHUNK_SIZE) {
                if (currentChunk.length() >= MIN_CHUNK_SIZE) {
                    chunks.add(currentChunk.toString().trim());
                    int start = Math.max(0, currentChunk.length() - CHUNK_OVERLAP);
                    currentChunk = new StringBuilder(currentChunk.substring(start));
                }
            }
            currentChunk.append(para.trim()).append("\n\n");
        }

        if (currentChunk.length() >= MIN_CHUNK_SIZE) {
            chunks.add(currentChunk.toString().trim());
        }

        log.info("文档分块完成: 输入长度={}, 输出chunk数={}", content.length(), chunks.size());
        return chunks;
    }
}
