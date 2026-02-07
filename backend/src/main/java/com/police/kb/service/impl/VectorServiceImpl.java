package com.police.kb.service.impl;

import com.police.kb.common.ChineseSegmenter;
import com.police.kb.config.BatchProcessingConfig;
import com.police.kb.config.SeekDBConfig;
import com.police.kb.domain.entity.Document;
import com.police.kb.domain.entity.SysDict;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.mapper.SysDictMapper;
import com.police.kb.service.VectorService;
import com.police.kb.domain.vo.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorServiceImpl implements VectorService {

  private static final String VECTOR_TABLE = "document_vectors";

  @Value("${spring.ai.dashscope.api-key:sk-e8d86d11e21540d9b72bc83f78bf1154}")
  private String apiKey;

  private final JdbcTemplate jdbcTemplate;
  private final DocumentMapper documentMapper;
  private final SysDictMapper sysDictMapper;
  private final ObjectMapper objectMapper;

  @Autowired(required = false)
  private EmbeddingModel embeddingModel;

  @Autowired(required = false)
  private VectorStore vectorStore;

  @Autowired
  private BatchProcessingConfig batchConfig;

  @Autowired
  private SeekDBConfig seekDBConfig;

  @Qualifier("searchExecutor")
  @Autowired
  private Executor searchExecutor;

  @Override
  @Transactional
  public void vectorizeDocument(Long documentId) {
    Document doc = documentMapper.selectById(documentId);
    if (doc == null) {
      log.warn("文档不存在: {}", documentId);
      return;
    }

    String docTitle = doc.getTitle();

    try {
      log.info("开始向量化文档: id={}, title={}", documentId, docTitle);
      log.info("文档内容长度: {}", doc.getContent() != null ? doc.getContent().length() : 0);

      if (!hasContent(doc)) {
        log.warn("文档没有内容，使用标题作为向量化内容");
      }

      deleteDocumentVectors(documentId);
      log.info("已删除旧向量");

      List<String> paragraphs = splitByParagraph(doc.getContent());
      log.info("段落数量: {}", paragraphs.size());

      if (paragraphs.isEmpty()) {
        String fallbackContent = doc.getSummary();
        if (fallbackContent == null || fallbackContent.isEmpty()) {
          fallbackContent = doc.getTitle();
        }
        log.info("使用备用内容: length={}, content={}", fallbackContent != null ? fallbackContent.length() : 0,
            fallbackContent);
        if (fallbackContent != null && fallbackContent.length() >= 10) {
          paragraphs = List.of(fallbackContent);
        } else {
          paragraphs = new ArrayList<>();
          log.warn("备用内容太短，无法向量化: id={}, title={}", documentId, docTitle);
        }
      }

      // 批量生成向量以提高性能
      log.info("开始批量生成向量，共{}个段落", paragraphs.size());
      List<String> vectors = generateVectorsWithConfig(paragraphs, batchConfig.getDefaultBatchSize());
      log.info("批量向量生成完成，成功生成{}个向量", vectors.stream().filter(Objects::nonNull).count());

      // 批量插入向量到数据库
      batchInsertVectors(documentId, paragraphs, vectors);

      log.info("文档向量化完成: {}, 段落数: {}", documentId, paragraphs.size());
    } catch (Exception e) {
      log.error("文档向量化失败: id={}, title={}, error={}", documentId, docTitle, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional
  public void revectorizeDocument(Long documentId) {
    vectorizeDocument(documentId);
  }

  @Override
  @Transactional
  public void deleteDocumentVectors(Long documentId) {
    jdbcTemplate.update("DELETE FROM " + VECTOR_TABLE + " WHERE document_id = ?", documentId);
  }

    @Override
    public List<SearchResult> semanticSearch(String keyword, Long kbId, Long categoryId, String originScope, String originDepartment, int topK) {
        log.info("执行语义搜索: keyword={}, kbId={}, categoryId={}, originScope={}, originDepartment={}, topK={}", 
            keyword, kbId, categoryId, originScope, originDepartment, topK);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.info("关键词无效，返回空结果");
            return new ArrayList<>();
        }

        String cleanKeyword = keyword.trim();
        log.info("清理后的关键词: {}", cleanKeyword);

        try {
            String questionVector = generateVector(cleanKeyword);
            if (questionVector == null || questionVector.isEmpty()) {
                log.warn("向量生成失败，回退到全文搜索");
                return fullTextSearch(cleanKeyword, kbId, categoryId, originScope, originDepartment, topK);
            }

            return seekDBHybridSearch(cleanKeyword, questionVector, kbId, categoryId, originScope, originDepartment, topK);
        } catch (Exception e) {
            log.error("语义搜索失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return fullTextSearch(cleanKeyword, kbId, categoryId, originScope, originDepartment, topK);
        }
    }

    @Override
    public List<SearchResult> hybridSearch(String keyword, Long kbId, Long categoryId, String originScope, String originDepartment, int topK) {
        try {
            if (!seekDBConfig.isHybridSearchEnabled()) {
                log.info("混合搜索已禁用，使用语义搜索");
                return semanticSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
            }

            String questionVector = generateVector(keyword);
            if (questionVector == null || questionVector.isEmpty()) {
                log.warn("向量生成失败，回退到语义搜索");
                return semanticSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
            }

            log.info("使用SeekDB混合搜索");
            return seekDBHybridSearch(keyword, questionVector, kbId, categoryId, originScope, originDepartment, topK);
        } catch (Exception e) {
            log.error("混合搜索失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return semanticSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
        }
    }

    @Override
    public List<SearchResult> searchWithFilters(String keyword, Long kbId, Long categoryId, Map<String, Object> filters, int topK) {
        String originScope = filters != null ? (String) filters.get("originScope") : null;
        String originDepartment = filters != null ? (String) filters.get("originDepartment") : null;
        return hybridSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
    }

    @Override
    public void syncVectorMetadata(Long documentId, String originScope, String originDepartment) {
        log.info("同步文档溯源元数据: documentId={}, originScope={}, originDepartment={}", 
            documentId, originScope, originDepartment);
        
        try {
            jdbcTemplate.update(
                "UPDATE " + VECTOR_TABLE + " SET origin_scope = ?, origin_department = ?, updated_time = NOW() WHERE document_id = ?",
                originScope, originDepartment, documentId
            );
            log.info("向量表溯源元数据同步完成: documentId={}", documentId);
        } catch (Exception e) {
            log.error("同步向量表溯源元数据失败: documentId={}, error={}", documentId, e.getMessage(), e);
        }
    }

    /**
      * SeekDB混合搜索：结合向量相似度和全文搜索
      * 使用加权融合策略，提升检索精准率
      * 采用并行检索，向量搜索和全文搜索同时执行
      */
     private List<SearchResult> seekDBHybridSearch(String keyword, String questionVector, Long kbId, 
         Long categoryId, String originScope, String originDepartment, int topK) {
       List<SearchResult> results = new ArrayList<>();

       try {
         log.info("执行SeekDB混合搜索: keyword={}, kbId={}, categoryId={}, originScope={}, originDepartment={}, topK={}", 
             keyword, kbId, categoryId, originScope, originDepartment, topK);

         float vectorWeight = seekDBConfig.getVectorWeight();
         float fulltextWeight = seekDBConfig.getFulltextWeight();

         log.info("混合搜索权重: 向量={}, 全文={}", vectorWeight, fulltextWeight);

         // 并行执行向量搜索和全文搜索
         CompletableFuture<List<SearchResult>> vectorFuture = CompletableFuture.supplyAsync(() -> {
           log.debug("开始执行向量搜索...");
           List<SearchResult> vectorResults = vectorSimilaritySearch(keyword, questionVector, kbId, 
               categoryId, originScope, originDepartment, topK);
           log.debug("向量搜索完成，结果数: {}", vectorResults.size());
           return vectorResults;
         }, searchExecutor);

         CompletableFuture<List<SearchResult>> textFuture = CompletableFuture.supplyAsync(() -> {
           log.debug("开始执行全文搜索...");
           List<SearchResult> textResults = fullTextSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
           log.debug("全文搜索完成，结果数: {}", textResults.size());
           return textResults;
         }, searchExecutor);

         // 等待两个搜索任务完成
         CompletableFuture<Void> allFutures = CompletableFuture.allOf(vectorFuture, textFuture);
         
         try {
           allFutures.get();
         } catch (Exception e) {
           log.warn("并行检索超时或异常: {}", e.getMessage());
         }

         List<SearchResult> vectorResults = vectorFuture.join();
         List<SearchResult> textResults = textFuture.join();

         log.info("并行搜索完成: 向量={}, 全文={}", vectorResults.size(), textResults.size());

         results = weightedFusion(vectorResults, textResults, vectorWeight, fulltextWeight);

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        double hybridScoreThreshold = seekDBConfig.getHybridScoreThreshold();
        int beforeFilterSize = results.size();
        results = results.stream()
            .filter(result -> result.getScore() >= hybridScoreThreshold)
            .collect(Collectors.toList());
        int afterFilterSize = results.size();

        log.info("综合分阈值过滤: 阈值={}, 过滤前={}, 过滤后={}",
            hybridScoreThreshold, beforeFilterSize, afterFilterSize);

        if (results.size() > topK) {
          results = results.subList(0, topK);
        }

        log.info("混合搜索最终结果数量: {}", results.size());

      } catch (Exception e) {
        log.error("混合搜索执行失败: keyword={}, error={}", keyword, e.getMessage(), e);
        return vectorSimilaritySearch(keyword, questionVector, kbId, categoryId, originScope, originDepartment, topK);
      }

      return results;
    }

    /**
     * 加权融合向量搜索和全文搜索结果
     * 使用基于排名的 RRF (Reciprocal Rank Fusion) 算法
     */
    private List<SearchResult> weightedFusion(List<SearchResult> vectorResults,
        List<SearchResult> textResults, float vectorWeight, float fulltextWeight) {
      Map<Long, SearchResult> resultMap = new HashMap<>();
      Map<Long, Double> vectorScores = new HashMap<>();
      Map<Long, Double> textScores = new HashMap<>();

      for (SearchResult result : vectorResults) {
        Long docId = result.getDocumentId();
        resultMap.put(docId, result);
        vectorScores.put(docId, result.getScore());
      }

      for (SearchResult result : textResults) {
        Long docId = result.getDocumentId();
        if (!resultMap.containsKey(docId)) {
          resultMap.put(docId, result);
        }
        textScores.put(docId, result.getScore());
      }

      List<SearchResult> fusedResults = new ArrayList<>();
      double maxVectorScore = 0.0;
      double maxTextScore = 0.0;

      for (double score : vectorScores.values()) {
        maxVectorScore = Math.max(maxVectorScore, score);
      }
      for (double score : textScores.values()) {
        maxTextScore = Math.max(maxTextScore, score);
      }

      for (Map.Entry<Long, SearchResult> entry : resultMap.entrySet()) {
        Long docId = entry.getKey();
        SearchResult result = entry.getValue();

        double normalizedVectorScore = maxVectorScore > 0
            ? vectorScores.getOrDefault(docId, 0.0) / maxVectorScore
            : 0;
        double normalizedTextScore = maxTextScore > 0
            ? textScores.getOrDefault(docId, 0.0) / maxTextScore
            : 0;

        double fusedScore;
        boolean hasVector = vectorScores.containsKey(docId);
        boolean hasText = textScores.containsKey(docId);

        if (hasVector && hasText) {
          fusedScore = normalizedVectorScore * vectorWeight + normalizedTextScore * fulltextWeight;
        } else if (hasVector) {
          fusedScore = normalizedVectorScore * (vectorWeight + fulltextWeight);
        } else {
          fusedScore = normalizedTextScore * (vectorWeight + fulltextWeight);
        }

        result.setScore(fusedScore);
        fusedResults.add(result);
      }

      return fusedResults;
    }

    /**
     * 向量相似度搜索
     * 使用SeekDB的l2_distance函数进行真正的向量检索
     */
    private List<SearchResult> vectorSimilaritySearch(String keyword, String questionVector, Long kbId, 
        Long categoryId, String originScope, String originDepartment, int topK) {
      List<SearchResult> results = new ArrayList<>();

      try {
        log.info("执行SeekDB向量相似度搜索: keyword={}, kbId={}, categoryId={}, originScope={}, originDepartment={}, topK={}", 
            keyword, kbId, categoryId, originScope, originDepartment, topK);

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT d.id AS document_id, d.title, d.summary, ");
        sqlBuilder.append("dv.content, ");
        sqlBuilder.append("l2_distance(dv.vector, ?) AS vector_distance ");
        sqlBuilder.append("FROM document d ");
        sqlBuilder.append("INNER JOIN ").append(VECTOR_TABLE).append(" dv ON d.id = dv.document_id AND dv.deleted = 0 ");
        sqlBuilder.append("WHERE d.deleted = 0 ");
        sqlBuilder.append("AND dv.vector IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        params.add(questionVector);

        // kbId 过滤（使用kb_path支持级联查询）
        if (kbId != null && kbId > 0) {
          sqlBuilder.append("AND FIND_IN_SET(?, d.kb_path) ");
          params.add(kbId);
        }

        // categoryId 分类过滤
        if (categoryId != null && categoryId > 0) {
          sqlBuilder.append("AND d.kb_id IN (SELECT id FROM sys_dict WHERE kind = 'kb_category' AND (id = ? OR parent_code = ?) AND deleted = 0) ");
          params.add(categoryId);
          params.add(getCategoryCodeById(categoryId));
        }

        if (originScope != null && !originScope.isEmpty()) {
          sqlBuilder.append("AND (d.origin_scope = ? OR dv.origin_scope = ?) ");
          params.add(originScope);
          params.add(originScope);
        }

        if (originDepartment != null && !originDepartment.isEmpty()) {
          sqlBuilder.append("AND (d.origin_department = ? OR dv.origin_department = ?) ");
          params.add(originDepartment);
          params.add(originDepartment);
        }

        sqlBuilder.append("AND l2_distance(dv.vector, ?) IS NOT NULL ");
        sqlBuilder.append("ORDER BY vector_distance APPROXIMATE ");
        sqlBuilder.append("LIMIT ").append(topK);

        params.add(questionVector);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());

        log.info("向量搜索结果数量: {}", rows.size());

        Set<Long> seenIds = new HashSet<>();
        double maxDistance = 0.0;
        double minDistance = Double.MAX_VALUE;

        for (Map<String, Object> row : rows) {
          Object distanceObj = row.get("vector_distance");
          if (distanceObj != null) {
            double distance = ((Number) distanceObj).doubleValue();
            maxDistance = Math.max(maxDistance, distance);
            minDistance = Math.min(minDistance, distance);
          }
        }

        for (Map<String, Object> row : rows) {
          Long documentId = ((Number) row.get("document_id")).longValue();

          if (seenIds.contains(documentId)) {
            continue;
          }
          seenIds.add(documentId);

          String content = (String) row.get("content");

          double vectorDistance = 0.0;
          if (row.get("vector_distance") != null) {
            vectorDistance = ((Number) row.get("vector_distance")).doubleValue();
          }

          double vectorScore = 0.5;
          if (maxDistance > minDistance && maxDistance > 0) {
            vectorScore = 1.0 - (vectorDistance - minDistance) / (maxDistance - minDistance);
            vectorScore = 0.5 + vectorScore * 0.5;
            vectorScore = Math.max(0.5, Math.min(1.0, vectorScore));
          } else if (rows.size() == 1) {
            vectorScore = 0.9;
          }

          Document doc = documentMapper.selectById(documentId);
          if (doc != null && doc.getDeleted() == 0) {
            SearchResult result = new SearchResult();
            result.setDocumentId(documentId);
            result.setTitle(doc.getTitle());
            result.setSummary(doc.getSummary());
            result.setContent(content != null ? content : "");
            result.setScore(vectorScore);
            results.add(result);

            log.debug("向量搜索结果: id={}, title={}, distance={}, score={}",
                documentId, doc.getTitle(), vectorDistance, vectorScore);
          }
        }

        log.info("向量搜索完成: 有效结果={}/{}", results.size(), rows.size());

        if (results.isEmpty()) {
          log.warn("向量搜索无结果，回退到全文搜索");
          return fullTextSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
        }

        return results;

      } catch (Exception e) {
        log.error("向量搜索失败: keyword={}, error={}", keyword, e.getMessage(), e);
        log.warn("向量搜索异常，回退到全文搜索");
        return fullTextSearch(keyword, kbId, categoryId, originScope, originDepartment, topK);
      }
    }

    /**
     * 全文搜索
     * 使用中文分词器进行关键词分词
     */
    private List<SearchResult> fullTextSearch(String keyword, Long kbId, Long categoryId, String originScope, String originDepartment, int topK) {
      List<SearchResult> results = new ArrayList<>();

      try {
        List<String> tokens = ChineseSegmenter.segment(keyword);

        if (tokens.isEmpty()) {
          tokens = List.of(keyword);
        }

        log.info("搜索关键词: {}, 分词数量: {}, 分词结果: {}", keyword, tokens.size(), tokens);

        StringBuilder likeConditions = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (kbId != null && kbId > 0) {
          params.add(kbId);
        }

        for (int i = 0; i < tokens.size(); i++) {
          if (i > 0) {
            likeConditions.append(" OR ");
          }
          likeConditions.append("(dv.content LIKE ? OR d.title LIKE ? OR d.summary LIKE ?)");
          String pattern = "%" + escapeLike(tokens.get(i)) + "%";
          params.add(pattern);
          params.add(pattern);
          params.add(pattern);
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT d.id AS document_id, d.title, d.summary, ");
        sqlBuilder.append("dv.content ");
        sqlBuilder.append("FROM document d ");
        sqlBuilder.append("INNER JOIN ").append(VECTOR_TABLE).append(" dv ON d.id = dv.document_id AND dv.deleted = 0 ");
        sqlBuilder.append("WHERE d.deleted = 0 ");

        // kbId 过滤（使用kb_path支持级联查询）
        if (kbId != null && kbId > 0) {
          sqlBuilder.append("AND FIND_IN_SET(?, d.kb_path) ");
        }

        // categoryId 分类过滤
        if (categoryId != null && categoryId > 0) {
          sqlBuilder.append("AND d.kb_id IN (SELECT id FROM sys_dict WHERE kind = 'kb_category' AND (id = ? OR parent_code = ?) AND deleted = 0) ");
          params.add(categoryId);
          params.add(getCategoryCodeById(categoryId));
        }

        if (originScope != null && !originScope.isEmpty()) {
          sqlBuilder.append("AND (d.origin_scope = ? OR dv.origin_scope = ?) ");
          params.add(originScope);
          params.add(originScope);
        }

        if (originDepartment != null && !originDepartment.isEmpty()) {
          sqlBuilder.append("AND (d.origin_department = ? OR dv.origin_department = ?) ");
          params.add(originDepartment);
          params.add(originDepartment);
        }

        if (originScope != null && !originScope.isEmpty()) {
          sqlBuilder.append("AND (d.origin_scope = ? OR dv.origin_scope = ?) ");
        }

        if (originDepartment != null && !originDepartment.isEmpty()) {
          sqlBuilder.append("AND (d.origin_department = ? OR dv.origin_department = ?) ");
        }

        sqlBuilder.append("AND dv.content IS NOT NULL AND LENGTH(dv.content) > 10 ");
        if (likeConditions.length() > 0) {
          sqlBuilder.append("AND (").append(likeConditions.toString()).append(") ");
        }

        sqlBuilder.append("LIMIT ").append(topK);

        log.info("全文搜索SQL: {}", sqlBuilder.toString());

        if (originScope != null && !originScope.isEmpty()) {
          params.add(originScope);
          params.add(originScope);
        }

        if (originDepartment != null && !originDepartment.isEmpty()) {
          params.add(originDepartment);
          params.add(originDepartment);
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());

        log.info("全文搜索结果数量: {}", rows.size());

        Set<Long> seenIds = new HashSet<>();
        for (Map<String, Object> row : rows) {
          Long documentId = ((Number) row.get("document_id")).longValue();

          if (seenIds.contains(documentId)) {
            continue;
          }
          seenIds.add(documentId);

          String content = (String) row.get("content");

          Document doc = documentMapper.selectById(documentId);
          if (doc != null && doc.getDeleted() == 0) {
            SearchResult result = new SearchResult();
            result.setDocumentId(documentId);
            result.setTitle(doc.getTitle());
            result.setSummary(doc.getSummary());
            result.setContent(content != null ? content : "");
            result.setScore(0.7);
            results.add(result);

            log.debug("全文搜索结果: id={}, title={}", documentId, doc.getTitle());
          }
        }

        log.info("解析全文搜索结果完成: 总数={}", results.size());
      } catch (Exception e) {
        log.error("全文搜索失败: keyword={}, error={}", keyword, e.getMessage(), e);
      }

      return results;
    }

  /**
   * 合并向量搜索和全文搜索结果，并去重
   */
  private List<SearchResult> mergeAndDedupResults(List<SearchResult> vectorResults, List<SearchResult> textResults) {
    List<SearchResult> merged = new ArrayList<>();
    Map<Long, SearchResult> resultMap = new HashMap<>();

    // 合并向量搜索结果
    for (SearchResult result : vectorResults) {
      resultMap.put(result.getDocumentId(), result);
    }

    // 合并全文搜索结果（如果文档已存在，取较高分数）
    for (SearchResult result : textResults) {
      Long docId = result.getDocumentId();
      if (resultMap.containsKey(docId)) {
        // 文档已存在，取较高分数
        SearchResult existing = resultMap.get(docId);
        if (result.getScore() > existing.getScore()) {
          resultMap.put(docId, result);
        }
      } else {
        resultMap.put(docId, result);
      }
    }

    // 转换为列表并按分数排序
    merged = new ArrayList<>(resultMap.values());
    merged.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

    log.info("合并结果完成: 向量结果={}, 全文结果={}, 合并去重后={}",
        vectorResults.size(), textResults.size(), merged.size());

    return merged;
  }

  /**
   * 使用MATCH...AGAINST进行全文搜索（需要FULLTEXT索引）
   * 如果全文搜索失败，回退到基础的LIKE搜索，不再使用l2_distance向量相似度搜索
   */
  private List<SearchResult> hybridSearchWithMatch(String keyword, Long kbId, int topK) {
    try {
      // 尝试使用MATCH...AGAINST全文搜索
      StringBuilder sqlBuilder = new StringBuilder();
      sqlBuilder.append("SELECT d.id AS document_id, d.title, d.summary, ");
      sqlBuilder.append("dv.content ");
      sqlBuilder.append("FROM document d ");
      sqlBuilder.append("LEFT JOIN ").append(VECTOR_TABLE).append(" dv ON d.id = dv.document_id AND dv.deleted = 0 ");
      sqlBuilder.append("WHERE d.deleted = 0 ");
      sqlBuilder.append("AND FIND_IN_SET(?, d.kb_path) ");
      sqlBuilder.append("AND dv.content IS NOT NULL AND LENGTH(dv.content) > 10 ");
      sqlBuilder.append("AND MATCH(dv.content) AGAINST(? IN NATURAL LANGUAGE MODE) ");
      sqlBuilder.append("ORDER BY d.created_time DESC ");
      sqlBuilder.append("LIMIT ").append(topK);

      log.info("全文搜索SQL: {}", sqlBuilder.toString());

      List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlBuilder.toString(), kbId, keyword);
      log.info("全文搜索结果数量: {}", rows.size());
      return parseSearchResults(rows);
    } catch (Exception e) {
      log.warn("全文搜索失败，直接返回空结果: {}", e.getMessage());
      // 不再回退到l2_distance向量相似度搜索，避免数据类型错误
      return new ArrayList<>();
    }
  }

  /**
   * 使用l2_distance进行向量相似度搜索（SeekDB官方推荐的混合搜索方式）
   */
  private List<SearchResult> hybridSearchWithVector(String keyword, Long kbId, int topK) {
    try {
      String questionVector = generateVector(keyword);
      if (questionVector == null || questionVector.isEmpty()) {
        log.warn("向量生成失败，无法进行向量相似度搜索");
        return new ArrayList<>();
      }

      StringBuilder sqlBuilder = new StringBuilder();
      sqlBuilder.append("SELECT d.id AS document_id, d.title, d.summary, ");
      sqlBuilder.append("l2_distance(dv.vector, ?) AS vector_distance, ");
      sqlBuilder.append("dv.content ");
      sqlBuilder.append("FROM document d ");
      sqlBuilder.append("LEFT JOIN ").append(VECTOR_TABLE).append(" dv ON d.id = dv.document_id AND dv.deleted = 0 ");
      sqlBuilder.append("WHERE d.deleted = 0 ");
      sqlBuilder.append("AND FIND_IN_SET(?, d.kb_path) ");
      sqlBuilder.append("AND dv.vector IS NOT NULL AND LENGTH(dv.vector) > 10 ");
      sqlBuilder.append("ORDER BY vector_distance APPROXIMATE ");
      sqlBuilder.append("LIMIT ").append(topK);

      log.info("向量相似度搜索SQL: {}", sqlBuilder.toString());

      List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlBuilder.toString(), questionVector, kbId);
      log.info("向量相似度搜索结果数量: {}", rows.size());

      // 解析结果并计算分数
      return parseVectorSearchResults(rows);
    } catch (Exception e) {
      log.error("向量相似度搜索失败: keyword={}, error={}", keyword, e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * 解析向量相似度搜索结果
   */
  private List<SearchResult> parseVectorSearchResults(List<Map<String, Object>> rows) {
    List<SearchResult> results = new ArrayList<>();
    Set<Long> seenIds = new HashSet<>();

    try {
      for (Map<String, Object> row : rows) {
        Long documentId = ((Number) row.get("document_id")).longValue();

        // 跳过重复的documentId
        if (seenIds.contains(documentId)) {
          continue;
        }
        seenIds.add(documentId);

        String content = (String) row.get("content");

        // 获取向量距离
        double vectorDistance = 0.0;
        if (row.get("vector_distance") != null) {
          vectorDistance = ((Number) row.get("vector_distance")).doubleValue();
        }

        // 将向量距离转换为相似度分数（距离越小，分数越高）
        double vectorScore = vectorDistance > 0 ? Math.max(0, 1.0 - vectorDistance / 50.0) : 1.0;

        Document doc = documentMapper.selectById(documentId);
        if (doc != null && doc.getDeleted() == 0) {
          SearchResult result = new SearchResult();
          result.setDocumentId(documentId);
          result.setTitle(doc.getTitle());
          result.setSummary(doc.getSummary());
          result.setContent(content != null ? content : "");
          result.setScore(vectorScore);
          results.add(result);
          log.debug("向量搜索结果: id={}, title={}, vectorDistance={}, score={}",
              documentId, doc.getTitle(), vectorDistance, vectorScore);
        }
      }
      log.info("解析向量搜索结果完成: 总数={}", results.size());
    } catch (Exception e) {
      log.error("解析向量搜索结果失败: {}", e.getMessage(), e);
    }
    return results;
  }

  /**
   * 解析混合搜索结果
   */
  @SuppressWarnings("unchecked")
  private List<SearchResult> parseHybridSearchResults(String resultJson) {
    List<SearchResult> results = new ArrayList<>();

    try {
      ObjectMapper mapper = new ObjectMapper();
      List<Map<String, Object>> searchResults = mapper.readValue(resultJson, List.class);

      for (Map<String, Object> item : searchResults) {
        // 尝试获取document_id
        Long docId = null;
        if (item.containsKey("document_id")) {
          docId = ((Number) item.get("document_id")).longValue();
        } else if (item.containsKey("id")) {
          docId = ((Number) item.get("id")).longValue();
        } else if (item.containsKey("fields")) {
          // SeekDB可能将document_id放在fields中
          Map<String, Object> fields = (Map<String, Object>) item.get("fields");
          if (fields != null && fields.containsKey("document_id")) {
            docId = ((Number) fields.get("document_id")).longValue();
          }
        }

        if (docId == null) {
          log.warn("无法从搜索结果中获取document_id: {}", item);
          continue;
        }

        // 获取content
        String content = null;
        double keywordScore = 0.5;
        double semanticScore = 0.5;

        if (item.containsKey("content")) {
          content = (String) item.get("content");
        } else if (item.containsKey("fields")) {
          Map<String, Object> fields = (Map<String, Object>) item.get("fields");
          if (fields != null) {
            content = (String) fields.get("content");
            if (fields.containsKey("_keyword_score") && fields.get("_keyword_score") instanceof Number) {
              keywordScore = ((Number) fields.get("_keyword_score")).doubleValue();
            }
            if (fields.containsKey("_semantic_score") && fields.get("_semantic_score") instanceof Number) {
              semanticScore = ((Number) fields.get("_semantic_score")).doubleValue();
            }
          }
        }

        // 计算综合分数
        double combinedScore = (keywordScore + semanticScore) / 2.0;
        if (item.containsKey("_score") && item.get("_score") instanceof Number) {
          combinedScore = ((Number) item.get("_score")).doubleValue();
        }

        Document doc = documentMapper.selectById(docId);
        if (doc != null && doc.getDeleted() == 0) {
          SearchResult result = new SearchResult();
          result.setDocumentId(docId);
          result.setTitle(doc.getTitle());
          result.setSummary(doc.getSummary());
          result.setContent(content != null ? content : "");
          result.setScore(combinedScore);
          results.add(result);
          log.debug("搜索结果: id={}, title={}, keywordScore={}, semanticScore={}, combinedScore={}",
              docId, doc.getTitle(), keywordScore, semanticScore, combinedScore);
        }
      }

      log.info("解析混合搜索结果完成: 总数={}", results.size());
    } catch (Exception e) {
      log.error("解析混合搜索结果失败: {}, resultJson={}", e.getMessage(),
          resultJson != null && resultJson.length() > 500 ? resultJson.substring(0, 500) : resultJson, e);
    }

    return results;
  }

  @Override
  public String generateVector(String text) {
    try {
      if (embeddingModel != null) {
        log.info("使用Spring AI EmbeddingModel生成向量");
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        if (response.getResults().isEmpty()) {
          log.error("嵌入响应为空");
          return generateMockVectorString(text);
        }

        float[] embedding = response.getResults().get(0).getOutput();
        if (embedding == null || embedding.length == 0) {
          log.error("嵌入向量为空");
          return generateMockVectorString(text);
        }

        log.info("向量生成成功，维度: {}", embedding.length);

        StringBuilder vectorStr = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
          if (i > 0)
            vectorStr.append(",");
          vectorStr.append(embedding[i]);
        }
        vectorStr.append("]");
        return vectorStr.toString();
      }

      log.warn("EmbeddingModel未配置，使用离线模式生成Mock向量");
      return generateMockVectorString(text);
    } catch (Exception e) {
      log.error("生成向量失败: {}", e.getMessage(), e);
      return generateMockVectorString(text);
    }
  }

  /**
   * 生成单个Mock向量字符串（离线测试模式）
   */
  private String generateMockVectorString(String text) {
    log.info("生成Mock向量字符串，文本: {}", text.substring(0, Math.min(20, text.length())));

    StringBuilder vectorStr = new StringBuilder("[");
    int hash = text.hashCode();
    for (int i = 0; i < 1024; i++) {
      if (i > 0)
        vectorStr.append(",");
      float value = (float) (Math.sin(hash + i) * 0.1);
      vectorStr.append(value);
    }
    vectorStr.append("]");

    return vectorStr.toString();
  }

    /**
     * 转义SQL LIKE特殊字符
     */
    private String escapeLike(String str) {
        return str.replace("\\", "\\\\")
                 .replace("%", "\\%")
                 .replace("_", "\\_");
    }

    /**
      * 根据分类ID获取分类编码
      */
     private String getCategoryCodeById(Long categoryId) {
         try {
             com.police.kb.domain.entity.SysDict dict = sysDictMapper.selectById(categoryId);
             return dict != null ? dict.getCode() : null;
         } catch (Exception e) {
             log.warn("获取分类编码失败: categoryId={}", categoryId);
             return null;
         }
     }

     /**
      * 批量生成向量（支持自定义批大小）
      */
     public List<String> generateVectorsWithConfig(List<String> texts, int batchSize) {
    if (texts == null || texts.isEmpty()) {
      return new ArrayList<>();
    }

    // 验证和调整批大小
    batchSize = Math.max(batchConfig.getMinBatchSize(),
        Math.min(batchConfig.getMaxBatchSize(), batchSize));

    log.info("开始批量生成向量，文本数量: {}, 批大小: {}", texts.size(), batchSize);

    List<String> allVectors = new ArrayList<>();
    long startTime = System.currentTimeMillis();

    try {
      // 分批处理
      for (int i = 0; i < texts.size(); i += batchSize) {
        int endIndex = Math.min(i + batchSize, texts.size());
        List<String> batch = texts.subList(i, endIndex);

        log.debug("处理批次 {}-{}, 批大小: {}", i, endIndex - 1, batch.size());

        List<String> batchVectors = processBatchWithRetry(batch);
        allVectors.addAll(batchVectors);

        // 添加小延迟避免API限制
        if (i + batchSize < texts.size()) {
          Thread.sleep(100);
        }
      }

      long endTime = System.currentTimeMillis();
      int successCount = (int) allVectors.stream().filter(Objects::nonNull).count();

      log.info("批量向量生成完成，总耗时: {}ms，成功: {}/{}",
          (endTime - startTime), successCount, texts.size());

      return allVectors;

    } catch (Exception e) {
      log.error("批量生成向量过程中发生严重错误: {}", e.getMessage(), e);
      // 备用方案：逐个生成剩余的向量
      return fallbackBatchGeneration(texts, allVectors.size());
    }
  }

  /**
   * 处理单个批次（带重试机制）
   */
  private List<String> processBatchWithRetry(List<String> batch) {
    int retryCount = 0;
    Exception lastException = null;

    while (retryCount < batchConfig.getMaxRetries()) {
      try {
        return processBatch(batch);
      } catch (Exception e) {
        retryCount++;
        lastException = e;
        log.warn("批处理失败，重试 {}/{}: {}", retryCount, batchConfig.getMaxRetries(), e.getMessage());

        // 指数退避，使用配置的延迟
        try {
          Thread.sleep(batchConfig.getRetryDelayMs() * (long) Math.pow(2, retryCount - 1));
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    log.error("批处理在 {} 次重试后仍然失败: {}", batchConfig.getMaxRetries(), lastException.getMessage());
    // 返回空向量列表表示失败
    return batch.stream().map(s -> (String) null).collect(Collectors.toList());
  }

  /**
   * 处理单个批次的核心逻辑
   */
  private List<String> processBatch(List<String> batch) {
    long batchStartTime = System.currentTimeMillis();

    try {
      EmbeddingResponse response;

      List<String> vectors;
      if (embeddingModel == null) {
        // 离线模式：直接生成Mock向量字符串
        log.info("使用离线模式生成Mock嵌入向量");
        vectors = generateMockVectorStrings(batch);
      } else {
        // 在线模式：调用真实API
        response = embeddingModel.embedForResponse(batch);
        vectors = new ArrayList<>();
        for (int i = 0; i < response.getResults().size(); i++) {
          float[] embedding = response.getResults().get(i).getOutput();
          if (embedding != null && embedding.length > 0) {
            StringBuilder vectorStr = new StringBuilder("[");
            for (int j = 0; j < embedding.length; j++) {
              if (j > 0)
                vectorStr.append(",");
              vectorStr.append(embedding[j]);
            }
            vectorStr.append("]");
            vectors.add(vectorStr.toString());
          } else {
            log.warn("第{}个文本的嵌入向量为空", i);
            vectors.add(null);
          }
        }
      }

      long batchEndTime = System.currentTimeMillis();
      log.debug("批次处理完成，耗时: {}ms，成功: {}/{}",
          (batchEndTime - batchStartTime),
          vectors.stream().filter(Objects::nonNull).count(),
          batch.size());

      return vectors;

    } catch (Exception e) {
      long batchEndTime = System.currentTimeMillis();
      log.error("批次处理失败，耗时: {}ms，错误: {}",
          (batchEndTime - batchStartTime), e.getMessage());

      if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
        log.error("网络连接问题：可能是API密钥无效或网络故障。请检查API密钥配置。");
      } else if (e.getMessage() != null && e.getMessage().contains("401")) {
        log.error("认证失败：API密钥无效或已过期。请更新spring.ai.dashscope.api-key配置。");
      } else if (e.getMessage() != null && e.getMessage().contains("403")) {
        log.error("访问被拒绝：可能没有足够的权限或配额已用完。");
      }

      throw e;
    }
  }

  /**
   * 生成Mock向量字符串（离线测试模式）
   */
  private List<String> generateMockVectorStrings(List<String> texts) {
    log.info("生成Mock向量字符串，文本数量: {}", texts.size());

    List<String> vectors = new ArrayList<>();
    for (String text : texts) {
      // 创建一个简单的1024维模拟向量
      StringBuilder vectorStr = new StringBuilder("[");
      int hash = text.hashCode();
      for (int i = 0; i < 1024; i++) {
        if (i > 0)
          vectorStr.append(",");
        // 使用文本的hash来生成确定性的向量值
        float value = (float) (Math.sin(hash + i) * 0.1); // 范围在-0.1到0.1之间
        vectorStr.append(value);
      }
      vectorStr.append("]");
      vectors.add(vectorStr.toString());
    }

    return vectors;
  }

  /**
   * 备用批处理方案（逐个生成）
   */
  private List<String> fallbackBatchGeneration(List<String> texts, int alreadyProcessed) {
    log.warn("使用备用方案处理剩余的 {} 个文本", texts.size() - alreadyProcessed);

    List<String> remainingVectors = new ArrayList<>();
    for (int i = alreadyProcessed; i < texts.size(); i++) {
      String vector = generateVector(texts.get(i));
      remainingVectors.add(vector);
    }

    return remainingVectors;
  }

  /**
   * 批量插入向量到数据库
   */
  private void batchInsertVectors(Long documentId, List<String> paragraphs, List<String> vectors) {
    if (paragraphs.size() != vectors.size()) {
      throw new IllegalArgumentException("段落数量和向量数量不匹配");
    }

    // 从 document 表获取 kb_id
    Long kbId = jdbcTemplate.queryForObject(
        "SELECT kb_id FROM document WHERE id = ? AND deleted = 0",
        Long.class, documentId);

    log.info("获取文档所属知识库: documentId={}, kbId={}", documentId, kbId);

    int batchSize = batchConfig.getDbBatchSize();
    log.info("开始批量插入向量，文档ID: {}, 总数量: {}, 批大小: {}", documentId, paragraphs.size(), batchSize);

    for (int i = 0; i < paragraphs.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, paragraphs.size());
      List<Object[]> batchArgs = new ArrayList<>();

      // 准备批数据
      for (int j = i; j < endIndex; j++) {
        String content = paragraphs.get(j);
        String vector = vectors.get(j);

        if (vector == null || vector.isEmpty()) {
          log.warn("跳过无效向量: documentId={}, index={}", documentId, j);
          continue;
        }

        batchArgs.add(new Object[] { documentId, content, vector, j, kbId });
      }

      if (!batchArgs.isEmpty()) {
        try {
          int[] results = jdbcTemplate.batchUpdate(
              "INSERT INTO " + VECTOR_TABLE
                  + " (document_id, content, vector, chunk_index, kb_id, created_time) VALUES (?, ?, ?, ?, ?, NOW())",
              batchArgs);

          int successCount = 0;
          for (int result : results) {
            if (result > 0)
              successCount++;
          }

          log.info("批量插入完成: documentId={}, 批次 {}-{}, 成功: {}/{}",
              documentId, i, endIndex - 1, successCount, batchArgs.size());

        } catch (Exception e) {
          log.error("批量插入失败: documentId={}, 批次 {}-{}, error={}",
              documentId, i, endIndex - 1, e.getMessage());

          // 回退到单个插入
          fallbackSingleInserts(documentId, paragraphs, vectors, i, endIndex);
        }
      }
    }

    log.info("向量批量插入完成: documentId={}, 总数量: {}", documentId, paragraphs.size());
  }

  /**
   * 备用单个插入方法
   */
  private void fallbackSingleInserts(Long documentId, List<String> paragraphs, List<String> vectors,
      int startIndex, int endIndex) {
    log.warn("使用备用单个插入方法: documentId={}, 范围 {}-{}", documentId, startIndex, endIndex);

    for (int i = startIndex; i < endIndex; i++) {
      String content = paragraphs.get(i);
      String vector = vectors.get(i);

      if (vector == null || vector.isEmpty()) {
        continue;
      }

      try {
        int rows = jdbcTemplate.update(
            "INSERT INTO " + VECTOR_TABLE
                + " (document_id, content, vector, chunk_index, created_time) VALUES (?, ?, ?, ?, NOW())",
            documentId, content, vector, i);
        if (rows > 0) {
          log.debug("单个插入成功: documentId={}, index={}", documentId, i);
        }
      } catch (Exception e) {
        log.error("单个插入失败: documentId={}, index={}, error={}", documentId, i, e.getMessage());
      }
    }
  }

  private List<String> splitByParagraph(String content) {
    if (content == null || content.isEmpty()) {
      log.debug("内容为空，返回空列表");
      return new ArrayList<>();
    }

    try {
      String chunkingStrategy = seekDBConfig.getRag() != null ? seekDBConfig.getRag().getChunkingStrategy() : "smart";

      switch (chunkingStrategy) {
        case "token_count":
          return splitByTokenCount(content);
        case "semantic":
        case "smart":
          return smartSemanticSplit(content); // 使用智能语义分块
        case "fixed":
        default:
          return fixedSizeSplit(content);
      }
    } catch (Exception e) {
      log.warn("分块策略执行失败，使用备用分块策略: {}", e.getMessage());
      return fallbackSemanticSplit(content);
    }
  }

  /**
   * 基于Token计数的智能分块策略（简化版本）
   * 由于TokenCountDocumentSplitter在1.0.0-M4中不可用，使用字符估算
   */
  private List<String> splitByTokenCount(String content) {
    SeekDBConfig.RagConfig ragConfig = seekDBConfig.getRag();
    int chunkSize = ragConfig != null ? ragConfig.getChunkSize() : 512;
    int chunkOverlap = ragConfig != null ? ragConfig.getChunkOverlap() : 100;
    int minChunkSize = ragConfig != null ? ragConfig.getMinChunkSize() : 50;
    int maxChunkSize = ragConfig != null ? ragConfig.getMaxChunkSize() : 2000;

    log.info("使用TokenCount分块策略(简化版): chunkSize={}, chunkOverlap={}", chunkSize, chunkOverlap);

    try {
      int avgCharsPerToken = 4;
      int adjustedChunkSize = Math.min(chunkSize * avgCharsPerToken, maxChunkSize);
      int adjustedOverlap = Math.min(chunkOverlap * avgCharsPerToken, adjustedChunkSize / 2);

      List<String> chunks = new ArrayList<>();
      for (int i = 0; i < content.length(); i += (adjustedChunkSize - adjustedOverlap)) {
        int end = Math.min(i + adjustedChunkSize, content.length());
        String chunk = content.substring(i, end);
        if (chunk.trim().length() >= minChunkSize) {
          chunks.add(chunk);
        }
        if (end == content.length())
          break;
      }

      log.info("TokenCount分块完成: 输入长度={}, 输出chunk数={}", content.length(), chunks.size());
      return chunks;
    } catch (Exception e) {
      log.warn("TokenCount分块失败，使用固定分块: {}", e.getMessage());
      return fixedSizeSplit(content);
    }
  }

  /**
   * 固定大小分块策略（原有实现保留）
   */
  private List<String> fixedSizeSplit(String content) {
    List<String> chunks = new ArrayList<>();
    int chunkSize = 512;
    int overlap = 100;

    for (int i = 0; i < content.length(); i += (chunkSize - overlap)) {
      int end = Math.min(i + chunkSize, content.length());
      String chunk = content.substring(i, end);
      if (chunk.trim().length() >= 50) {
        chunks.add(chunk);
      }
      if (end == content.length())
        break;
    }

    log.info("固定分块完成: 输入长度={}, 输出chunk数={}", content.length(), chunks.size());
    return chunks;
  }

  /**
   * 智能语义分块策略 - 针对公安业务优化
   * 保持法律条文的完整性，同时确保语义连贯
   */
  private List<String> smartSemanticSplit(String content) {
    SeekDBConfig.RagConfig ragConfig = seekDBConfig.getRag();
    int maxChunkSize = ragConfig != null ? ragConfig.getMaxChunkSize() : 1000;
    int minChunkSize = ragConfig != null ? ragConfig.getMinChunkSize() : 100;
    int overlapSize = ragConfig != null ? ragConfig.getChunkOverlap() : 150;

    log.info("使用智能语义分块策略: maxChunkSize={}, minChunkSize={}, overlap={}",
        maxChunkSize, minChunkSize, overlapSize);

    List<String> chunks = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return chunks;
    }

    try {
      // 1. 优先按法律条文分割（第一级）
      List<String> articles = splitByArticles(content);

      // 2. 如果没有法律条文，按段落分割
      if (articles.size() <= 1) {
        articles = splitByParagraphs(content);
      }

      // 3. 对每个条目进行二次分块
      StringBuilder currentChunk = new StringBuilder();
      List<String> allSubChunks = new ArrayList<>();

      for (String article : articles) {
        if (article.trim().isEmpty()) {
          continue;
        }

        // 如果单个条目太大，需要再次分割
        if (article.length() > maxChunkSize) {
          // 保存之前的chunk
          if (currentChunk.length() >= minChunkSize) {
            allSubChunks.add(currentChunk.toString());
          }

          // 分割大条目
          List<String> subChunks = splitLargeContent(article, maxChunkSize, minChunkSize);
          allSubChunks.addAll(subChunks);

          // 重置当前chunk
          currentChunk = new StringBuilder();
        } else {
          // 添加到当前chunk
          if (currentChunk.length() > 0 && currentChunk.length() + article.length() + 1 <= maxChunkSize) {
            currentChunk.append("\n\n");
          }

          if (currentChunk.length() + article.length() <= maxChunkSize) {
            currentChunk.append(article.trim());
          } else {
            // 当前chunk已满，保存并开始新chunk
            if (currentChunk.length() >= minChunkSize) {
              allSubChunks.add(currentChunk.toString());
            }
            currentChunk = new StringBuilder(article.trim());
          }
        }
      }

      // 添加最后一个chunk
      if (currentChunk.length() >= minChunkSize) {
        allSubChunks.add(currentChunk.toString());
      }

      // 4. 添加重叠区域
      chunks = addOverlap(allSubChunks, overlapSize);

      log.info("智能语义分块完成: 输入长度={}, 条目数={}, 最终chunk数={}",
          content.length(), articles.size(), chunks.size());

    } catch (Exception e) {
      log.warn("智能语义分块失败，回退到备用分块策略: {}", e.getMessage());
      return fallbackSemanticSplit(content);
    }

    return chunks;
  }

  /**
   * 按法律条文分割（支持多种法律格式）
   */
  private List<String> splitByArticles(String content) {
    List<String> articles = new ArrayList<>();

    // 匹配各种法律条文格式
    // 1. 第XXX条 格式
    String[] patterns = {
        "(第[一二三四五六七八九十百千]+条[\\s\\S]*?)(?=第[一二三四五六七八九十百千]+条|$)",
        "(第\\d+条[\\s\\S]*?)(?=第\\d+条|$)",
        "(第\\d{1,3}条[\\s\\S]*?)(?=第\\d{1,3}条|$)",
        "\\n\\d+\\.[\\s\\S]*?(?=\\n\\d+\\.|$)",
        "\\n\\d{1,3}\\.[\\s\\S]*?(?=\\n\\d{1,3}\\.|$)"
    };

    boolean matched = false;
    for (String pattern : patterns) {
      try {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(content);

        while (m.find()) {
          String article = m.group(1).trim();
          if (article.length() >= 20) { // 过滤太短的匹配
            articles.add(article);
            matched = true;
          }
        }

        if (matched) {
          break;
        }
      } catch (Exception e) {
        log.debug("法律条文分割pattern失败: {}", e.getMessage());
      }
    }

    // 如果没有匹配到法律条文，返回原文本
    if (!matched) {
      articles.add(content);
    }

    return articles;
  }

  /**
   * 按段落分割
   */
  private List<String> splitByParagraphs(String content) {
    List<String> paragraphs = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return paragraphs;
    }

    // 按换行符分割，过滤空段落
    String[] parts = content.split("\\n\\n+");
    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty() && trimmed.length() >= 20) {
        paragraphs.add(trimmed);
      }
    }

    if (paragraphs.isEmpty()) {
      paragraphs.add(content);
    }

    return paragraphs;
  }

  /**
   * 分割大段内容
   */
  private List<String> splitLargeContent(String content, int maxSize, int minSize) {
    List<String> chunks = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return chunks;
    }

    // 先按句子分割
    List<String> sentences = splitBySentences(content);

    StringBuilder currentChunk = new StringBuilder();
    for (String sentence : sentences) {
      if (currentChunk.length() + sentence.length() + 1 <= maxSize) {
        if (currentChunk.length() > 0) {
          currentChunk.append("。");
        }
        currentChunk.append(sentence);
      } else {
        if (currentChunk.length() >= minSize) {
          chunks.add(currentChunk.toString());
        }
        currentChunk = new StringBuilder(sentence);
      }
    }

    if (currentChunk.length() >= minSize) {
      chunks.add(currentChunk.toString());
    }

    return chunks;
  }

  /**
   * 添加重叠区域
   */
  private List<String> addOverlap(List<String> chunks, int overlapSize) {
    if (chunks == null || chunks.isEmpty() || overlapSize <= 0) {
      return chunks;
    }

    List<String> overlappedChunks = new ArrayList<>();

    for (int i = 0; i < chunks.size(); i++) {
      String chunk = chunks.get(i);

      // 添加重叠前缀
      if (i > 0 && overlapSize > 0) {
        String previousChunk = chunks.get(i - 1);
        int prefixLength = Math.min(overlapSize, previousChunk.length());
        String prefix = previousChunk.substring(previousChunk.length() - prefixLength);
        chunk = "[上文接续] " + prefix + "\n\n" + chunk;
      }

      overlappedChunks.add(chunk);
    }

    return overlappedChunks;
  }

  /**
   * 备用语义分块策略 - 保持句子完整性
   */
  private List<String> fallbackSemanticSplit(String content) {
    List<String> chunks = new ArrayList<>();
    if (content == null || content.isEmpty()) {
      return chunks;
    }

    // 按句子分割（中文标点）
    List<String> sentences = splitBySentences(content);

    StringBuilder currentChunk = new StringBuilder();
    int maxChunkSize = 800; // 字符数，略高于token限制
    int minChunkSize = 100;

    for (String sentence : sentences) {
      if (currentChunk.length() + sentence.length() + 1 <= maxChunkSize) {
        // 可以添加这个句子
        if (currentChunk.length() > 0) {
          currentChunk.append("。");
        }
        currentChunk.append(sentence);
      } else {
        // 当前chunk已满，保存并开始新chunk
        if (currentChunk.length() >= minChunkSize) {
          chunks.add(currentChunk.toString());
        }

        // 开始新chunk
        if (sentence.length() > maxChunkSize) {
          // 超长句子截断
          currentChunk = new StringBuilder(sentence.substring(0, maxChunkSize));
        } else {
          currentChunk = new StringBuilder(sentence);
        }
      }
    }

    // 添加最后一个chunk
    if (currentChunk.length() >= minChunkSize) {
      chunks.add(currentChunk.toString());
    }

    log.debug("备用分块完成: 句子数={}, chunk数={}", sentences.size(), chunks.size());
    return chunks;
  }

  /**
   * 按句子分割 - 考虑中文和英文标点
   */
  private List<String> splitBySentences(String text) {
    List<String> sentences = new ArrayList<>();
    if (text == null || text.isEmpty()) {
      return sentences;
    }

    // 使用正则表达式按句末标点分割
    String[] parts = text.split("(?<=[。！？!?])\\s*");

    for (String part : parts) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        sentences.add(trimmed);
      }
    }

    // 如果没有找到句末标点，返回原文本
    if (sentences.isEmpty()) {
      sentences.add(text);
    }

    return sentences;
  }

  private boolean hasContent(Document doc) {
    return (doc.getContent() != null && !doc.getContent().isEmpty())
        || (doc.getSummary() != null && !doc.getSummary().isEmpty());
  }

  private List<SearchResult> parseSearchResults(List<Map<String, Object>> rows) {
    List<SearchResult> results = new ArrayList<>();
    Set<Long> seenIds = new HashSet<>(); // 用于去重

    try {
      for (Map<String, Object> row : rows) {
        Long documentId = ((Number) row.get("document_id")).longValue();

        // 跳过重复的documentId
        if (seenIds.contains(documentId)) {
          log.debug("跳过重复文档: id={}", documentId);
          continue;
        }
        seenIds.add(documentId);

        String content = (String) row.get("content");

        Object scoreObj = row.get("score");
        double score = 0.5;
        if (scoreObj != null) {
          if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).doubleValue();
          } else {
            score = Double.parseDouble(scoreObj.toString());
          }
        }

        Document doc = documentMapper.selectById(documentId);
        if (doc != null) {
          SearchResult result = new SearchResult();
          result.setDocumentId(documentId);
          result.setTitle(doc.getTitle());
          result.setSummary(doc.getSummary());
          result.setContent(content != null ? content : "");
          result.setScore(score);
          results.add(result);
          log.debug("搜索结果: id={}, title={}, score={}", documentId, doc.getTitle(), score);
        }
      }
      log.info("解析搜索结果完成: 总数={}, 去重前数量={}", results.size(), rows.size());
    } catch (Exception e) {
      log.error("解析搜索结果失败: {}", e.getMessage(), e);
    }
    return results;
  }

   /**
    * 构建混合搜索参数JSON
    */
  private String buildHybridSearchParam(String question, String questionVector, int topK, Integer kbId) {
    try {
      Map<String, Object> searchParam = new HashMap<>();

      // 全文搜索配置
      Map<String, Object> query = new HashMap<>();
      Map<String, Object> queryString = new HashMap<>();
      queryString.put("fields", List.of("content", "title"));
      queryString.put("query", question);
      query.put("query_string", queryString);
      searchParam.put("query", query);

      // KNN向量搜索配置
      Map<String, Object> knn = new HashMap<>();
      knn.put("field", "vector");
      knn.put("k", topK);
      knn.put("query_vector", parseVectorString(questionVector));

      // 过滤条件
      if (kbId != null && kbId > 0) {
        Map<String, Object> filter = new HashMap<>();
        Map<String, Object> term = new HashMap<>();
        term.put("kb_id", kbId);
        filter.put("term", term);
        knn.put("filter", filter);
      }
      searchParam.put("knn", knn);

      // 混合搜索配置
      Map<String, Object> hybrid = new HashMap<>();
      hybrid.put("weights", List.of(seekDBConfig.getVectorWeight(), seekDBConfig.getFulltextWeight()));
      hybrid.put("fusion", "rrf");
      searchParam.put("hybrid", hybrid);

      return objectMapper.writeValueAsString(searchParam);
    } catch (Exception e) {
      log.error("构建混合搜索参数失败: {}", e.getMessage());
      return null;
    }
  }

  /**
   * 执行混合搜索存储过程
   */
  private List<Map<String, Object>> executeHybridSearch(String searchParam) {
    try {
      String sql = "SET @result = ?; SELECT JSON_PRETTY(@result) AS result_json";
      return jdbcTemplate.queryForList(sql, searchParam);
    } catch (Exception e) {
      log.error("执行混合搜索存储过程失败: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * 解析混合搜索结果
   */
  private List<SearchResult> parseHybridSearchResults(List<Map<String, Object>> rows) {
    List<SearchResult> results = new ArrayList<>();

    try {
      for (Map<String, Object> row : rows) {
        Object resultJson = row.get("result_json");
        if (resultJson == null)
          continue;

        List<Map<String, Object>> items = objectMapper.readValue(
            resultJson.toString(),
            new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
            });

        for (Map<String, Object> item : items) {
          SearchResult result = new SearchResult();

          Object docIdObj = item.get("document_id");
          if (docIdObj != null) {
            result.setDocumentId(Long.parseLong(docIdObj.toString()));
          }

          result.setTitle((String) item.get("title"));
          result.setContent((String) item.get("content"));

          Object scoreObj = item.get("score");
          if (scoreObj != null) {
            result.setScore(Double.parseDouble(scoreObj.toString()));
          }

          // 获取文档完整信息
          Document doc = documentMapper.selectById(result.getDocumentId());
          if (doc != null) {
            result.setTitle(doc.getTitle());
            result.setSummary(doc.getSummary());
          }

          results.add(result);
        }
      }

      log.info("混合搜索结果解析完成: 总数={}", results.size());
    } catch (Exception e) {
      log.error("解析混合搜索结果失败: {}", e.getMessage(), e);
    }

    return results;
  }

  /**
   * 解析向量字符串为Float列表
   */
  private List<Float> parseVectorString(String vectorStr) {
    List<Float> vector = new ArrayList<>();
    try {
      // 移除方括号
      String cleaned = vectorStr.trim();
      if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
        cleaned = cleaned.substring(1, cleaned.length() - 1);
      }

      // 分割并转换
      String[] parts = cleaned.split(",");
      for (String part : parts) {
        vector.add(Float.parseFloat(part.trim()));
      }
    } catch (Exception e) {
      log.error("解析向量字符串失败: {}", e.getMessage());
    }
    return vector;
  }

  /**
   * 关键词搜索（混合搜索失败时的备用方案）
   */
  private List<SearchResult> keywordSearch(String question, int topK, Integer kbId) {
    log.info("关键词搜索 - 问题: {}, TopK: {}", question.substring(0, 30), topK);

    try {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT dv.id as document_id, dv.content, dv.title, 0.5 as score ");
      sql.append("FROM document_vectors dv ");
      sql.append("INNER JOIN document d ON dv.document_id = d.id AND d.deleted = 0 ");
      sql.append("WHERE dv.deleted = 0 ");

      if (kbId != null && kbId > 0) {
        sql.append("AND FIND_IN_SET(?, d.kb_path) ");
      }

      sql.append("AND (dv.title LIKE ? OR dv.content LIKE ?) ");
      sql.append("ORDER BY dv.id DESC ");
      sql.append("LIMIT ").append(topK);

      String keyword = "%" + escapeLike(question) + "%";
      List<Object> params = new ArrayList<>();
      if (kbId != null && kbId > 0) {
        params.add(kbId);
      }
      params.add(keyword);
      params.add(keyword);
      
      return jdbcTemplate.query(sql.toString(),
          (rs, rowNum) -> {
            SearchResult result = new SearchResult();
            result.setDocumentId(rs.getLong("document_id"));
            result.setTitle(rs.getString("title"));
            result.setContent(rs.getString("content"));
            result.setScore(rs.getDouble("score"));

            Document doc = documentMapper.selectById(result.getDocumentId());
            if (doc != null) {
              result.setSummary(doc.getSummary());
            }

            return result;
          }, params.toArray());

    } catch (Exception e) {
      log.error("关键词搜索失败: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  /**
   * JSON字符串转义
   *
   * @param str 原始字符串
   * @return 转义后的字符串
   */
  private String escapeJson(String str) {
    if (str == null)
      return "";
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }
}
