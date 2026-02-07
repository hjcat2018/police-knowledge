package com.police.kb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文档分块服务
 * <p>
 * 提供文档内容的智能分块功能。
 * 支持多种分块策略：递归字符切分、简单切分、自定义配置等。
 * </p>
 * <p>
 * 技术特性：
 * <ul>
 *   <li>递归字符切分 - 保持语义完整性</li>
 *   <li>中文优化 - 针对中文标点符号优化</li>
 *   <li>重叠保留 - 相邻块之间保留重叠内容</li>
 *   <li>元数据保留 - 分块过程中保留原始元数据</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
@Service
public class DocumentChunkingService {

    /**
     * 递归切分的分隔符优先级
     * <p>
     * 从高优先级到低优先级排列，
     * 优先使用较大的分隔符以保持语义完整性。
     * </p>
     */
    private static final List<String> SEPARATORS = Arrays.asList(
        "\n\n",
        "\n",
        "。",
        "！",
        "？",
        ";",
        "；",
        ":",
        "：",
        ",",
        "，",
        " ",
        ""
    );

    /**
     * 默认块大小
     */
    private static final int DEFAULT_CHUNK_SIZE = 800;

    /**
     * 默认重叠大小
     */
    private static final int DEFAULT_OVERLAP = 100;

    /**
     * 最小块长度阈值
     */
    private static final int MIN_CHUNK_SIZE = 50;

