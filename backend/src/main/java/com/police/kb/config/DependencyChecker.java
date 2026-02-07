package com.police.kb.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DependencyChecker implements ApplicationRunner {

  @Autowired(required = false)
  private EmbeddingModel embeddingModel;

  @Autowired(required = false)
  private VectorStore vectorStore;

  @Autowired
  private SeekDBConfig seekDBConfig;

  @Override
  public void run(ApplicationArguments args) {
    log.info("=".repeat(60));
    log.info("启动依赖检查...");
    log.info("=".repeat(60));

    if (embeddingModel != null) {
      log.info("✅ EmbeddingModel: 已配置");
    } else {
      log.warn("⚠️  EmbeddingModel: 未配置，向量生成功能不可用");
      log.warn("   提示: 请确保 spring.ai.dashscope.api-key 已正确配置");
    }

    if (vectorStore != null) {
      log.info("✅ VectorStore: 已配置");
    } else {
      log.warn("⚠️  VectorStore: 未配置，将使用传统数据库检索");
      log.warn("   提示: 向量检索性能可能受限，建议配置 SeekDB 向量存储");
    }

    if (seekDBConfig != null) {
      log.info("✅ SeekDB配置: 已加载");
      log.info("   - Host: {}:{}", seekDBConfig.getHost(), seekDBConfig.getPort());
      log.info("   - Database: {}", seekDBConfig.getDatabase());
      log.info("   - Username: {}", seekDBConfig.getUsername());
      log.info("   - 混合搜索: {}", seekDBConfig.isHybridSearchEnabled() ? "启用" : "禁用");
      if (seekDBConfig.getRag() != null) {
        log.info("   - TopK: {}", seekDBConfig.getRag().getTopK());
        log.info("   - 相似度阈值: {}", seekDBConfig.getRag().getSimilarityThreshold());
      }
      log.info("✅ 向量索引配置: HNSW(M={}, efConstruction={}, efSearch={})",
          seekDBConfig.getM(), seekDBConfig.getEfConstruction(), seekDBConfig.getEfSearch());
    } else {
      log.warn("⚠️  SeekDB配置: 未加载");
    }

    log.info("=".repeat(60));
    log.info("依赖检查完成！");
    log.info("=".repeat(60));
  }
}
