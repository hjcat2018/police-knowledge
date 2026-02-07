package com.police.kb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 批处理配置类
 * <p>
 * 配置文档批处理、向量生成等操作的参数。
 * 支持批量大小、超时时间、重试次数等配置。
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "police.kb.batch")
public class BatchProcessingConfig {

    /**
     * 默认批大小
     */
    private int defaultBatchSize = 10;

    /**
     * 最大批大小
     */
    private int maxBatchSize = 50;

    /**
     * 最小批大小
     */
    private int minBatchSize = 1;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 批处理超时时间（毫秒）
     */
    private long batchTimeoutMs = 60000;

    /**
     * 重试间隔（毫秒）
     */
    private long retryDelayMs = 1000;

    /**
     * 数据库批量操作大小
     */
    private int dbBatchSize = 100;

    /**
     * 数据库操作最大重试次数
     */
    private int dbMaxRetries = 3;

    /**
     * 最大文档大小
     */
    private int maxDocumentSize = 10000;

    /**
     * 分块重叠大小
     */
    private int chunkOverlapSize = 100;

    /**
     * 是否启用并行处理
     */
    private boolean enableParallelProcessing = false;

    /**
     * 并行处理线程数
     */
    private int parallelThreadCount = 4;
}