    /**
     * 使用递归字符切分进行文档分块
     * <p>
     * 推荐的分块方法，优先使用较大的分隔符，
     * 保持段落和句子的完整性。
     * </p>
     *
     * @param documents 原始文档列表
     * @return 分块后的文档列表
     */
    public List<Document> chunkDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文档列表为空");
            return new ArrayList<>();
        }

        log.info("开始递归字符切分，原始文档数: {}", documents.size());
        long startTime = System.currentTimeMillis();

        List<Document> chunkedDocuments = new ArrayList<>();
        for (Document doc : documents) {
            chunkedDocuments.addAll(chunkDocumentRecursive(doc, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP));
        }

        long endTime = System.currentTimeMillis();
        log.info("递归字符切分完成，耗时: {}ms，原始文档数: {}，分块后文档数: {}",
            (endTime - startTime), documents.size(), chunkedDocuments.size());

        return chunkedDocuments;
    }

    /**
     * 使用简单字符切分进行文档分块
     * <p>
     * 兼容旧版本的简单分块方法，
     * 按固定字符数进行切分。
     * </p>
     *
     * @param documents 原始文档列表
     * @return 分块后的文档列表
     */
    public List<Document> chunkDocumentsSimple(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文档列表为空");
            return new ArrayList<>();
        }

        log.info("开始简单字符切分，原始文档数: {}", documents.size());
        long startTime = System.currentTimeMillis();

        List<Document> chunkedDocuments = new ArrayList<>();
        for (Document doc : documents) {
            chunkedDocuments.addAll(chunkSingleDocument(doc));
        }

        long endTime = System.currentTimeMillis();
        log.info("简单字符切分完成，耗时: {}ms，原始文档数: {}，分块后文档数: {}",
            (endTime - startTime), documents.size(), chunkedDocuments.size());

        return chunkedDocuments;
    }

    /**
     * 递归字符切分算法
     * <p>
     * 优先使用较大的分隔符进行切分，
     * 保持语义完整性。
     * </p>
     *
     * @param document 原始文档
     * @param chunkSize 目标块大小
     * @param overlap 重叠字符数
     * @return 切分后的文档块
     */
    private List<Document> chunkDocumentRecursive(Document document, int chunkSize, int overlap) {
        List<Document> chunks = new ArrayList<>();
        String content = document.getText();

        if (content == null || content.isEmpty()) {
            return chunks;
        }

        if (content.length() <= chunkSize) {
            Document chunk = new Document(content, document.getMetadata());
            chunk.getMetadata().put("chunkIndex", 0);
            chunks.add(chunk);
            return chunks;
        }

        List<String> parts = splitBySeparators(content, chunkSize);

        int index = 0;
        for (String part : parts) {
            if (part.length() > chunkSize) {
                Document tempDoc = new Document(part, document.getMetadata());
                List<Document> subChunks = chunkDocumentRecursive(tempDoc, chunkSize, overlap);
                for (Document subChunk : subChunks) {
                    subChunk.getMetadata().put("chunkIndex", index++);
                    chunks.add(subChunk);
                }
            } else if (part.trim().length() >= MIN_CHUNK_SIZE) {
                Document chunk = new Document(part, document.getMetadata());
                chunk.getMetadata().put("chunkIndex", index++);
                chunks.add(chunk);
            }
        }

        chunks = addOverlap(chunks, overlap);

        return chunks;
    }

    /**
     * 使用分隔符优先级切分文本
     *
     * @param text 原始文本
     * @param maxLength 最大块长度
     * @return 切分后的文本块列表
     */
    private List<String> splitBySeparators(String text, int maxLength) {
        for (String separator : SEPARATORS) {
            if (separator.isEmpty()) {
                return splitByLength(text, maxLength);
            }

            List<String> parts = splitBySeparator(text, separator);
            if (parts.size() > 1) {
                boolean allSmallEnough = parts.stream().allMatch(part -> part.length() <= maxLength);
                if (allSmallEnough) {
                    return parts;
                }
            }
        }

        return splitByLength(text, maxLength);
    }

    /**
     * 按指定分隔符切分
     *
     * @param text 原始文本
     * @param separator 分隔符
     * @return 切分后的文本块列表
     */
    private List<String> splitBySeparator(String text, String separator) {
        List<String> parts = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = text.indexOf(separator, start);
            if (end == -1) {
                parts.add(text.substring(start));
                break;
            }

            String part = text.substring(start, end + separator.length());
            parts.add(part);
            start = end + separator.length();
        }

        return parts;
    }

    /**
     * 按长度切分
     * <p>
     * 作为最后的回退方案，
     * 直接按固定长度切分文本。
     * </p>
     *
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 切分后的文本块列表
     */
    private List<String> splitByLength(String text, int maxLength) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < text.length(); i += maxLength) {
            int end = Math.min(i + maxLength, text.length());
            parts.add(text.substring(i, end));
        }
        return parts;
    }

    /**
     * 为切分后的块添加重叠
     * <p>
     * 在相邻块之间保留重叠内容，
     * 以保持检索时的上下文连贯性。
     * </p>
     *
     * @param chunks 切分后的文档块列表
     * @param overlap 重叠字符数
     * @return 添加重叠后的文档块列表
     */
    private List<Document> addOverlap(List<Document> chunks, int overlap) {
        if (chunks.size() <= 1 || overlap <= 0) {
            return chunks;
        }

        List<Document> result = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Document current = chunks.get(i);
            String currentText = current.getText();

            if (i > 0) {
                Document prev = chunks.get(i - 1);
                String prevText = prev != null ? prev.getText() : "";
                int overlapStart = Math.max(0, prevText.length() - overlap);
                String overlapText = prevText.substring(overlapStart);
                currentText = overlapText + currentText;
            }

            Document newChunk = new Document(currentText, current.getMetadata());
            result.add(newChunk);
        }

        return result;
    }

    /**
     * 简单单文档切分
     * <p>
     * 按固定字符数切分单个文档，
     * 保留20%的重叠。
     * </p>
     *
     * @param document 原始文档
     * @return 切分后的文档块列表
     */
    private List<Document> chunkSingleDocument(Document document) {
        List<Document> chunks = new ArrayList<>();
        String content = document.getText();
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        int chunkSize = DEFAULT_CHUNK_SIZE;
        int overlap = DEFAULT_OVERLAP;

        for (int i = 0; i < content.length(); i += (chunkSize - overlap)) {
            int end = Math.min(i + chunkSize, content.length());
            String chunkContent = content.substring(i, end);

            if (chunkContent.trim().length() >= MIN_CHUNK_SIZE) {
                Document chunk = new Document(chunkContent, document.getMetadata());
                chunk.getMetadata().put("chunkIndex", chunks.size());
                chunks.add(chunk);
            }

            if (end == content.length()) {
                break;
            }
        }

        return chunks;
    }

    /**
     * 使用自定义配置进行分块
     *
     * @param documents 原始文档列表
     * @param chunkSize 每个chunk的字符数
     * @param minChunkSizeChars 最小字符数
     * @param overlap 重叠字符数
     * @return 分块后的文档列表
     */
    public List<Document> chunkDocumentsWithConfig(
            List<Document> documents,
            int chunkSize,
            int minChunkSizeChars,
            int overlap) {

        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        log.info("使用自定义配置进行递归切分: chunkSize={}, overlap={}", chunkSize, overlap);

        List<Document> chunkedDocuments = new ArrayList<>();
        for (Document doc : documents) {
            List<Document> chunks = chunkDocumentRecursive(doc, chunkSize, overlap);
            chunks.removeIf(chunk -> chunk.getText().trim().length() < minChunkSizeChars);
            chunkedDocuments.addAll(chunks);
        }

        return chunkedDocuments;
    }

    /**
     * 使用中文优化的递归分块策略
     * <p>
     * 针对中文文档优化的分块参数：
     * <ul>
     *   <li>chunkSize: 600字符</li>
     *   <li>minChunkSize: 200字符</li>
     *   <li>overlap: 120字符</li>
     * </ul>
     * </p>
     *
     * @param documents 原始文档列表
     * @return 分块后的文档列表
     */
    public List<Document> chunkDocumentsChinese(List<Document> documents) {
        log.info("使用中文优化递归分块策略");
        return chunkDocumentsWithConfig(documents, 600, 200, 120);
    }

    /**
     * 使用短文本优化的递归分块策略
     * <p>
     * 针对短文档优化的分块参数：
     * <ul>
     *   <li>chunkSize: 400字符</li>
     *   <li>minChunkSize: 100字符</li>
     *   <li>overlap: 80字符</li>
     * </ul>
     * </p>
     *
     * @param documents 原始文档列表
     * @return 分块后的文档列表
     */
    public List<Document> chunkShortDocuments(List<Document> documents) {
        log.info("使用短文本优化递归分块策略");
        return chunkDocumentsWithConfig(documents, 400, 100, 80);
    }

    /**
     * 保留原始元数据的分块
     * <p>
     * 在分块过程中保留原始文档的元数据，
     * 并添加分块索引信息。
     * </p>
     *
     * @param documents 原始文档列表
     * @return 分块后的文档列表
     */
    public List<Document> chunkDocumentsWithMetadata(List<Document> documents) {
        List<Document> chunked = chunkDocuments(documents);

        for (int i = 0; i < chunked.size(); i++) {
            Document doc = chunked.get(i);
            doc.getMetadata().put("chunkIndex", i);
            doc.getMetadata().put("totalChunks", chunked.size());
            doc.getMetadata().put("originalId", doc.getId());
        }

        return chunked;
    }
}
