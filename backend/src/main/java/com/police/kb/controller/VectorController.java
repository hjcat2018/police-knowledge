package com.police.kb.controller;

import com.police.kb.common.Result;
import com.police.kb.mapper.DocumentMapper;
import com.police.kb.mapper.DocumentVectorMapper;
import com.police.kb.service.VectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/vector")
@RequiredArgsConstructor
public class VectorController {

  private final VectorService vectorService;
  private final DocumentMapper documentMapper;
  private final DocumentVectorMapper documentVectorMapper;
  private final JdbcTemplate jdbcTemplate;

  /**
   * 获取向量统计数据
   *
   * @return 统计数据
   */
  @GetMapping("/stats")
  public Result<Map<String, Object>> getStats() {
    try {
      Map<String, Object> stats = new HashMap<>();

      String totalDocSql = "SELECT COUNT(*) FROM document WHERE deleted = 0";
      Long totalDocuments = jdbcTemplate.queryForObject(totalDocSql, Long.class);

      String vectorizedSql = "SELECT COUNT(*) FROM document WHERE deleted = 0 AND status = 2";
      Long vectorizedDocuments = jdbcTemplate.queryForObject(vectorizedSql, Long.class);

      String totalVecSql = "SELECT COUNT(*) FROM document_vectors";
      Long totalVectors = jdbcTemplate.queryForObject(totalVecSql, Long.class);

      String kb2DocSql = "SELECT COUNT(*) FROM document WHERE deleted = 0 AND kb_id = 2";
      Long kb2Documents = jdbcTemplate.queryForObject(kb2DocSql, Long.class);

      String kb2VecSql = "SELECT COUNT(*) FROM document_vectors dv INNER JOIN document d ON dv.document_id = d.id WHERE d.deleted = 0 AND d.kb_id = 2";
      Long kb2Vectors = jdbcTemplate.queryForObject(kb2VecSql, Long.class);

      stats.put("totalDocuments", totalDocuments != null ? totalDocuments : 0);
      stats.put("vectorizedDocuments", vectorizedDocuments != null ? vectorizedDocuments : 0);
      stats.put("totalVectors", totalVectors != null ? totalVectors : 0);
      stats.put("kb2Documents", kb2Documents != null ? kb2Documents : 0);
      stats.put("kb2Vectors", kb2Vectors != null ? kb2Vectors : 0);

      log.info("向量统计数据 - 总文档数: {}, 已向量化: {}, 总向量数: {}, kb_id=2文档数: {}, kb_id=2向量数: {}",
          totalDocuments, vectorizedDocuments, totalVectors, kb2Documents, kb2Vectors);

      return Result.success(stats);
    } catch (Exception e) {
      log.error("获取向量统计失败: {}", e.getMessage(), e);
      return Result.error("获取统计失败: " + e.getMessage());
    }
  }

  /**
   * 向量化单个文档
   *
   * @param id 文档ID
   * @return 操作结果
   */
  @PostMapping("/vectorize/{id}")
  public Result<Void> vectorizeDocument(@PathVariable Long id) {
    try {
      vectorService.vectorizeDocument(id);
      return Result.success();
    } catch (Exception e) {
      log.error("文档向量化失败: {}", e.getMessage(), e);
      return Result.error("向量化失败: " + e.getMessage());
    }
  }

  /**
   * 向量化所有待处理的文档
   *
   * @return 操作结果
   */
  @PostMapping("/vectorize/all")
  public Result<Map<String, Object>> vectorizeAllDocuments() {
    try {
      int successCount = 0;
      int failCount = 0;

      String sql = "SELECT id FROM document WHERE deleted = 0 AND status = 1";
      var rows = jdbcTemplate.queryForList(sql);

      for (var row : rows) {
        Long docId = ((Number) row.get("id")).longValue();
        try {
          vectorService.vectorizeDocument(docId);
          successCount++;
        } catch (Exception e) {
          log.warn("文档 {} 向量化失败: {}", docId, e.getMessage());
          failCount++;
        }
      }

      Map<String, Object> result = new HashMap<>();
      result.put("successCount", successCount);
      result.put("failCount", failCount);

      return Result.success(result);
    } catch (Exception e) {
      log.error("批量向量化失败: {}", e.getMessage(), e);
      return Result.error("批量向量化失败: " + e.getMessage());
    }
  }

  /**
   * 获取指定知识库的文档列表（调试用）
   *
   * @param kbId 知识库ID
   * @return 文档列表
   */
  @GetMapping("/debug/docs/{kbId}")
  public Result<List<Map<String, Object>>> getDocumentsByKbId(@PathVariable Integer kbId) {
    try {
      String sql = "SELECT id, title, kb_id, status, created_time FROM document WHERE deleted = 0 AND kb_id = ? ORDER BY created_time DESC";
      List<Map<String, Object>> documents = jdbcTemplate.queryForList(sql, kbId);

      log.info("查询知识库 {} 的文档: {} 个", kbId, documents.size());

      return Result.success(documents);
    } catch (Exception e) {
      log.error("查询文档失败: {}", e.getMessage(), e);
      return Result.error("查询失败: " + e.getMessage());
    }
  }

  /**
   * 获取指定文档的向量内容片段（调试用）
   *
   * @param docId 文档ID
   * @return 向量内容片段
   */
  @GetMapping("/debug/vectors/{docId}")
  public Result<List<Map<String, Object>>> getVectorsByDocId(@PathVariable Long docId) {
    try {
      String sql = "SELECT id, document_id, content, LENGTH(content) as content_len, created_time FROM document_vectors WHERE document_id = ? AND deleted = 0 LIMIT 10";
      List<Map<String, Object>> vectors = jdbcTemplate.queryForList(sql, docId);

      log.info("查询文档 {} 的向量: {} 个", docId, vectors.size());

      // 打印部分内容用于调试
      for (var vec : vectors) {
        String content = (String) vec.get("content");
        if (content != null && content.length() > 100) {
          log.info("向量 {} 内容前100字: {}", vec.get("id"), content.substring(0, 100));
        } else if (content != null) {
          log.info("向量 {} 内容: {}", vec.get("id"), content);
        }
      }

      return Result.success(vectors);
    } catch (Exception e) {
      log.error("查询向量失败: {}", e.getMessage(), e);
      return Result.error("查询失败: " + e.getMessage());
    }
  }
}
